package com.edunexus.service;

import java.time.LocalDateTime;
import java.util.List;

public record ProgressSummary(
        int totalLessons,
        int completedLessons,
        int completionPercent,
        int averageQuizScore,
        int averageAssignmentScore,
        int estimatedStudyMinutes,
        List<ModuleProgressRow> moduleProgress,
        List<ActivityItem> recentActivities
) {
    public record ModuleProgressRow(String moduleTitle, int completedLessons, int totalLessons, int percent) {
    }

    public record ActivityItem(String description, LocalDateTime timestamp) {
    }
}
