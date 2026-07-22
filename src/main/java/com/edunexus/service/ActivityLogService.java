package com.edunexus.service;

import com.edunexus.domain.ActivityLog;
import com.edunexus.domain.User;
import com.edunexus.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Immutable audit trail (no update/delete path is ever exposed through the UI). */
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void log(User actor, String action, String targetType, Long targetId, String result, String detail) {
        activityLogRepository.save(ActivityLog.builder()
                .actor(actor)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .result(result)
                .detail(detail)
                .build());
    }

    public List<ActivityLog> getAll() {
        return activityLogRepository.findAllByOrderByCreatedAtDesc();
    }
}
