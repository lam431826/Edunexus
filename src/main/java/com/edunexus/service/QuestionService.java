package com.edunexus.service;

import com.edunexus.domain.Module;
import com.edunexus.domain.Question;
import com.edunexus.domain.QuestionOption;
import com.edunexus.domain.enums.ContentSource;
import com.edunexus.domain.enums.ContentStatus;
import com.edunexus.domain.enums.Difficulty;
import com.edunexus.dto.QuestionForm;
import com.edunexus.dto.QuestionOptionForm;
import com.edunexus.repository.QuestionRepository;
import com.edunexus.service.ai.DraftQuestion;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    public Question getById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question not found: " + id));
    }

    public List<Question> getByCourse(Long courseId) {
        return questionRepository.findByModule_CourseId(courseId);
    }

    @Transactional
    public Question createOrUpdate(Long questionId, Module module, QuestionForm form) {
        Question question = questionId == null ? new Question() : getById(questionId);
        question.setModule(module);
        question.setText(form.getText());
        question.setDifficulty(form.getDifficulty());
        question.setExplanation(form.getExplanation());
        if (question.getId() == null) {
            question.setSource(ContentSource.MANUAL);
            question.setStatus(ContentStatus.APPROVED);
        }
        question.getOptions().clear();
        int idx = 0;
        for (QuestionOptionForm optForm : form.getOptions()) {
            if (optForm.getText() == null || optForm.getText().isBlank()) {
                continue;
            }
            question.getOptions().add(QuestionOption.builder()
                    .question(question)
                    .text(optForm.getText())
                    .correct(optForm.isCorrect())
                    .orderIndex(idx++)
                    .build());
        }
        if (question.getOptions().stream().noneMatch(QuestionOption::isCorrect)) {
            throw new IllegalArgumentException("At least one option must be marked correct.");
        }
        return questionRepository.save(question);
    }

    /** Persists an AI-generated draft in Pending-review state (GBR-05). */
    @Transactional
    public Question saveAiDraft(Module module, DraftQuestion draft) {
        Question question = Question.builder()
                .module(module)
                .text(draft.text())
                .difficulty(draft.difficulty())
                .explanation(draft.explanation())
                .source(ContentSource.AI)
                .status(ContentStatus.PENDING_REVIEW)
                .build();
        List<QuestionOption> options = new ArrayList<>();
        for (int i = 0; i < draft.options().size(); i++) {
            options.add(QuestionOption.builder()
                    .question(question)
                    .text(draft.options().get(i))
                    .correct(i == draft.correctIndex())
                    .orderIndex(i)
                    .build());
        }
        question.setOptions(options);
        return questionRepository.save(question);
    }

    @Transactional
    public void approve(Question question) {
        question.setStatus(ContentStatus.APPROVED);
        questionRepository.save(question);
    }

    @Transactional
    public void reject(Question question) {
        question.setStatus(ContentStatus.REJECTED);
        questionRepository.save(question);
    }

    public void delete(Long id) {
        questionRepository.deleteById(id);
    }

    public record ImportRowResult(int rowNumber, boolean success, String message) {
    }

    public record ImportResult(List<ImportRowResult> rows, int successCount, int failureCount) {
    }

    /**
     * GBR-07: each row is imported independently — a bad row is reported without blocking valid rows.
     * Expected columns: Question | Difficulty | Option1 | Option2 | Option3 | Option4 | CorrectIndex(1-4) | Explanation
     */
    @Transactional
    public ImportResult importFromExcel(Module module, MultipartFile file) {
        List<ImportRowResult> rows = new ArrayList<>();
        int success = 0;
        int failure = 0;
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowBlank(row)) {
                    continue;
                }
                try {
                    String text = cellText(row, 0);
                    Difficulty difficulty = Difficulty.valueOf(cellText(row, 1).trim().toUpperCase());
                    List<String> options = new ArrayList<>();
                    for (int c = 2; c <= 5; c++) {
                        String opt = cellText(row, c);
                        if (!opt.isBlank()) {
                            options.add(opt);
                        }
                    }
                    int correctIndex = (int) Double.parseDouble(cellText(row, 6)) - 1;
                    String explanation = cellText(row, 7);

                    if (text.isBlank() || options.size() < 2 || correctIndex < 0 || correctIndex >= options.size()) {
                        throw new IllegalArgumentException("Missing required fields or invalid correct index.");
                    }

                    Question question = Question.builder()
                            .module(module)
                            .text(text)
                            .difficulty(difficulty)
                            .explanation(explanation)
                            .source(ContentSource.IMPORT)
                            .status(ContentStatus.APPROVED)
                            .build();
                    List<QuestionOption> qOptions = new ArrayList<>();
                    for (int i = 0; i < options.size(); i++) {
                        qOptions.add(QuestionOption.builder()
                                .question(question)
                                .text(options.get(i))
                                .correct(i == correctIndex)
                                .orderIndex(i)
                                .build());
                    }
                    question.setOptions(qOptions);
                    questionRepository.save(question);
                    rows.add(new ImportRowResult(r + 1, true, "Imported"));
                    success++;
                } catch (Exception rowEx) {
                    rows.add(new ImportRowResult(r + 1, false, rowEx.getMessage()));
                    failure++;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the uploaded file: " + e.getMessage(), e);
        }
        return new ImportResult(rows, success, failure);
    }

    /** Only a truly empty row (e.g. a trailing blank row) is skipped; a row with any content but a
     * missing Question is still reported as a failed row rather than silently ignored (GBR-07). */
    private boolean isRowBlank(Row row) {
        for (int c = 0; c <= 7; c++) {
            if (!cellText(row, c).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String cellText(Row row, int col) {
        var cell = row.getCell(col);
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
