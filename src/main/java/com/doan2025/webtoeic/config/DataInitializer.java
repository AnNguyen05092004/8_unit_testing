package com.doan2025.webtoeic.config;

import com.doan2025.webtoeic.constants.Constants;
import com.doan2025.webtoeic.constants.enums.*;
import com.doan2025.webtoeic.domain.*;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Random RANDOM = new Random();
    private static final String THEME = "https://res.cloudinary.com/dj1au6uyp/image/upload/v1751818849/images/torx1vzofcpdlaojbstt.jpg";
    private static final String VIDEO = "http://res.cloudinary.com/dj1au6uyp/video/upload/v1751819722/videos/uhh7etr5bpx1g2jhhnti.mp4";
    private final CheckInitRepository checkInitRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ManagerRepository managerRepository;
    private final ConsultantRepository consultantRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final PostRepository postRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final ScoreScaleRepository scoreScaleRepository;
    private final RangeTopicRepository topicRepository;
    @Value("${account.manager.email}")
    private String MANAGER_EMAIL;
    @Value("${account.manager.password}")
    private String MANAGER_PASSWORD;
    @Value("${account.consultant.email}")
    private String CONSULTANT_EMAIL;
    @Value("${account.consultant.password}")
    private String CONSULTANT_PASSWORD;
    @Value("${account.teacher.email}")
    private String TEACHER_EMAIL;
    @Value("${account.teacher.password}")
    private String TEACHER_PASSWORD;
    @Value("${account.student.password}")
    private String STUDENT_PASSWORD;
    @Value("${account.student.email}")
    private String STUDENT_EMAIL;

    @Override
    public void run(String... args) {
        generateDataUser();
//        generateDataPost();
//        generateDataCourse();
//        generateDataLesson();
        initDataRangeTopic();
        initDataScoreScale();
    }

    private void generateDataUser() {
        if (!checkInitRepository.existsByCode(ResponseObject.USER.name())) {
            if (!userRepository.existsByEmail(MANAGER_EMAIL)) {
                User user = new User();
                user.setEmail(MANAGER_EMAIL);
                user.setPassword(passwordEncoder.encode(MANAGER_PASSWORD));
                user.setFirstName("User");
                user.setLastName("Admin");
                user.setRole(ERole.MANAGER);
                boolean check = false;
                while (!check) {
                    String code = generatedUserCode(ERole.MANAGER);
                    if (!userRepository.existsByCode(code)) {
                        user.setCode(code);
                        check = true;
                    }
                }
                Manager manager = managerRepository.save(new Manager());
                user.setManager(manager);
                User u = userRepository.save(user);
                manager.setUser(u);
                managerRepository.save(manager);
            }
            if (!userRepository.existsByEmail(CONSULTANT_EMAIL)) {
                User user = new User();
                user.setEmail(CONSULTANT_EMAIL);
                user.setPassword(passwordEncoder.encode(CONSULTANT_PASSWORD));
                user.setFirstName("User");
                user.setLastName("Consultant");
                user.setRole(ERole.CONSULTANT);
                boolean check = false;
                while (!check) {
                    String code = generatedUserCode(ERole.CONSULTANT);
                    if (!userRepository.existsByCode(code)) {
                        user.setCode(code);
                        check = true;
                    }
                }
                Consultant consultant = consultantRepository.save(new Consultant());
                user.setConsultant(consultant);
                userRepository.save(user);
                consultant.setUser(user);
                consultantRepository.save(consultant);
            }
            if (!userRepository.existsByEmail(TEACHER_EMAIL)) {
                User user = new User();
                user.setEmail(TEACHER_EMAIL);
                user.setPassword(passwordEncoder.encode(TEACHER_PASSWORD));
                user.setFirstName("User");
                user.setLastName("Teacher");
                user.setRole(ERole.TEACHER);
                boolean check = false;
                while (!check) {
                    String code = generatedUserCode(ERole.TEACHER);
                    if (!userRepository.existsByCode(code)) {
                        user.setCode(code);
                        check = true;
                    }
                }
                Teacher teacher = teacherRepository.save(new Teacher("dai hoc 1", "Bang cap 1"));
                user.setTeacher(teacher);
                userRepository.save(user);
                teacher.setUser(user);
                teacherRepository.save(teacher);
            }
            if (!userRepository.existsByEmail(STUDENT_EMAIL)) {
                User user = new User();
                user.setEmail(STUDENT_EMAIL);
                user.setPassword(passwordEncoder.encode(STUDENT_PASSWORD));
                user.setFirstName("User");
                user.setLastName("Student");
                user.setRole(ERole.STUDENT);
                boolean check = false;
                while (!check) {
                    String code = generatedUserCode(ERole.STUDENT);
                    if (!userRepository.existsByCode(code)) {
                        user.setCode(code);
                        check = true;
                    }
                }
                Student student = studentRepository.save(new Student("Dai hoc 1", "Chuyen nganh 1"));
                user.setStudent(student);
                userRepository.save(user);
                student.setUser(user);
                studentRepository.save(student);
            }
            checkInitRepository.save(new CheckInit(ResponseObject.USER.name()));
        }

    }

    private void generateDataPost() {
        User author = userRepository.findByEmail(CONSULTANT_EMAIL)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        if (!checkInitRepository.existsByCode(ResponseObject.POST.name())) {
            for (int i = 0; i < 50; i++) {
                int index = RANDOM.nextInt(1, 5);
                ECategoryPost categoryPost = ECategoryPost.fromValue(index);
                Post post = new Post("Post Title " + i, "Post Content " + i, THEME, author, categoryPost);
                postRepository.save(post);
            }
            checkInitRepository.save(new CheckInit(ResponseObject.POST.name()));
        }
    }

    private void generateDataCourse() {
        User author = userRepository.findByEmail(CONSULTANT_EMAIL)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        if (!checkInitRepository.existsByCode(ResponseObject.COURSE.name())) {
            for (int i = 0; i < 50; i++) {
                int index = RANDOM.nextInt(1, 5);
                long price = RANDOM.nextLong(100, 999);
                ECategoryCourse categoryCourse = ECategoryCourse.fromValue(index);
                Course course = new Course("Course Title " + i, "Course Description " + i, price, THEME, categoryCourse, author, author);
                courseRepository.save(course);
            }
            checkInitRepository.save(new CheckInit(ResponseObject.COURSE.name()));
        }
    }

    private void generateDataLesson() {
        if (!checkInitRepository.existsByCode(ResponseObject.LESSON.name())) {
            List<Course> courses = courseRepository.findAll();
            User createdBy = userRepository.findByEmail(CONSULTANT_EMAIL)
                    .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
            for (Course course : courses) {
                for (int i = 0; i < 10; i++) {
                    Lesson lesson = new Lesson("Lesson title " + i, "Lesson content " + i, VIDEO,
                            14.814308, i, course);
                    lesson.setCreatedBy(createdBy);
                    Lesson saveLesson = lessonRepository.save(lesson);
                    course.getLessons().add(saveLesson);
                    courseRepository.save(course);
                }
            }
            checkInitRepository.save(new CheckInit(ResponseObject.LESSON.name()));
        }

    }

    private void initDataScoreScale() {
        if (!checkInitRepository.existsByCode(ResponseObject.SCORE_SCALE.name())) {
            for (EScoreScale item : EScoreScale.values()) {
                scoreScaleRepository.save(new ScoreScale(
                        item.getValue(),
                        item.getCode(),
                        item.getFromScore(),
                        item.getToScore()
                ));
            }
        }
    }

    private void initDataRangeTopic() {
        if (!checkInitRepository.existsByCode(ResponseObject.RANGE_TOPIC.name())) {
            for (ERangeTopic gr : ERangeTopic.values()) {
                topicRepository.save(new RangeTopic(
                        gr.getValue(),
                        gr.name(),
                        gr.getTitle(),
                        gr.getDescription()
                ));
            }
        }
    }

    private String generatedUserCode(ERole role) {
        return switch (role) {
            case TEACHER -> Constants.PRE_CODE_TEACHER + new Random().nextLong(100_000_000, 999_999_999);
            case CONSULTANT -> Constants.PRE_CODE_CONSULTANT + new Random().nextLong(100_000_000, 999_999_999);
            case MANAGER -> Constants.PRE_CODE_MANAGER + new Random().nextLong(100_000_000, 999_999_999);
            default -> Constants.PRE_CODE_STUDENT + new Random().nextLong(100_000_000, 999_999_999);
        };
    }
}
