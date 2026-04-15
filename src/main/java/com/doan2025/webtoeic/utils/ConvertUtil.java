package com.doan2025.webtoeic.utils;

import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.Class;
import com.doan2025.webtoeic.domain.*;
import com.doan2025.webtoeic.dto.response.*;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class ConvertUtil {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final ExplanationQuestionRepository explanationQuestionRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    public SubmitResponse convertSubmitToDto(HttpServletRequest request, StudentQuiz studentQuiz, boolean isList) {
        return SubmitResponse.builder()
                .idSubmitted(studentQuiz.getId())
                .score(studentQuiz.getScore())
                .user(modelMapper.map(studentQuiz.getUser(), UserResponse.class))
                .startAt(studentQuiz.getStartAt())
                .endAt(studentQuiz.getEndAt())
                .titleQuiz(studentQuiz.getQuiz().getTitle())
                .quiz(isList ? null : convertQuizResponseToDto(studentQuiz))
                .des(studentQuiz.getDes())
                .build();
    }

    public QuizResponse convertQuizResponseToDto(StudentQuiz studentQuiz) {
        List<Question> questionsInQuiz = questionRepository.findByQuizId(studentQuiz.getQuiz().getId());
        return QuizResponse.builder()
                .id(studentQuiz.getQuiz().getId())
                .title(studentQuiz.getQuiz().getTitle())
                .description(studentQuiz.getQuiz().getDescription())
                .totalQuestions(studentQuiz.getQuiz().getTotalQuestions())
                .status(studentQuiz.getQuiz().getStatus())
                .createAt(studentQuiz.getQuiz().getCreateAt())
                .updateAt(studentQuiz.getQuiz().getUpdateAt())
                .createBy(studentQuiz.getQuiz().getCreateBy() != null ? modelMapper.map(studentQuiz.getQuiz().getCreateBy(), UserResponse.class) : null)
                .updateBy(studentQuiz.getQuiz().getUpdateBy() != null ? modelMapper.map(studentQuiz.getQuiz().getUpdateBy(), UserResponse.class) : null)
                .questions(questionsInQuiz.stream().map(item -> convertQuestionResponseToDto(item, studentQuiz.getId())).toList())
                .build();
    }

    public QuestionResponse convertQuestionResponseToDto(Question question, Long studentQuiz) {
        ExplanationQuestion explanationQuestion = explanationQuestionRepository.findByQuestionId(question.getId());
        List<Answer> answers = answerRepository.findByQuestionId(question.getId());
        return QuestionResponse.builder()
                .questionContent(question.getContent())
                .difficulty(question.getScoreScale() != null ? question.getScoreScale().getTitle() : null)
                .category(question.getRangeTopic() != null ? question.getRangeTopic().getContent() : null)
                .id(question.getId())
                .explanation(convertExplanationQuestionToDto(explanationQuestion))
                .answers(answers.stream().map(item -> convertAnswerResponseToDto(item, studentQuiz)).toList())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .isDelete(question.getIsDelete())
                .isActive(question.getIsActive())
                .createdBy(question.getCreateBy() != null ? modelMapper.map(question.getCreateBy(), UserResponse.class) : null)
                .updatedBy(question.getUpdateBy() != null ? modelMapper.map(question.getUpdateBy(), UserResponse.class) : null)
                .build();
    }

    public AnswerResponse convertAnswerResponseToDto(Answer answer, Long studentQuiz) {
        StudentAnswer studentAnswer = studentAnswerRepository.findByAnswer_IdAndStudentQuiz_Id(answer.getId(), studentQuiz);
        return AnswerResponse.builder()
                .id(answer.getId())
                .correct(answer.getIsCorrect())
                .content(answer.getContent())
                .isDelete(answer.getIsDelete())
                .isActive(answer.getIsActive())
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt())
                .createdBy(answer.getCreatedAt() != null ?
                        modelMapper.map(answer.getCreatedAt(), UserResponse.class) : null)
                .updatedBy(answer.getUpdatedAt() != null ?
                        modelMapper.map(answer.getUpdatedAt(), UserResponse.class) : null)
                .isChoose(studentAnswer != null)
                .build();
    }

    public ShareQuizResponse convertShareQuizToDto(HttpServletRequest request, SharedQuiz sharedQuiz) {
        return ShareQuizResponse.builder()
                .sharedQuizId(sharedQuiz.getId())
                .startAt(sharedQuiz.getStartAt())
                .endAt(sharedQuiz.getEndAt())
                .createdAt(sharedQuiz.getCreatedAt())
                .updatedAt(sharedQuiz.getUpdatedAt())
                .isActive(sharedQuiz.getIsActive())
                .isDelete(sharedQuiz.getIsDelete())
                .clazz(convertClassToDto(request, sharedQuiz.getClazz()))
                .quiz(convertQuizToDto(sharedQuiz.getQuiz()))
                .createdBy(sharedQuiz.getCreatedBy() != null ? modelMapper.map(sharedQuiz.getCreatedBy(), UserResponse.class) : null)
                .updatedBy(sharedQuiz.getUpdatedBy() != null ? modelMapper.map(sharedQuiz.getUpdatedBy(), UserResponse.class) : null)
                .build();
    }

    public QuizResponse convertQuizToDto(Quiz quiz) {
        List<Question> questionsInQuiz = questionRepository.findByQuizId(quiz.getId());
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .totalQuestions(quiz.getTotalQuestions())
                .status(quiz.getStatus())
                .createAt(quiz.getCreateAt())
                .updateAt(quiz.getUpdateAt())
                .createBy(quiz.getCreateBy() != null ? modelMapper.map(quiz.getCreateBy(), UserResponse.class) : null)
                .updateBy(quiz.getUpdateBy() != null ? modelMapper.map(quiz.getUpdateBy(), UserResponse.class) : null)
                .questions(questionsInQuiz.stream().map(this::convertQuestionToDto).collect(Collectors.toList()))
                .build();
    }

    public BankResponse convertQuestionBankToDto(QuestionBank questionBank) {
        List<Question> questionsInBank = questionRepository.findByQuestionBankId(questionBank.getId());
        return BankResponse.builder()
                .id(questionBank.getId())
                .questionBankTitle(questionBank.getTitle())
                .url(questionBank.getLinkUrl())
                .questions(questionsInBank.stream().map(this::convertQuestionToDto).collect(Collectors.toList()))
                .isActive(questionBank.getIsActive())
                .isDelete(questionBank.getIsDelete())
                .createdAt(questionBank.getCreateAt())
                .updatedAt(questionBank.getUpdateAt() != null ? questionBank.getUpdateAt() : null)
                .createdBy(questionBank.getCreateBy() != null ?
                        modelMapper.map(questionBank.getCreateBy(), UserResponse.class) : null)
                .updatedBy(questionBank.getUpdateBy() != null ?
                        modelMapper.map(questionBank.getUpdateBy(), UserResponse.class) : null)
                .build();
    }

    public QuestionResponse convertQuestionToDto(Question question) {
        ExplanationQuestion explanationQuestion = explanationQuestionRepository.findByQuestionId(question.getId());
        List<Answer> answers = answerRepository.findByQuestionId(question.getId());
        return QuestionResponse.builder()
                .questionContent(question.getContent())
                .difficulty(question.getScoreScale() != null ? question.getScoreScale().getTitle() : null)
                .category(question.getRangeTopic() != null ? question.getRangeTopic().getContent() : null)
                .id(question.getId())
                .explanation(convertExplanationQuestionToDto(explanationQuestion))
                .answers(answers.stream().map(this::convertAnswerToDto).collect(Collectors.toList()))
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .isDelete(question.getIsDelete())
                .isActive(question.getIsActive())
                .createdBy(question.getCreateBy() != null ? modelMapper.map(question.getCreateBy(), UserResponse.class) : null)
                .updatedBy(question.getUpdateBy() != null ? modelMapper.map(question.getUpdateBy(), UserResponse.class) : null)
                .build();
    }

    public AnswerResponse convertAnswerToDto(Answer answer) {
        return AnswerResponse.builder()
                .id(answer.getId())
                .correct(answer.getIsCorrect())
                .content(answer.getContent())
                .isDelete(answer.getIsDelete())
                .isActive(answer.getIsActive())
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt())
                .createdBy(answer.getCreatedAt() != null ?
                        modelMapper.map(answer.getCreatedAt(), UserResponse.class) : null)
                .updatedBy(answer.getUpdatedAt() != null ?
                        modelMapper.map(answer.getUpdatedAt(), UserResponse.class) : null)
                .build();
    }

    public ExplanationQuestionResponse convertExplanationQuestionToDto(ExplanationQuestion explanationQuestion) {
        return ExplanationQuestionResponse.builder()
                .id(explanationQuestion.getId())
                .explanationVietnamese(explanationQuestion.getExplanationVietnamese())
                .explanationEnglish(explanationQuestion.getExplanationEnglish())
                .createdAt(explanationQuestion.getCreatedAt())
                .updatedAt(explanationQuestion.getUpdatedAt())
                .isActive(explanationQuestion.getIsActive())
                .isDelete(explanationQuestion.getIsDelete())
                .createdBy(explanationQuestion.getCreatedBy() != null ?
                        modelMapper.map(explanationQuestion.getCreatedBy(), UserResponse.class) : null)
                .updatedBy(explanationQuestion.getUpdatedBy() != null ?
                        modelMapper.map(explanationQuestion.getUpdatedBy(), UserResponse.class) : null)
                .build();
    }


    public ScoreScaleResponse convertScoreScaleToDto(HttpServletRequest request, ScoreScale scoreScale) {
        return ScoreScaleResponse.builder()
                .id(scoreScale.getId())
                .title(scoreScale.getTitle())
                .fromScore(scoreScale.getFromScore())
                .toScore(scoreScale.getToScore())
                .isActive(scoreScale.getIsActive())
                .createdAt(scoreScale.getCreatedAt())
                .updatedAt(scoreScale.getUpdatedAt())
                .isDelete(scoreScale.getIsDelete())
                .build();
    }

    public RangeTopicResponse convertRangeTopicToDto(HttpServletRequest request, RangeTopic topic) {
        return RangeTopicResponse.builder()
                .id(topic.getId())
                .vietnamese(topic.getVietnamese())
                .content(topic.getContent())
                .description(topic.getDescription())
                .isActive(topic.getIsActive())
                .isDelete(topic.getIsDelete())
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .build();
    }

    public SubmitExerciseResponse convertSubmitExerciseToDto(HttpServletRequest httpServletRequest,
                                                             SubmitExercise submitExercise) {
        return SubmitExerciseResponse.builder()
                .id(submitExercise.getId())
                .linkUrl(submitExercise.getLinkUrl())
                .isActive(submitExercise.getIsActive())
                .isDelete(submitExercise.getIsDelete())
                .createdAt(submitExercise.getCreatedAt())
                .updatedAt(submitExercise.getUpdatedAt())
                .createdBy(modelMapper.map(submitExercise.getCreatedBy(), UserResponse.class))
                .build();

    }

    public ClassNotificationResponse convertClassNotificationToDto(HttpServletRequest httpServletRequest,
                                                                   ClassNotification classNotification,
                                                                   List<AttachDocumentClass> attachDocumentClassList) {
        return ClassNotificationResponse.builder()
                .id(classNotification.getId())
                .description(classNotification.getDescription())
                .isPin(classNotification.getIsPin())
                .typeNotification(classNotification.getTypeNotification().getName())
                .fromDate(classNotification.getFromDate())
                .toDate(classNotification.getToDate())
                .isDelete(classNotification.getIsDelete())
                .isActive(classNotification.getIsActive())
                .createdAt(classNotification.getCreatedAt())
                .updatedAt(classNotification.getUpdatedAt())
                .updatedBy(classNotification.getUpdatedBy() == null ? null : modelMapper.map(classNotification.getUpdatedBy(), UserResponse.class))
                .createdBy(classNotification.getCreatedBy() == null ? null : modelMapper.map(classNotification.getCreatedBy(), UserResponse.class))
                .attachDocumentClasses(attachDocumentClassList.stream()
                        .map(item -> convertAttachDocumentClassToDto(httpServletRequest, item))
                        .toList())
                .build();
    }

    public AttachDocumentClassResponse convertAttachDocumentClassToDto(HttpServletRequest httpServletRequest, AttachDocumentClass attachDocumentClass) {
        return AttachDocumentClassResponse.builder()
                .id(attachDocumentClass.getId())
                .linkUrl(attachDocumentClass.getLinkUrl())
                .createdAt(attachDocumentClass.getCreatedAt())
                .createdBy(attachDocumentClass.getCreatedBy() == null ? null : modelMapper.map(attachDocumentClass.getCreatedBy(), UserResponse.class))
                .updatedAt(attachDocumentClass.getUpdatedAt() == null ? null : attachDocumentClass.getUpdatedAt())
                .updatedBy(attachDocumentClass.getUpdatedBy() == null ? null : modelMapper.map(attachDocumentClass.getUpdatedBy(), UserResponse.class))
                .isActive(attachDocumentClass.getIsActive())
                .isDelete(attachDocumentClass.getIsDelete())
                .build();
    }

    public ClassMemberResponse convertClassMemberToDto(HttpServletRequest httpServletRequest, ClassMember classMember) {
        return ClassMemberResponse.builder()
                .id(classMember.getId())
                .memberId(classMember.getMember().getId())
                .phone(classMember.getMember().getPhone())
                .address(classMember.getMember().getAddress())
                .email(classMember.getMember().getEmail())
                .joinDate(classMember.getJoinDate())
                .roleInClass(classMember.getRoleInClass().name())
                .status(classMember.getStatus().name())
                .name(classMember.getMember().getFirstName() + " " + classMember.getMember().getLastName())
                .build();
    }

    public ClassScheduleResponse convertScheduleToDto(HttpServletRequest request, ClassSchedule schedule) {
        return ClassScheduleResponse.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .status(schedule.getStatus().name())
                .startAt(schedule.getStartAt())
                .endAt(schedule.getEndAt())
                .isActive(schedule.getIsActive())
                .isDelete(schedule.getIsDelete())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .clazz(convertClassToDto(request, schedule.getClazz()))
                .createdBy(modelMapper.map(schedule.getCreatedBy(), UserResponse.class))
                .updatedBy(schedule.getUpdatedBy() == null ? null : modelMapper.map(schedule.getUpdatedBy(), UserResponse.class))
                .room(modelMapper.map(schedule.getRoom(), RoomResponse.class))
                .build();

    }

    public ClassResponse convertClassToDto(HttpServletRequest request, Class clazz) {
        return ClassResponse.builder()
                .id(clazz.getId())
                .name(clazz.getName())
                .description(clazz.getDescription())
                .title(clazz.getTitle())
                .status(clazz.getStatus().name())
                .createdAt(clazz.getCreatedAt())
                .updatedAt(clazz.getUpdatedAt())
                .createdByName(clazz.getCreatedBy().getFirstName() + " " + clazz.getCreatedBy().getLastName())
                .updatedByName(clazz.getUpdatedBy() == null ? null : clazz.getUpdatedBy().getFirstName() + " " + clazz.getUpdatedBy().getLastName())
                .teacher(modelMapper.map(clazz.getTeacher(), UserResponse.class))
                .build();

    }

    public OrderResponse convertOrderToDto(HttpServletRequest request, Orders order, OrderDetail orderDetail) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(order.getId());
        orderResponse.setStatus(order.getStatus());
        orderResponse.setPaymentMethod(order.getPaymentMethod());
        orderResponse.setCreatedAt(order.getCreatedAt());
        orderResponse.setUpdatedAt(order.getUpdatedAt());

        OrderDetailResponse detail = new OrderDetailResponse();
        detail.setId(orderDetail.getId());
        detail.setPriceAtPurchase(orderDetail.getPriceAtPurchase());
        CourseResponse courseResponse = convertCourseToDto(request, orderDetail.getCourse());
        courseResponse.setAuthor(orderDetail.getCourse().getAuthor() != null ? modelMapper.map(orderDetail.getCourse().getAuthor(), UserResponse.class) : null);
        courseResponse.setCreatedBy(orderDetail.getCourse().getCreatedBy() != null ? modelMapper.map(orderDetail.getCourse().getCreatedBy(), UserResponse.class) : null);
        courseResponse.setUpdatedBy(orderDetail.getCourse().getUpdatedBy() != null ? modelMapper.map(orderDetail.getCourse().getUpdatedBy(), UserResponse.class) : null);

        detail.setCourse(courseResponse);
        orderResponse.setDetail(convertOrderDetailToDto(request, orderDetail));
        return orderResponse;
    }

    public OrderDetailResponse convertOrderDetailToDto(HttpServletRequest request, OrderDetail orderDetail) {
        OrderDetailResponse detail = new OrderDetailResponse();
        detail.setId(orderDetail.getId());
        detail.setPriceAtPurchase(orderDetail.getPriceAtPurchase());
        CourseResponse courseResponse = convertCourseToDto(request, orderDetail.getCourse());
        courseResponse.setAuthor(null);
        courseResponse.setCreatedBy(null);
        courseResponse.setUpdatedBy(null);
        detail.setCourse(courseResponse);
        return detail;
    }

    public CartItemResponse convertCartItemToDto(HttpServletRequest httpServletRequest, CartItem cartItem) {
        CartItemResponse cartItemResponse = new CartItemResponse();
        cartItemResponse.setId(cartItem.getId());
        CourseResponse courseResponse = convertCourseToDto(httpServletRequest, cartItem.getCourse());
        courseResponse.setAuthor(null);
        courseResponse.setCreatedBy(null);
        courseResponse.setUpdatedBy(null);
        cartItemResponse.setCourse(courseResponse);
        return cartItemResponse;
    }

    public LessonResponse convertLessonToDto(HttpServletRequest httpServletRequest, Lesson lesson, List<AttachDocumentLesson> attachDocumentLessons) {
        LessonResponse lessonResponse = new LessonResponse();
        lessonResponse.setId(lesson.getId());
        lessonResponse.setTitle(lesson.getTitle());
        lessonResponse.setContent(lesson.getContent());
        lessonResponse.setVideoUrl(lesson.getVideoUrl());
        lessonResponse.setOrderIndex(lesson.getOrderIndex());
        lessonResponse.setCreatedAt(lesson.getCreatedAt());
        lessonResponse.setUpdatedAt(lesson.getUpdatedAt());
        lessonResponse.setIsPreviewAble(lesson.getIsPreviewAble());
        lessonResponse.setIsDelete(lesson.getIsDelete());
        lessonResponse.setIsActive(lesson.getIsActive());
        lessonResponse.setDuration(lesson.getDuration());
        lessonResponse.setOrderIndex(lesson.getOrderIndex());
        lessonResponse.setCreatedBy(modelMapper.map(lesson.getCreatedBy(), UserResponse.class));
        if (lesson.getUpdatedBy() != null) {
            lessonResponse.setUpdatedBy(modelMapper.map(lesson.getUpdatedBy(), UserResponse.class));
        }
        if (lesson.getUpdatedAt() != null) {
            lessonResponse.setUpdatedAt(lesson.getUpdatedAt());
        }
        String email = "";
        String bearerToken = httpServletRequest.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            email = jwtUtil.getEmailFromToken(httpServletRequest);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
            if (user.getRole().equals(ERole.MANAGER) || user.getRole().equals(ERole.CONSULTANT)) {
                lessonResponse.setIsBought(true);
            }
            if (lesson.getCourse().getEnrollments() != null && !lesson.getCourse().getEnrollments().isEmpty()) {
                for (Enrollment enrollment : lesson.getCourse().getEnrollments()) {
                    if (enrollment.getUser().equals(user)) {
                        lessonResponse.setIsBought(true);
                    }
                }
            }
        }
        lessonResponse.setAttachDocumentLessons(attachDocumentLessons.stream()
                .map(item -> convertAttachDocumentLessonToDto(httpServletRequest, item))
                .collect(Collectors.toList()));
        return lessonResponse;
    }

    public AttachDocumentLessonResponse convertAttachDocumentLessonToDto(HttpServletRequest httpServletRequest, AttachDocumentLesson attachDocumentLesson) {
        return AttachDocumentLessonResponse.builder()
                .linkUrl(attachDocumentLesson.getLinkUrl())
                .createdAt(attachDocumentLesson.getCreatedAt())
                .updatedAt(attachDocumentLesson.getUpdatedAt() == null ? null : attachDocumentLesson.getUpdatedAt())
                .id(attachDocumentLesson.getId())
                .isActive(attachDocumentLesson.getIsActive())
                .isDelete(attachDocumentLesson.getIsDelete())
                .build();
    }

    public CourseResponse convertCourseToDto(HttpServletRequest request, Course course) {
        CourseResponse courseResponse = new CourseResponse();
        courseResponse.setId(course.getId());
        courseResponse.setTitle(course.getTitle());
        courseResponse.setDescription(course.getDescription());
        courseResponse.setPrice(course.getPrice());
        courseResponse.setThumbnailUrl(course.getThumbnailUrl());
        courseResponse.setCategoryName(course.getCategoryCourse().getName());
        courseResponse.setIsActive(course.getIsActive());
        courseResponse.setIsDelete(course.getIsDelete());
        courseResponse.setCreatedAt(course.getCreatedAt());
        courseResponse.setUpdatedAt(course.getUpdatedAt());
        courseResponse.setIsBought(false);
        courseResponse.setAuthor(course.getAuthor() != null ? modelMapper.map(course.getAuthor(), UserResponse.class) : null);
        courseResponse.setCreatedBy(course.getCreatedBy() != null ? modelMapper.map(course.getCreatedBy(), UserResponse.class) : null);
        courseResponse.setUpdatedBy(course.getUpdatedBy() != null ? modelMapper.map(course.getUpdatedBy(), UserResponse.class) : null);
        courseResponse.setAuthorName(courseResponse.getAuthor().getFirstName() + " " + courseResponse.getAuthor().getLastName());
        courseResponse.setCreatedByName(courseResponse.getCreatedBy().getFirstName() + " " + courseResponse.getCreatedBy().getLastName());
        String email = "";
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            email = jwtUtil.getEmailFromToken(request);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
            if (user.getRole().equals(ERole.MANAGER) || user.getRole().equals(ERole.CONSULTANT)) {
                courseResponse.setIsBought(true);
            }
            if (course.getEnrollments() != null && !course.getEnrollments().isEmpty()) {
                for (Enrollment enrollment : course.getEnrollments()) {
                    if (enrollment.getUser().equals(user)) {
                        courseResponse.setIsBought(true);
                    }
                }
            }
        }
        return courseResponse;
    }
}
