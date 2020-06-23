package ru.idemidov.interviewgateway;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.idemidov.interviewgateway.model.Result;
import ru.idemidov.interviewgateway.repository.ApiKeySettingsRepository;
import ru.idemidov.interviewgateway.service.ApiKeySettingsManager;
import ru.idemidov.interviewgateway.service.Main;
import ru.idemidov.interviewgateway.service.QueueService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {TestConfiguration.class, RedisProperties.class})
class MainControllerTest {
    @MockBean
    ApiKeySettingsRepository apiKeySettingsRepositoryMock;
    @MockBean
    QueueService queueServiceMock;
    @MockBean
    RedisTemplate<String, String> redisTemplateMock;

    @Test
    void execute() {
    }

    @Test
    void getResultByHash_success() {
        Map<Object, Object> mapMock = new HashMap<>();
        mapMock.put("123", "Exit code 0\ntest");
        HashOperations<String, Object, Object> hashOperationMock = Mockito.mock(HashOperations.class);
        Mockito.when(redisTemplateMock.opsForHash()).thenReturn(hashOperationMock);
        Mockito.when(hashOperationMock.entries(Mockito.any())).thenReturn(mapMock);
        MainController controller = new MainController(
                new Main(new ApiKeySettingsManager(apiKeySettingsRepositoryMock), queueServiceMock, redisTemplateMock),
                new ApiKeySettingsManager(apiKeySettingsRepositoryMock),
                new StaticMessageSource()
        );
        Assertions.assertEquals(new ResponseEntity<>(new Result("Exit code 0\ntest", ""), HttpStatus.OK), controller.getResult("123"));
    }

    @Test
    void saveTmp() {
    }

    @Test
    void readTmp() {
    }

    @Test
    void getApiKeySettings() {
    }
}