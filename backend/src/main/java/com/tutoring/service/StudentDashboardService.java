package com.tutoring.service;

import com.tutoring.dto.StudentDashboardVO;

public interface StudentDashboardService {

    StudentDashboardVO getOverview(Long studentId);

}
