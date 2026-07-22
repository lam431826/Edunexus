package com.edunexus.web.admin;

import com.edunexus.domain.ActivityLog;
import com.edunexus.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/activity-log")
public class AdminActivityLogController {

    private static final int PAGE_SIZE = 50;

    private final ActivityLogService activityLogService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        List<ActivityLog> all = activityLogService.getAll();
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) PAGE_SIZE));
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));
        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, all.size());
        List<ActivityLog> pageItems = from < to ? all.subList(from, to) : List.of();

        model.addAttribute("logs", pageItems);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", all.size());
        return "admin/activity-log";
    }
}
