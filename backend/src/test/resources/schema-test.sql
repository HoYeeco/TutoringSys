CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    avatar VARCHAR(255),
    email VARCHAR(100),
    phone VARCHAR(20),
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description CLOB,
    teacher_id BIGINT NOT NULL,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS course_selection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS assignment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description CLOB,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    deadline TIMESTAMP,
    total_score INT DEFAULT 100,
    status VARCHAR(20) DEFAULT 'draft',
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    content CLOB NOT NULL,
    options CLOB,
    answer VARCHAR(500),
    reference_answer CLOB,
    score INT DEFAULT 0,
    min_words INT,
    max_words INT,
    analysis CLOB,
    sort_order INT DEFAULT 0,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS submission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id VARCHAR(50),
    student_id BIGINT NOT NULL,
    assignment_id BIGINT NOT NULL,
    status INT DEFAULT 0,
    total_score INT,
    ai_total_score INT,
    final_total_score INT,
    review_status INT DEFAULT 0,
    reviewer_id BIGINT,
    review_time TIMESTAMP,
    submit_time TIMESTAMP,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer CLOB,
    score DECIMAL(5,2),
    ai_score DECIMAL(5,2),
    ai_feedback CLOB,
    teacher_score DECIMAL(5,2),
    teacher_feedback CLOB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS student_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT,
    student_id BIGINT,
    assignment_id BIGINT,
    question_id BIGINT,
    answer VARCHAR(500),
    answer_content CLOB,
    score INT,
    ai_score INT,
    final_score INT,
    ai_feedback CLOB,
    teacher_feedback CLOB,
    feedback CLOB,
    grader_type VARCHAR(20),
    review_status INT DEFAULT 0,
    reviewer_id BIGINT,
    review_time TIMESTAMP,
    is_draft INT DEFAULT 0,
    status INT DEFAULT 0,
    submit_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200),
    content CLOB,
    type VARCHAR(20) DEFAULT 'notification',
    related_id BIGINT,
    related_type VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message_id BIGINT NOT NULL,
    is_read INT DEFAULT 0,
    read_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS error_book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT,
    student_answer_id BIGINT,
    status INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS llm_call_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(100),
    user_id BIGINT,
    model_name VARCHAR(100),
    prompt_tokens INT,
    completion_tokens INT,
    total_tokens INT,
    status INT DEFAULT 0,
    error_message CLOB,
    request_time TIMESTAMP,
    response_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation_type VARCHAR(100),
    operation_content CLOB,
    operator VARCHAR(100),
    operator_role VARCHAR(50),
    ip_address VARCHAR(50),
    success INT DEFAULT 1,
    error_message CLOB,
    operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS login_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    success INT DEFAULT 1
);

CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value CLOB,
    description VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
