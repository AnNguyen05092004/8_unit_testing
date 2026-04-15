package com.doan2025.webtoeic.unitTesting.quizAI;

import com.doan2025.webtoeic.domain.RangeTopic;
import com.doan2025.webtoeic.domain.ScoreScale;
import com.doan2025.webtoeic.dto.response.AiResponse;
import com.doan2025.webtoeic.dto.response.QuestionResponse;
import com.doan2025.webtoeic.repository.RangeTopicRepository;
import com.doan2025.webtoeic.repository.ScoreScaleRepository;
import com.doan2025.webtoeic.service.ReaderService;
import com.doan2025.webtoeic.service.impl.AIServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIServiceImplTest {

    @Mock
    private ChatClient.Builder builder;
    @Mock
    private ChatClient chatClient;
    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;
    @Mock
    private RangeTopicRepository rangeTopicRepository;
    @Mock
    private ScoreScaleRepository scoreScaleRepository;
    @Mock
    private ReaderService readerService;

    private AIServiceImpl aiService;

    @BeforeEach
    void setUp() {
        when(builder.build()).thenReturn(chatClient);
        aiService = new AIServiceImpl(builder, rangeTopicRepository, scoreScaleRepository, readerService);
    }

    @Nested
    @DisplayName("checkCallAI - Check AI connectivity")
    class CheckCallAITests {

        @Test
        @DisplayName("TC_AI_001 - checkCallAI returns content successfully")
        void checkCallAI_WithValidChatFlow_ShouldReturnContent() {
            when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
            when(requestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.content()).thenReturn("AI works in a few words");

            String result = aiService.checkCallAI();

            assertThat(result).isEqualTo("AI works in a few words");
            verify(chatClient).prompt(any(Prompt.class));
            verify(requestSpec).call();
            verify(callResponseSpec).content();
        }

        @Test
        @DisplayName("TC_AI_002 - checkCallAI throws when AI call fails")
        void checkCallAI_WhenChatClientFails_ShouldThrowException() {
            when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
            when(requestSpec.call()).thenThrow(new RuntimeException("AI service unavailable"));

            assertThatThrownBy(() -> aiService.checkCallAI())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("AI service unavailable");

            verify(chatClient).prompt(any(Prompt.class));
            verify(requestSpec).call();
        }
    }

    @Nested
    @DisplayName("analysisWithAI - Analyze content and generate questions")
    class AnalysisWithAITests {

        @Test
        @DisplayName("TC_AI_003 - analysisWithAI returns generated questions successfully")
        void analysisWithAI_WithValidInput_ShouldReturnAiResponse() {
            String url = "https://example.com/input.pdf";
            String fileContent = "This is file content";

            RangeTopic rangeTopic = new RangeTopic();
            rangeTopic.setContent("GRAMMAR");

            ScoreScale scoreScale = new ScoreScale();
            scoreScale.setTitle("EASY");

            QuestionResponse questionResponse = QuestionResponse.builder()
                    .questionContent("Sample question")
                    .category("GRAMMAR")
                    .difficulty("EASY")
                    .build();

            when(readerService.readContentOfFile(url)).thenReturn(fileContent);
            when(rangeTopicRepository.findAll()).thenReturn(List.of(rangeTopic));
            when(scoreScaleRepository.findAll()).thenReturn(List.of(scoreScale));

            when(chatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.system(anyString())).thenReturn(requestSpec);
            when(requestSpec.user(anyString())).thenReturn(requestSpec);
            when(requestSpec.options(any(ChatOptions.class))).thenReturn(requestSpec);
            when(requestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.entity(any(ParameterizedTypeReference.class)))
                    .thenReturn(List.of(questionResponse));

            AiResponse result = aiService.analysisWithAI(url);

            assertThat(result).isNotNull();
            assertThat(result.getUrl()).isEqualTo(url);
            assertThat(result.getQuestions()).hasSize(1);
            assertThat(result.getQuestions().get(0).getQuestionContent()).isEqualTo("Sample question");

            verify(readerService).readContentOfFile(url);
            verify(rangeTopicRepository).findAll();
            verify(scoreScaleRepository).findAll();
            verify(chatClient).prompt();
            verify(requestSpec).system(anyString());
            verify(requestSpec).user(anyString());
            verify(requestSpec).options(any(ChatOptions.class));
            verify(requestSpec).call();
        }

        @Test
        @DisplayName("TC_AI_004 - analysisWithAI throws when file reader fails")
        void analysisWithAI_WhenReaderFails_ShouldThrowException() {
            String url = "https://example.com/broken.pdf";

            when(readerService.readContentOfFile(url)).thenThrow(new RuntimeException("Cannot read file"));

            assertThatThrownBy(() -> aiService.analysisWithAI(url))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Cannot read file");

            verify(readerService).readContentOfFile(url);
            verify(chatClient, never()).prompt();
        }

        @Test
        @DisplayName("TC_AI_005 - analysisWithAI throws when AI response mapping fails")
        void analysisWithAI_WhenAIEntityMappingFails_ShouldThrowException() {
            String url = "https://example.com/input.pdf";

            RangeTopic rangeTopic = new RangeTopic();
            rangeTopic.setContent("GRAMMAR");

            ScoreScale scoreScale = new ScoreScale();
            scoreScale.setTitle("EASY");

            when(readerService.readContentOfFile(url)).thenReturn("Some content");
            when(rangeTopicRepository.findAll()).thenReturn(List.of(rangeTopic));
            when(scoreScaleRepository.findAll()).thenReturn(List.of(scoreScale));

            when(chatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.system(anyString())).thenReturn(requestSpec);
            when(requestSpec.user(anyString())).thenReturn(requestSpec);
            when(requestSpec.options(any(ChatOptions.class))).thenReturn(requestSpec);
            when(requestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.entity(any(ParameterizedTypeReference.class)))
                    .thenThrow(new RuntimeException("AI output parse error"));

            assertThatThrownBy(() -> aiService.analysisWithAI(url))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("AI output parse error");

            verify(readerService).readContentOfFile(url);
            verify(chatClient).prompt();
            verify(requestSpec).call();
        }
    }
}
