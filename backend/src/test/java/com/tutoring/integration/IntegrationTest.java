package com.tutoring.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutoring.dto.CreateAssignmentRequest;
import com.tutoring.dto.QuestionDTO;
import com.tutoring.dto.SubmitRequest;
import com.tutoring.entity.*;
import com.tutoring.mapper.*;
import com.tutoring.security.CustomUserDetailsService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseSelectionMapper courseSelectionMapper;

    @Autowired
    private AssignmentMapper assignmentMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static Long teacherId;
    private static Long studentId;
    private static Long courseId;
    private static Long assignmentId;

    @BeforeEach
    void setUp() {
        if (teacherId == null) {
            User teacher = new User();
            teacher.setUsername("test_teacher");
            teacher.setPassword(passwordEncoder.encode("password123"));
            teacher.setRealName("Test Teacher");
            teacher.setRole("TEACHER");
            teacher.setStatus(1);
            teacher.setDeleted(0);
            userMapper.insert(teacher);
            teacherId = teacher.getId();
        }

        if (studentId == null) {
            User student = new User();
            student.setUsername("test_student");
            student.setPassword(passwordEncoder.encode("password123"));
            student.setRealName("Test Student");
            student.setRole("STUDENT");
            student.setStatus(1);
            student.setDeleted(0);
            userMapper.insert(student);
            studentId = student.getId();
        }

        if (courseId == null) {
            Course course = new Course();
            course.setName("Test Course");
            course.setDescription("Test Course Description");
            course.setTeacherId(teacherId);
            course.setDeleted(0);
            courseMapper.insert(course);
            courseId = course.getId();

            CourseSelection selection = new CourseSelection();
            selection.setCourseId(courseId);
            selection.setStudentId(studentId);
            courseSelectionMapper.insert(selection);
        }
    }

    @Test
    @Order(1)
    void testTeacherLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                .param("username", "test_teacher")
                .param("password", "password123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("test_teacher"))
            .andExpect(jsonPath("$.data.role").value("TEACHER"))
            .andReturn();
    }

    @Test
    @Order(2)
    void testStudentLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                .param("username", "test_student")
                .param("password", "password123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("test_student"))
            .andExpect(jsonPath("$.data.role").value("STUDENT"))
            .andReturn();
    }

    @Test
    @Order(3)
    void testLoginWithWrongPassword() throws Exception {
        mockMvc.perform(post("/auth/login")
                .param("username", "test_teacher")
                .param("password", "wrongpassword"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    void testRegister() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"new_user\",\"password\":\"password123\",\"realName\":\"New User\",\"role\":\"STUDENT\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(5)
    @WithMockUser(username = "test_teacher", authorities = {"TEACHER"})
    void testCreateAssignment() throws Exception {
        CreateAssignmentRequest request = new CreateAssignmentRequest();
        request.setTitle("Test Assignment");
        request.setDescription("Test Assignment Description");
        request.setCourseId(courseId);
        request.setDeadline(LocalDateTime.now().plusDays(7));
        request.setTotalScore(100);

        List<QuestionDTO> questions = new ArrayList<>();
        
        QuestionDTO q1 = new QuestionDTO();
        q1.setType("SINGLE");
        q1.setContent("What is 2+2?");
        q1.setOptions(List.of("3", "4", "5", "6"));
        q1.setAnswer("B");
        q1.setScore(50);
        questions.add(q1);

        QuestionDTO q2 = new QuestionDTO();
        q2.setType("SUBJECTIVE");
        q2.setContent("Explain polymorphism in Java.");
        q2.setReferenceAnswer("Polymorphism allows objects to take multiple forms...");
        q2.setScore(50);
        questions.add(q2);

        request.setQuestions(questions);

        MvcResult result = mockMvc.perform(post("/teacher/assignments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        assignmentId = root.path("data").asLong();

        Assertions.assertNotNull(assignmentId);
        Assertions.assertTrue(assignmentId > 0);
    }

    @Test
    @Order(6)
    @WithMockUser(username = "test_teacher", authorities = {"TEACHER"})
    void testPublishAssignment() throws Exception {
        if (assignmentId == null || assignmentId == 0) {
            return;
        }
        mockMvc.perform(post("/teacher/assignments/" + assignmentId + "/publish")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(7)
    @WithMockUser(username = "test_student", authorities = {"STUDENT"})
    void testStudentGetAssignmentList() throws Exception {
        mockMvc.perform(get("/student/assignments/list")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(8)
    @WithMockUser(username = "test_student", authorities = {"STUDENT"})
    void testStudentGetAssignmentDetail() throws Exception {
        if (assignmentId == null || assignmentId == 0) {
            return;
        }
        mockMvc.perform(get("/student/assignments/" + assignmentId + "/detail"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(9)
    @WithMockUser(username = "test_student", authorities = {"STUDENT"})
    void testStudentSubmitAssignment() throws Exception {
        if (assignmentId == null || assignmentId == 0) {
            return;
        }
        SubmitRequest request = new SubmitRequest();

        List<SubmitRequest.AnswerItem> answers = new ArrayList<>();
        
        List<Question> questions = questionMapper.selectList(null);
        for (Question q : questions) {
            SubmitRequest.AnswerItem answer = new SubmitRequest.AnswerItem();
            answer.setQuestionId(q.getId());
            if ("SINGLE".equals(q.getType())) {
                answer.setAnswer("B");
            } else {
                answer.setAnswer("Polymorphism is the ability of an object to take on many forms.");
            }
            answers.add(answer);
        }

        request.setAnswers(answers);

        mockMvc.perform(post("/student/assignments/" + assignmentId + "/submit")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(10)
    @WithMockUser(username = "test_teacher", authorities = {"TEACHER"})
    void testTeacherGetReviewList() throws Exception {
        mockMvc.perform(get("/teacher/review/list")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(11)
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/user/profile"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @Order(12)
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

}
