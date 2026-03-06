package com.tutoringsys.api.controller.admin;

import com.tutoringsys.api.dto.StatsOverviewVo;
import com.tutoringsys.common.response.R;
import com.tutoringsys.dao.mapper.AssignmentMapper;
import com.tutoringsys.dao.mapper.CourseMapper;
import com.tutoringsys.dao.mapper.UserMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    @Resource
    private UserMapper userMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private AssignmentMapper assignmentMapper;

    @GetMapping("/overview")
    public R<StatsOverviewVo> getOverview() {
        StatsOverviewVo vo = new StatsOverviewVo();

        // 统计用户总数（排除逻辑删除）
        vo.setTotalUsers(userMapper.selectCount(null));

        // 统计课程总数
        vo.setTotalCourses(courseMapper.selectCount(null));

        // 统计作业总数
        vo.setTotalAssignments(assignmentMapper.selectCount(null));

        // 统计今日提交量（这里简化处理，实际需要查询student_answer表）
        vo.setTodaySubmissions(0); // 待实现

        // 模拟LLM调用次数
        vo.setLlmCallsToday(0L);

        return R.ok(vo);
    }

    @GetMapping("/submission-trend")
    public R<Object> getSubmissionTrend() {
        // 实现近7天提交量趋势
        // 这里简化处理，实际需要按天统计
        return R.ok();
    }
}