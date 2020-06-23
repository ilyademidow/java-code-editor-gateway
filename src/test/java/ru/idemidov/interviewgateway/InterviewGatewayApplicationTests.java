package ru.idemidov.interviewgateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {TestConfiguration.class, RedisProperties.class})
class InterviewGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
