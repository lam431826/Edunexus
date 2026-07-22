package com.edunexus.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

/**
 * Teacher-authored class supplement. Deliberately a parallel entity to Lesson (not a retrofit) so
 * original SME-owned course content can never be modified or deleted by a Teacher.
 */
@Entity
@Table(name = "class_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @Nationalized
    @Column(nullable = false, length = 200)
    private String title;

    @Nationalized
    @Lob
    @Column(name = "body_markdown")
    private String bodyMarkdown;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private int orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
