package ru.idemidov.interviewgateway;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.event.annotation.BeforeTestExecution;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import ru.idemidov.interviewgateway.model.ApiKeySettings;
import ru.idemidov.interviewgateway.model.Code;
import ru.idemidov.interviewgateway.model.Result;
import ru.idemidov.interviewgateway.repository.ApiKeySettingsRepository;
import ru.idemidov.interviewgateway.service.ApiKeySettingsManager;
import ru.idemidov.interviewgateway.service.Main;
import ru.idemidov.interviewgateway.service.QueueService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class MainControllerTest {
    ApiKeySettingsRepository apiKeySettingsRepositoryMock = Mockito.mock(ApiKeySettingsRepository.class);
    QueueService queueServiceMock = Mockito.mock(QueueService.class);
    RedisTemplate<String, String> redisTemplateMock = Mockito.mock(RedisTemplate.class);
    MessageSource messageSourceMock = Mockito.mock(MessageSource.class);
    MainController controller;

    @BeforeEach
    void init() {
        when(messageSourceMock.getMessage(anyString(), any(Object[].class), any(Locale.class))).thenReturn("bad request");

        controller = new MainController(
                new Main(new ApiKeySettingsManager(apiKeySettingsRepositoryMock), queueServiceMock, redisTemplateMock),
                new ApiKeySettingsManager(apiKeySettingsRepositoryMock),
                messageSourceMock
        );
    }

    @Test
    void execute_success() {
        controller.execute(new Code("test", "demo", "class Main {}"));
        verify(queueServiceMock, atLeast(1)).send(any(Code.class));
    }

    @Test
    void getResultByHash_success() {
        Map<Object, Object> mapMock = new HashMap<>();
        mapMock.put("123", "Exit code 0\ntest");
        HashOperations<String, Object, Object> hashOperationMock = Mockito.mock(HashOperations.class);
        when(redisTemplateMock.opsForHash()).thenReturn(hashOperationMock);
        when(hashOperationMock.entries(any())).thenReturn(mapMock);

        Assertions.assertEquals(new ResponseEntity<>(new Result("Exit code 0\ntest", ""), HttpStatus.OK), controller.getResult("123"));
    }

    @Test
    void saveTmp_wrongApiKey() {
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, controller.saveTmp(new Code("test", "test", "class Main { }")).getStatusCode());
    }

    @Test
    void getApiKeySettings() {
        ApiKeySettings targetApiKeySettings = new ApiKeySettings("test", "test", 10L, 100L, 1, LocalDateTime.now());
        when(apiKeySettingsRepositoryMock.findById(anyString())).thenReturn(Optional.of(targetApiKeySettings));
        Assertions.assertEquals(targetApiKeySettings.toString(), controller.getApiKeySettings("test", "test").getBody().getSuccess());
    }
}