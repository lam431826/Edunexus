package com.edunexus.domain;

import com.edunexus.domain.enums.LessonStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

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
    @Column(name = "body_markdown")
    private String bodyMarkdown;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Nationalized
    @Lob
    @Column(name = "ai_summary")
    private String aiSummary;

    @Column(name = "attachment_paths", length = 1000)
    private String attachmentPaths;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LessonStatus status = LessonStatus.DRAFT;

    @Column(name = "ai_generated", nullable = false)
    @Builder.Default
    private boolean aiGenerated = false;
}
