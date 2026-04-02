package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutoring.entity.Submission;
import com.tutoring.mapper.SubmissionMapper;
import com.tutoring.service.GradingAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradingCompensateService {

    private final SubmissionMapper submissionMapper;
    private final GradingAsyncService gradingAsyncService;

    /**
     * 定时补偿任务：每30秒检查一次未处理的提交
     * 处理那些因为事件未触发而一直处于 status=0 或 status=1 状态的提交
     */
    @Scheduled(fixedDelay = 30000)
    public void compensateUnprocessedSubmissions() {
        try {
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            
            List<Submission> pendingSubmissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                    .in(Submission::getStatus, Arrays.asList(0, 1))
                    .lt(Submission::getSubmitTime, fiveMinutesAgo)
                    .orderByAsc(Submission::getSubmitTime)
                    .last("LIMIT 10")
            );

            if (pendingSubmissions.isEmpty()) {
                return;
            }

            log.info("补偿任务发现 {} 条未处理的提交记录", pendingSubmissions.size());

            for (Submission submission : pendingSubmissions) {
                try {
                    log.info("补偿处理提交: submissionId={}, studentId={}, assignmentId={}, status={}, submitTime={}",
                        submission.getId(), submission.getStudentId(), submission.getAssignmentId(),
                        submission.getStatus(), submission.getSubmitTime());

                    submission.setStatus(2);
                    submissionMapper.updateById(submission);

                    gradingAsyncService.processGrading(
                        submission.getId(),
                        submission.getStudentId(),
                        submission.getAssignmentId()
                    );

                    log.info("补偿处理完成: submissionId={}", submission.getId());
                } catch (Exception e) {
                    log.error("补偿处理失败: submissionId={}", submission.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("补偿任务执行异常", e);
        }
    }

    /**
     * 立即触发指定提交的批改（用于手动补偿）
     */
    public void triggerGradingImmediately(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new RuntimeException("提交记录不存在");
        }

        if (submission.getStatus() != null && submission.getStatus() >= 2) {
            log.warn("提交记录已在处理中或已完成: submissionId={}, status={}", 
                submissionId, submission.getStatus());
            return;
        }

        log.info("手动触发批改: submissionId={}", submissionId);

        submission.setStatus(2);
        submissionMapper.updateById(submission);

        gradingAsyncService.processGrading(
            submission.getId(),
            submission.getStudentId(),
            submission.getAssignmentId()
        );
    }
}
