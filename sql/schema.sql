-- 创建user表
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL UNIQUE,
  `password` varchar(100) NOT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `role` enum('STUDENT','TEACHER','ADMIN') NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建course表
CREATE TABLE `course` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text,
  `teacher_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建course_selection表
CREATE TABLE `course_selection` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_course` (`student_id`,`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建assignment表
CREATE TABLE `assignment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `title` varchar(200) NOT NULL,
  `description` text,
  `deadline` datetime NOT NULL,
  `total_score` int DEFAULT '0',
  `status` enum('draft','published') DEFAULT 'draft',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建question表
CREATE TABLE `question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assignment_id` bigint DEFAULT NULL,
  `type` enum('single','multiple','judge','short_answer') NOT NULL,
  `content` text NOT NULL,
  `options` json DEFAULT NULL,
  `answer` json DEFAULT NULL,
  `score` int DEFAULT '0',
  `analysis` text,
  `sort_order` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_assignment_id` (`assignment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建student_answer表
CREATE TABLE `student_answer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `submission_id` varchar(64) NOT NULL,
  `student_id` bigint NOT NULL,
  `assignment_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  `answer` json DEFAULT NULL,
  `is_graded` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_submission_id` (`submission_id`),
  KEY `idx_student_assignment` (`student_id`,`assignment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建grading_result表
CREATE TABLE `grading_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_answer_id` bigint NOT NULL,
  `score` decimal(5,2) DEFAULT NULL,
  `error_type` varchar(50) DEFAULT NULL COMMENT '错误类型：concept/logic/format',
  `correct_answer` json DEFAULT NULL COMMENT '标准答案（用于对比展示）',
  `error_points` json DEFAULT NULL,
  `suggestions` json DEFAULT NULL,
  `feedback` text,
  `grader_type` enum('auto','teacher') DEFAULT 'auto',
  `grader_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_answer` (`student_answer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建file_record表
CREATE TABLE `file_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `original_name` varchar(255) NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_type` varchar(50) DEFAULT NULL,
  `upload_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始管理员账号，密码将在首次启动时通过脚本生成并更新
INSERT INTO `user` (`username`, `password`, `real_name`, `role`) VALUES
('admin', '', 'Administrator', 'ADMIN');