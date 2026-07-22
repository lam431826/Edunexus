package com.edunexus.bootstrap;

import com.edunexus.domain.Assignment;
import com.edunexus.domain.ClassEntity;
import com.edunexus.domain.Course;
import com.edunexus.domain.CourseGroup;
import com.edunexus.domain.Enrollment;
import com.edunexus.domain.Flashcard;
import com.edunexus.domain.FlashcardDeck;
import com.edunexus.domain.Lesson;
import com.edunexus.domain.Module;
import com.edunexus.domain.Question;
import com.edunexus.domain.QuestionOption;
import com.edunexus.domain.RubricCriterion;
import com.edunexus.domain.SubscriptionPlan;
import com.edunexus.domain.User;
import com.edunexus.domain.enums.ClassStatus;
import com.edunexus.domain.enums.ContentSource;
import com.edunexus.domain.enums.ContentStatus;
import com.edunexus.domain.enums.CourseStatus;
import com.edunexus.domain.enums.Difficulty;
import com.edunexus.domain.enums.EnrollmentAccessType;
import com.edunexus.domain.enums.EnrollmentStatus;
import com.edunexus.domain.enums.LessonStatus;
import com.edunexus.domain.enums.PlanStatus;
import com.edunexus.domain.enums.Role;
import com.edunexus.repository.AssignmentRepository;
import com.edunexus.repository.ClassRepository;
import com.edunexus.repository.CourseGroupRepository;
import com.edunexus.repository.CourseRepository;
import com.edunexus.repository.EnrollmentRepository;
import com.edunexus.repository.FlashcardDeckRepository;
import com.edunexus.repository.FlashcardRepository;
import com.edunexus.repository.LessonRepository;
import com.edunexus.repository.ModuleRepository;
import com.edunexus.repository.QuestionRepository;
import com.edunexus.repository.SubscriptionPlanRepository;
import com.edunexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Seeds demo accounts and one full course so every screen renders with real data on first run. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final FlashcardDeckRepository flashcardDeckRepository;
    private final FlashcardRepository flashcardRepository;
    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseGroupRepository courseGroupRepository;
    private final ClassRepository classRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User sme = userRepository.save(User.builder()
                .name("Nguyễn Văn SME")
                .email("sme@edunexus.dev")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.SME)
                .enabled(true)
                .build());

        User student1 = userRepository.save(User.builder()
                .name("Trần Thị Học Viên")
                .email("student1@edunexus.dev")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.STUDENT)
                .enabled(true)
                .build());

        User student2 = userRepository.save(User.builder()
                .name("Lê Văn Sinh")
                .email("student2@edunexus.dev")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.STUDENT)
                .enabled(true)
                .build());

        User admin = userRepository.save(User.builder()
                .name("Quản Trị Viên")
                .email("admin@edunexus.dev")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .enabled(true)
                .build());

        User courseManager = userRepository.save(User.builder()
                .name("Phạm Thị Quản Lý")
                .email("cm@edunexus.dev")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.COURSE_MANAGER)
                .enabled(true)
                .build());

        User teacher = userRepository.save(User.builder()
                .name("Hoàng Văn Giáo Viên")
                .email("teacher@edunexus.dev")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.TEACHER)
                .enabled(true)
                .build());

        CourseGroup courseGroup = courseGroupRepository.save(CourseGroup.builder()
                .name("Lập trình Web")
                .description("Nhóm khóa học nền tảng về phát triển web.")
                .manager(courseManager)
                .build());

        Course course = courseRepository.save(Course.builder()
                .title("Nền tảng Lập trình Web")
                .description("Khóa học nhập môn HTML, CSS và JavaScript cho người mới bắt đầu.")
                .owner(sme)
                .status(CourseStatus.PUBLISHED)
                .version(1)
                .courseGroup(courseGroup)
                .unitPrice(BigDecimal.valueOf(299000))
                .build());

        ClassEntity demoClass = classRepository.save(ClassEntity.builder()
                .name("Lập trình Web - Lớp K01")
                .sourceCourse(course)
                .teacher(teacher)
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now().plusMonths(3))
                .maxSize(40)
                .fee(BigDecimal.valueOf(499000))
                .status(ClassStatus.PUBLISHED)
                .build());

        subscriptionPlanRepository.save(SubscriptionPlan.builder()
                .courseGroup(courseGroup)
                .name("Gói 3 tháng - Lập trình Web")
                .durationMonths(3)
                .price(BigDecimal.valueOf(699000))
                .status(PlanStatus.ACTIVE)
                .build());

        Module module1 = moduleRepository.save(Module.builder().course(course).title("Nhập môn HTML & CSS").orderIndex(0).build());
        Module module2 = moduleRepository.save(Module.builder().course(course).title("JavaScript cơ bản").orderIndex(1).build());

        seedLessons(module1, "Cấu trúc trang HTML", "Định dạng với CSS");
        seedLessons(module2, "Biến và kiểu dữ liệu", "Hàm và sự kiện");

        seedQuestions(module1, "HTML/CSS");
        seedQuestions(module2, "JavaScript");

        seedFlashcards(module1, "Thuật ngữ HTML/CSS");
        seedFlashcards(module2, "Thuật ngữ JavaScript");

        Assignment assignment = Assignment.builder()
                .module(module1)
                .title("Bài luận: Vai trò của HTML ngữ nghĩa")
                .promptMarkdown("Viết một bài luận ngắn (300-500 từ) phân tích tầm quan trọng của HTML ngữ nghĩa "
                        + "(semantic HTML) đối với khả năng truy cập và SEO của một trang web.")
                .maxScore(100)
                .build();
        assignment.setRubricCriteria(List.of(
                RubricCriterion.builder().assignment(assignment).name("Nội dung & Lập luận").weightPercent(50)
                        .descriptor("Luận điểm rõ ràng, có ví dụ minh họa").build(),
                RubricCriterion.builder().assignment(assignment).name("Cấu trúc bài viết").weightPercent(30)
                        .descriptor("Mở - thân - kết mạch lạc").build(),
                RubricCriterion.builder().assignment(assignment).name("Chính tả & Trình bày").weightPercent(20)
                        .descriptor("Không lỗi chính tả, trình bày sạch sẽ").build()
        ));
        assignmentRepository.save(assignment);

        // student1: direct H1 course purchase. student2: H2 class enrollment (access derived via demoClass -> course).
        enrollmentRepository.save(Enrollment.builder()
                .student(student1)
                .accessType(EnrollmentAccessType.H1_COURSE)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build());
        enrollmentRepository.save(Enrollment.builder()
                .student(student2)
                .accessType(EnrollmentAccessType.H2_CLASS)
                .classEntity(demoClass)
                .status(EnrollmentStatus.ACTIVE)
                .build());
    }

    private void seedLessons(Module module, String title1, String title2) {
        lessonRepository.save(Lesson.builder()
                .module(module).title(title1).orderIndex(0).status(LessonStatus.PUBLISHED)
                .bodyMarkdown("## " + title1 + "\n\nNội dung bài giảng mẫu giới thiệu các khái niệm cơ bản, "
                        + "kèm ví dụ minh họa để học viên thực hành theo.")
                .build());
        lessonRepository.save(Lesson.builder()
                .module(module).title(title2).orderIndex(1).status(LessonStatus.PUBLISHED)
                .bodyMarkdown("## " + title2 + "\n\nNội dung bài giảng mẫu tiếp theo, mở rộng thêm kiến thức "
                        + "đã học ở bài trước.")
                .build());
    }

    private void seedQuestions(Module module, String topic) {
        for (int i = 1; i <= 8; i++) {
            Question question = Question.builder()
                    .module(module)
                    .text("Câu hỏi mẫu " + i + " về " + topic + "?")
                    .difficulty(Difficulty.values()[i % Difficulty.values().length])
                    .explanation("Giải thích mẫu cho câu hỏi " + i + " về " + topic + ".")
                    .source(ContentSource.MANUAL)
                    .status(ContentStatus.APPROVED)
                    .build();
            question.setOptions(List.of(
                    QuestionOption.builder().question(question).text("Đáp án đúng " + i).correct(true).orderIndex(0).build(),
                    QuestionOption.builder().question(question).text("Phương án nhiễu A").correct(false).orderIndex(1).build(),
                    QuestionOption.builder().question(question).text("Phương án nhiễu B").correct(false).orderIndex(2).build(),
                    QuestionOption.builder().question(question).text("Phương án nhiễu C").correct(false).orderIndex(3).build()
            ));
            questionRepository.save(question);
        }
    }

    private void seedFlashcards(Module module, String deckName) {
        FlashcardDeck deck = flashcardDeckRepository.save(FlashcardDeck.builder()
                .module(module).name(deckName).description("Bộ thẻ ghi nhớ mẫu cho " + deckName).build());
        for (int i = 1; i <= 5; i++) {
            flashcardRepository.save(Flashcard.builder()
                    .deck(deck)
                    .frontText("Thuật ngữ " + i)
                    .backText("Định nghĩa mẫu cho thuật ngữ " + i + " trong " + deckName)
                    .orderIndex(i - 1)
                    .source(ContentSource.MANUAL)
                    .status(ContentStatus.APPROVED)
                    .build());
        }
    }
}
