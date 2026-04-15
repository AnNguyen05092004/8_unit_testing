package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.*;
import com.doan2025.webtoeic.dto.SearchQuestionDto;
import com.doan2025.webtoeic.dto.request.AnswerRequest;
import com.doan2025.webtoeic.dto.request.QuestionRequest;
import com.doan2025.webtoeic.dto.response.AiResponse;
import com.doan2025.webtoeic.dto.response.AnswerResponse;
import com.doan2025.webtoeic.dto.response.BankResponse;
import com.doan2025.webtoeic.dto.response.QuestionResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.*;
import com.doan2025.webtoeic.service.AnswerService;
import com.doan2025.webtoeic.service.ExplanationQuestionService;
import com.doan2025.webtoeic.service.QuestionService;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class QuestionServiceImpl implements QuestionService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ConvertUtil convertUtil;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final ExplanationQuestionRepository explanationQuestionRepository;
    private final QuestionBankRepository questionBankRepository;
    private final RangeTopicRepository rangeTopicRepository;
    private final ScoreScaleRepository scoreScaleRepository;
    private final AnswerService answerService;
    private final ExplanationQuestionService explanationQuestionService;

    @Override
    public QuestionResponse getDetail(HttpServletRequest httpServletRequest, Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.QUESTION));
        return convertUtil.convertQuestionToDto(question);
    }

    @Override
    public Page<QuestionResponse> getQuestionList(HttpServletRequest httpServletRequest, SearchQuestionDto dto, Pageable pageable) {

        if (dto.getRangeTopics().isEmpty() || dto.getRangeTopics() == null) {
            dto.setRangeTopics(null);
        }

        if (dto.getScoreScales().isEmpty() || dto.getScoreScales() == null) {
            dto.setScoreScales(null);
        }

        Page<Question> questions = questionRepository.filterQuestion(dto, pageable);
        return questions.map(convertUtil::convertQuestionToDto);
    }

    @Override
    public BankResponse saveQuestion(HttpServletRequest httpServletRequest, AiResponse aiResponse) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        QuestionBank bank = QuestionBank.builder()
                .title(aiResponse.getQuestionBankTitle())
                .linkUrl(aiResponse.getUrl())
                .createBy(user)
                .build();
        QuestionBank savedBank = questionBankRepository.save(bank);

        List<QuestionResponse> questionResponses = aiResponse.getQuestions();
        for (QuestionResponse questionResponse : questionResponses) {
            RangeTopic rangeTopic = rangeTopicRepository.findByContent(questionResponse.getCategory());
            ScoreScale scoreScale = scoreScaleRepository.findByTitle(questionResponse.getDifficulty());
            Question question = Question.builder()
                    .content(questionResponse.getQuestionContent())
                    .questionBank(savedBank)
                    .rangeTopic(rangeTopic)
                    .scoreScale(scoreScale)
                    .createBy(user)
                    .build();
            Question savedQuestion = questionRepository.save(question);

            ExplanationQuestion explanationQuestion = ExplanationQuestion.builder()
                    .explanationEnglish(questionResponse.getExplanation().getExplanationEnglish())
                    .explanationVietnamese(questionResponse.getExplanation().getExplanationVietnamese())
                    .question(savedQuestion)
                    .createdBy(user)
                    .build();

            explanationQuestionRepository.save(explanationQuestion);

            long order = 1;

            for (AnswerResponse answerResponse : questionResponse.getAnswers()) {
                Answer answer = Answer.builder()
                        .question(savedQuestion)
                        .content(answerResponse.getContent())
                        .order(order)
                        .isCorrect(answerResponse.getCorrect())
                        .createdBy(user)
                        .build();
                answerRepository.save(answer);
                order++;
            }
        }
        return convertUtil.convertQuestionBankToDto(savedBank);
    }

    @Override
    public BankResponse addQuestionToBank(HttpServletRequest httpServletRequest, QuestionRequest questionRequest, Long bankId) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        QuestionBank bank = questionBankRepository.findById(bankId)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.BANK));
        Question question = Question.builder()
                .content(questionRequest.getQuestionContent())
                .questionBank(bank)
                .rangeTopic(rangeTopicRepository.findByContent(questionRequest.getCategory()))
                .scoreScale(scoreScaleRepository.findByTitle(questionRequest.getDifficulty()))
                .createBy(user)
                .build();
        Question savedQuestion = questionRepository.save(question);
        ExplanationQuestion explanationQuestion = ExplanationQuestion.builder()
                .explanationVietnamese(questionRequest.getExplanation().getExplanationVietnamese())
                .explanationEnglish(questionRequest.getExplanation().getExplanationEnglish())
                .question(savedQuestion)
                .build();

        explanationQuestionRepository.save(explanationQuestion);

        for (AnswerRequest item : questionRequest.getAnswers()) {
            Answer answer = Answer.builder()
                    .createdBy(user)
                    .isCorrect(item.getCorrect())
                    .question(savedQuestion)
                    .content(item.getContent())
                    .build();
            answerRepository.save(answer);
        }
        return convertUtil.convertQuestionBankToDto(bank);
    }

    @Override
    public BankResponse removeQuestionFromBank(HttpServletRequest httpServletRequest, List<Long> questionIds, Long bankId) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        QuestionBank bank = questionBankRepository.findById(bankId)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.BANK));
        for (Long questionId : questionIds) {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.QUESTION));

            question.setIsDelete(true);
            questionRepository.save(question);
        }
        return convertUtil.convertQuestionBankToDto(bank);
    }

    @Override
    public QuestionResponse updateQuestion(HttpServletRequest httpServletRequest, QuestionRequest questionRequest) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        Question question = questionRepository.findById(questionRequest.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.QUESTION));

        List.of(
                new FieldUpdateUtil<>(question::getContent, question::setContent, questionRequest.getQuestionContent()),
                new FieldUpdateUtil<>(question::getRangeTopic, question::setRangeTopic, rangeTopicRepository.findByContent(questionRequest.getCategory())),
                new FieldUpdateUtil<>(question::getScoreScale, question::setScoreScale, scoreScaleRepository.findByTitle(questionRequest.getDifficulty())),
                new FieldUpdateUtil<>(question::getIsActive, question::setIsActive, questionRequest.isActive()),
                new FieldUpdateUtil<>(question::getIsDelete, question::setIsDelete, questionRequest.isDelete())
        ).forEach(FieldUpdateUtil::updateIfNeeded);

        question.setUpdateBy(user);
        Question saved = questionRepository.save(question);
        questionRequest.getAnswers()
                .stream().map(item -> answerService.updateAnswer(httpServletRequest, item)).toList();
        explanationQuestionService.updateExplanationQuestion(httpServletRequest, questionRequest.getExplanation());
        return convertUtil.convertQuestionToDto(saved);
    }

}
