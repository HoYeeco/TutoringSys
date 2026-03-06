# API接口说明

## 1. 认证模块

### 1.1 登录
- **URL**: `/api/auth/login`
- **方法**: `POST`
- **参数**:
  - `username`: 用户名
  - `password`: 密码
- **返回值示例**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "user": {
        "id": 1,
        "username": "admin",
        "role": "ADMIN"
      }
    }
  }
  ```

### 1.2 注册
- **URL**: `/api/auth/register`
- **方法**: `POST`
- **参数**:
  - `username`: 用户名
  - `password`: 密码
  - `role`: 角色 (STUDENT, TEACHER, ADMIN)
- **返回值示例**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "id": 2,
      "username": "student1",
      "role": "STUDENT"
    }
  }
  ```

## 2. 学生模块

### 2.1 获取作业列表
- **URL**: `/api/student/assignments/list`
- **方法**: `GET`
- **参数**:
  - `page`: 页码，默认1
  - `size`: 每页大小，默认10
  - `status`: 作业状态，可选值：pending, graded
- **返回值示例**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "records": [
        {
          "id": 1,
          "title": "数学作业1",
          "courseName": "高等数学",
          "deadline": "2026-03-10T23:59:59",
          "status": "pending"
        }
      ],
      "total": 1,
      "size": 10,
      "current": 1
    }
  }
  ```

### 2.2 提交作业
- **URL**: `/api/student/assignments/submit/{assignmentId}`
- **方法**: `POST`
- **参数**:
  - `assignmentId`: 作业ID
  - `answers`: 答案列表
- **返回值示例**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "score": 85,
      "feedback": "整体表现良好，个别题目需要加强",
      "submissionId": "sub_123456"
    }
  }
  ```

## 3. 教师模块

### 3.1 创建作业
- **URL**: `/api/teacher/assignments`
- **方法**: `POST`
- **参数**:
  - `title`: 作业标题
  - `courseId`: 课程ID
  - `questions`: 题目列表
  - `deadline`: 截止时间
- **返回值示例**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "id": 1,
      "title": "数学作业1",
      "courseId": 1,
      "deadline": "2026-03-10T23:59:59"
    }
  }
  ```

### 3.2 获取作业提交列表
- **URL**: `/api/teacher/assignments/{assignmentId}/submissions`
- **方法**: `GET`
- **参数**:
  - `assignmentId`: 作业ID
  - `page`: 页码，默认1
  - `size`: 每页大小，默认10
- **返回值示例**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "records": [
        {
          "id": "sub_123456",
          "studentId": 2,
          "studentName": "学生1",
          "submitTime": "2026-03-05T10:00:00",
          "score": 85
        }
      ],
      "total": 1,
      "size": 10,
      "current": 1
    }
  }
  ```

## 4. 管理员模块

### 4.1 创建用户
- **URL**: `/api/admin/users`
- **方法**: `POST`
- **参数**:
  - `username`: 用户名
  - `password`: 密码
  - `role`: 角色
  - `name`: 真实姓名
- **返回值示例**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "id": 3,
      "username": "teacher1",
      "role": "TEACHER",
      "name": "张老师"
    }
  }
  ```

### 4.2 创建课程
- **URL**: `/api/admin/courses`
- **方法**: `POST`
- **参数**:
  - `name`: 课程名称
  - `description`: 课程描述
  - `teacherId`: 教师ID
- **返回值示例**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "id": 1,
      "name": "高等数学",
      "description": "高等数学课程",
      "teacherId": 3
    }
  }
  ```

## 5. 统计模块

### 5.1 获取统计概览
- **URL**: `/api/admin/stats/overview`
- **方法**: `GET`
- **返回值示例**:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "totalUsers": 100,
      "totalCourses": 20,
      "totalAssignments": 50,
      "todaySubmissions": 5,
      "llmCallsToday": 10
    }
  }
  ```
