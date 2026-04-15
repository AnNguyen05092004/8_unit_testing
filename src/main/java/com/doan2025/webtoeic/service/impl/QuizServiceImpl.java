package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.Constants;
import com.doan2025.webtoeic.constants.enums.*;
import com.doan2025.webtoeic.domain.Class;
import com.doan2025.webtoeic.domain.*;
import com.doan2025.webtoeic.dto.SearchMemberInClassDto;
import com.doan2025.webtoeic.dto.SearchQuizDto;
import com.doan2025.webtoeic.dto.SearchSubmittedDto;
import com.doan2025.webtoeic.dto.request.QuizRequest;
import com.doan2025.webtoeic.dto.request.SharedQuizRequest;
import com.doan2025.webtoeic.dto.request.SubmitRequest;
import com.doan2025.webtoeic.dto.response.*;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.*;
import com.doan2025.webtoeic.service.QuizService;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import com.doan2025.webtoeic.utils.NotiUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class QuizServiceImpl implements QuizService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ConvertUtil convertUtil;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final QuestionBankRepository questionBankRepository;
    private final QuizRepository quizRepository;
    private final QuestionQuizRepository questionQuizRepository;
    private final ClassRepository classRepository;
    private final ShareQuizRepository shareQuizRepository;
    private final StudentQuizRepository studentQuizRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final ClassMemberRepository classMemberRepository;
    private final NotiUtils notiUtils;
    private final ModelMapper modelMapper;

    @Override
    public OverviewResponse statisticDetailQuizInClass(HttpServletRequest httpServletRequest,
                                                       Long idQuiz, Long idClass, Long score, SearchSubmittedDto dto) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        Long total = studentQuizRepository.filter(idQuiz, idClass, dto, null, null).getTotalElements();
        Long overScore = studentQuizRepository.countOver(idQuiz, dto, score);
        return OverviewResponse.builder()
                .total(total)
                .overScore(overScore)
                .underScore(total - overScore)
                .overScorePercent(BigDecimal.valueOf(overScore).divide(BigDecimal.valueOf(total)).multiply(BigDecimal.valueOf(100)))
                .underScorePercent(BigDecimal.valueOf(total - overScore).divide(BigDecimal.valueOf(total)).multiply(BigDecimal.valueOf(100)))
                .build();
    }

    @Override
    public OverviewResponse statisticOverviewQuizInClass(HttpServletRequest httpServletRequest, Long idClass, Long score, SearchQuizDto dto) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        Long total = (long) shareQuizRepository.filter(dto, idClass).size();
        Long overScore = shareQuizRepository.statisticOverviewOverScoreQuizInClass(idClass, dto, score);
        return OverviewResponse.builder()
                .total(total)
                .overScore(overScore)
                .underScore(total - overScore)
                .overScorePercent(BigDecimal.valueOf(overScore).divide(BigDecimal.valueOf(total)))
                .underScorePercent(BigDecimal.valueOf(total - overScore).divide(BigDecimal.valueOf(total)))
                .build();
    }

    @Override
    public SubmitResponse getDetailSubmitQuiz(HttpServletRequest httpServletRequest, Long idSubmitted) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        StudentQuiz studentQuiz = studentQuizRepository.findById(idSubmitted)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.SUBMIT));

        return convertUtil.convertSubmitToDto(httpServletRequest, studentQuiz, false);
    }

    @Override
    public Page<SubmitResponse> getListSubmitQuiz(HttpServletRequest httpServletRequest, Long iQuiz, Long idClass, SearchSubmittedDto dto, Pageable pageable) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Page<StudentQuiz> studentQuizzes;
        if (user.getRole().equals(ERole.TEACHER) || user.getRole().equals(ERole.STUDENT)) {
            if (!classMemberRepository.existsMemberInClass(idClass, user.getId())) {
                throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
            }
            if (user.getRole().equals(ERole.STUDENT)) {
                studentQuizzes = studentQuizRepository.filter(iQuiz, idClass, dto, pageable, user.getEmail());
            } else {
                studentQuizzes = studentQuizRepository.filter(iQuiz, idClass, dto, pageable, null);
            }

        } else {
            studentQuizzes = studentQuizRepository.filter(iQuiz, idClass, dto, pageable, null);
        }


        return studentQuizzes.map(item -> convertUtil.convertSubmitToDto(httpServletRequest, item, true));
    }

    @Override
    public Page<OverviewStudentSubmit> overviewStudentSubmitInClass(HttpServletRequest httpServletRequest, Long idClass, Pageable pageable) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        if (Objects.equals(user.getRole(), ERole.TEACHER) && classMemberRepository.existsMemberInClass(idClass, user.getId())
                || Objects.equals(user.getRole(), ERole.CONSULTANT)
                || Objects.equals(user.getRole(), ERole.MANAGER)) {
            SearchMemberInClassDto search = SearchMemberInClassDto.builder()
                    .classId(idClass)
                    .status(List.of("ACTIVE"))
                    .build();
            List<ClassMember> studentsInClass = classMemberRepository.findMembersInClass(search);
            SearchQuizDto searchQuizDto = new SearchQuizDto();
            List<SharedQuiz> quizzesInClass = shareQuizRepository.filter(searchQuizDto, idClass);

            List<OverviewStudentSubmit> result = new ArrayList<>();
            for (ClassMember classMember : studentsInClass) {

                User student = classMember.getMember();

                OverviewStudentSubmit overviewStudentSubmit = OverviewStudentSubmit.builder().build();
                overviewStudentSubmit.setUserResponse(modelMapper.map(student, UserResponse.class));

                List<StudentQuiz> submittedList = studentQuizRepository.findByUser_idAndClazz_id(student.getId(), idClass);

                Map<Long, StudentQuiz> submissionMap = submittedList.stream()
                        .collect(Collectors.toMap(
                                sq -> sq.getQuiz().getId(),
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));

                List<StudentQuizResponse> quizResponses = new ArrayList<>();

                for (SharedQuiz sharedQuiz : quizzesInClass) {
                    StudentQuizResponse quizRes = new StudentQuizResponse();

                    quizRes.setIdQuiz(sharedQuiz.getQuiz().getId());
                    quizRes.setTitleQuiz(sharedQuiz.getQuiz().getTitle());

                    if (submissionMap.containsKey(sharedQuiz.getQuiz().getId())) {
                        StudentQuiz submission = submissionMap.get(sharedQuiz.getQuiz().getId());

                        quizRes.setScore(submission.getScore());
                        quizRes.setStartAt(submission.getStartAt());
                        quizRes.setEndAt(submission.getEndAt());
                        quizRes.setDes(submission.getDes());
                    } else {
                        quizRes.setScore(null);
                        quizRes.setStartAt(null);
                        quizRes.setEndAt(null);
                        quizRes.setDes("Chưa làm");
                    }

                    quizResponses.add(quizRes);
                }

                overviewStudentSubmit.setQuizSubmit(quizResponses);
                result.add(overviewStudentSubmit);
            }

            return new PageImpl<>(result, pageable, pageable.getPageSize());
        }
        throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
    }

    @Override
    public void submitQuiz(HttpServletRequest httpServletRequest, Long quizId, List<SubmitRequest> requests, Long idClass, String des) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.QUIZ));

        Class clazz = classRepository.findById(idClass)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CLASS));

        StudentQuiz studentQuiz = StudentQuiz.builder()
                .user(user)
                .quiz(quiz)
                .clazz(clazz)
                .startAt((requests.isEmpty() || Objects.isNull(requests)) ? new Date() : requests.get(0).getStartAt())
                .endAt((requests.isEmpty() || Objects.isNull(requests)) ? new Date() : requests.get(0).getEndAt())
                .des(des)
                .build();

        StudentQuiz studentQuizSaved = studentQuizRepository.save(studentQuiz);

        long cntIsCorrect = 0;
        if (!requests.isEmpty()) {
            for (SubmitRequest request : requests) {
                Question question = questionRepository.findById(request.getQuestionId())
                        .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.QUESTION));

                Answer answer = answerRepository.findById(request.getAnswerId())
                        .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ANSWER));

                StudentAnswer studentAnswer = StudentAnswer.builder()
                        .answer(answer)
                        .question(question)
                        .studentQuiz(studentQuizSaved)
                        .isCorrect(answer.getIsCorrect())
                        .build();

                if (answer.getIsCorrect()) {
                    cntIsCorrect++;
                }
                studentAnswerRepository.save(studentAnswer);
            }
        }
        BigDecimal score = BigDecimal.valueOf(cntIsCorrect)
                .divide(BigDecimal.valueOf(quiz.getTotalQuestions()), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.TEN);
        studentQuizSaved.setScore(score);
        studentQuizRepository.save(studentQuizSaved);
    }

    @Override
    public Page<ShareQuizResponse> getListQuizInClass(HttpServletRequest httpServletRequest, Long idClass, SearchQuizDto dto, Pageable pageable) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        if (user.getRole().equals(ERole.TEACHER) || user.getRole().equals(ERole.STUDENT)) {
            if (!classMemberRepository.existsMemberInClass(idClass, user.getId())) {
                throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
            }
        }
        Page<SharedQuiz> sharedQuizList = shareQuizRepository.filter(dto, idClass, pageable);
        return sharedQuizList.map(
                item -> convertUtil.convertShareQuizToDto(httpServletRequest, item));
    }

    @Override
    public void updateQuizInClass(HttpServletRequest httpServletRequest, SharedQuizRequest request) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        if (user.getRole().equals(ERole.TEACHER)) {
            if (!classMemberRepository.existsMemberInClass(request.getClassId(), user.getId())) {
                throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
            }
        }
        SharedQuiz sharedQuiz = shareQuizRepository.findById(request.getSharedQuizId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.QUIZ));

        List.of(
                new FieldUpdateUtil<>(sharedQuiz::getStartAt, sharedQuiz::setStartAt, request.getStartAt()),
                new FieldUpdateUtil<>(sharedQuiz::getEndAt, sharedQuiz::setEndAt, request.getEndAt()),
                new FieldUpdateUtil<>(sharedQuiz::getIsActive, sharedQuiz::setIsActive, request.getIsActive()),
                new FieldUpdateUtil<>(sharedQuiz::getIsDelete, sharedQuiz::setIsDelete, request.getIsDelete())
        ).forEach(FieldUpdateUtil::updateIfNeeded);

        sharedQuiz.setUpdatedBy(user);
        shareQuizRepository.save(sharedQuiz);
    }

    @Override
    public void pullQuizToClass(HttpServletRequest httpServletRequest, SharedQuizRequest request) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        if (user.getRole().equals(ERole.TEACHER)) {
            if (!classMemberRepository.existsMemberInClass(request.getClassId(), user.getId())) {
                throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
            }
        }

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.QUIZ));

        Class clazz = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CLASS));

        SharedQuiz sharedQuiz = SharedQuiz.builder()
                .clazz(clazz)
                .quiz(quiz)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .createdBy(user)
                .build();
        shareQuizRepository.save(sharedQuiz);
        List<User> users = classMemberRepository.findMembersInClass(request.getClassId());
        notiUtils.sendNoti(users,
                ENotiType.NEW_QUIZ_IN_CLASS,
                Constants.NEW_QUIZ_IN_CLASS_CONTENT,
                Constants.NEW_QUIZ_IN_CLASS_CONTENT,
                clazz.getId());
    }

    @Override
    public QuizResponse getQuiz(HttpServletRequest httpServletRequest, Long id) {
        return convertUtil.convertQuizToDto(quizRepository.findById(id)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.QUIZ)));
    }

    @Override
    public Page<QuizResponse> getQuizes(HttpServletRequest httpServletRequest, SearchQuizDto dto, Pageable pageable) {
        Page<Quiz> quizes = quizRepository.filter(dto, pageable);
        return quizes.map(convertUtil::convertQuizToDto);
    }

    @Override
    public QuizResponse createQuiz(HttpServletRequest httpServletRequest, QuizRequest quizRequest) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Quiz quiz = Quiz.builder()
                .title(quizRequest.getTitle())
                .description(quizRequest.getDescription())
                .totalQuestions(0L)
                .createBy(user)
                .build();
        Quiz savedQuiz = quizRepository.save(quiz);
        return convertUtil.convertQuizToDto(savedQuiz);
    }

    @Override
    public QuizResponse convertBankToQuiz(HttpServletRequest httpServletRequest, Long idBank) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        QuestionBank bank = questionBankRepository.findById(idBank)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.BANK));
        List<Question> questions = questionRepository.findByQuestionBankId(idBank);
        Quiz quiz = Quiz.builder()
                .title(bank.getTitle())
                .description(null)
                .totalQuestions((long) questions.size())
                .createBy(user)
                .status(EQuizStatus.PRIVATE)
                .isStudentCreated(false)
                .build();
        Quiz savedQuiz = quizRepository.save(quiz);
        long order = 1;
        for (Question question : questions) {
            QuestionQuiz questionQuiz = QuestionQuiz.builder()
                    .quiz(savedQuiz)
                    .question(question)
                    .order(order)
                    .build();
            questionQuizRepository.save(questionQuiz);
            order += 1;
        }
        return convertUtil.convertQuizToDto(savedQuiz);
    }

    @Override
    public QuizResponse updateQuiz(HttpServletRequest httpServletRequest, QuizRequest quizRequest) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Quiz quiz = quizRepository.findById(quizRequest.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.QUIZ));
        List.of(
                new FieldUpdateUtil<>(quiz::getTitle, quiz::setTitle, quizRequest.getTitle()),
                new FieldUpdateUtil<>(quiz::getDescription, quiz::setDescription, quizRequest.getDescription()),
                new FieldUpdateUtil<>(quiz::getIsActive, quiz::setIsActive, quizRequest.getIsActive()),
                new FieldUpdateUtil<>(quiz::getIsDelete, quiz::setIsDelete, quizRequest.getIsDelete())
        ).forEach(FieldUpdateUtil::updateIfNeeded);
        quiz.setUpdateBy(user);
        Quiz savedQuiz = quizRepository.save(quiz);
        return convertUtil.convertQuizToDto(savedQuiz);
    }

    @Override
    public QuizResponse addQuestionToQuiz(HttpServletRequest httpServletRequest, QuizRequest quizRequest) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Quiz quiz = quizRepository.findById(quizRequest.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.QUIZ));
        long questionInQuizCount = questionQuizRepository.countByQuizId(quizRequest.getId());
        for (Long idQuestion : quizRequest.getIdQuestions()) {
            Question question = questionRepository.findById(idQuestion)
                    .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.QUESTION));

            QuestionQuiz questionQuiz = QuestionQuiz.builder()
                    .quiz(quiz)
                    .question(question)
                    .order(questionInQuizCount)
                    .build();
            questionQuizRepository.save(questionQuiz);
            questionInQuizCount += 1;
        }
        quiz.setUpdateBy(user);
        quiz.setTotalQuestions(questionInQuizCount);
        Quiz savedQuiz = quizRepository.save(quiz);
        return convertUtil.convertQuizToDto(savedQuiz);
    }

    @Override
    public QuizResponse removeQuestionFromQuiz(HttpServletRequest httpServletRequest, QuizRequest quizRequest) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Quiz quiz = quizRepository.findById(quizRequest.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.QUIZ));
        long questionInQuizCount = questionQuizRepository.countByQuizId(quizRequest.getId());
        for (Long idQuestion : quizRequest.getIdQuestions()) {
            questionQuizRepository.deleteQuestionQuizByQuizIdAndQuestionId(quizRequest.getId(), idQuestion);
            questionInQuizCount--;
        }
        quiz.setUpdateBy(user);
        quiz.setTotalQuestions(questionInQuizCount);
        Quiz savedQuiz = quizRepository.save(quiz);
        return convertUtil.convertQuizToDto(savedQuiz);
    }
}
