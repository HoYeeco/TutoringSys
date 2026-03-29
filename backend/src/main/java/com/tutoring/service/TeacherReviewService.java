package com.tutoring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.ReviewActionRequest;
import com.tutoring.dto.ReviewDetailVO;
import com.tutoring.dto.ReviewListVO;

import java.util.List;

public interface TeacherReviewService {

    Page<ReviewListVO> getReviewList(Long teacherId, Integer page, Integer size,
            Long courseId, Long assignmentId, String keyword);

    ReviewDetailVO getReviewDetail(Long teacherId, Long answerId);

    void acceptAiScore(Long teacherId, Long answerId, String teacherFeedback);

    void modifyScore(Long teacherId, Long answerId, Integer newScore, String teacherFeedback);

    void batchAccept(Long teacherId, List<Long> answerIds, String teacherFeedback);

    void batchModify(Long teacherId, List<Long> answerIds, Integer newScore, String teacherFeedback);

}
