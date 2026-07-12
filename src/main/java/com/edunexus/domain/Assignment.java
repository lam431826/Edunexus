package com.edunexus.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Nationalized
    @Column(nullable = false, length = 200)
    private String title;

    @Nationalized
    @Lob
    @Column(name = "prompt_markdown")
    private String promptMarkdown;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "max_score", nullable = false)
    @Builder.Default
    private int maxScore = 100;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RubricCriterion> rubricCriteria = new ArrayList<>();
}
