package com.tutoring.service;

import com.tutoring.dto.TeacherDashboardVO;

public interface TeacherDashboardService {

    TeacherDashboardVO getDashboardOverview(Long teacherId);

}
