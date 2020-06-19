package ru.idemidov.interviewgateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScans;
import ru.idemidov.interviewgateway.model.ApiKeySettings;
import ru.idemidov.interviewgateway.repository.ApiKeySettingsRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;

@SpringBootApplication
public class InterviewGatewayApplication {
	//TODO just for test
	@Autowired
	private ApiKeySettingsRepository repository;

	@PostConstruct
	public void initApiKeySettings() {
		repository.save(new ApiKeySettings("0110", "donald_knutt", 2L, 30L, 5, LocalDateTime.now().minusHours(1)));
	}

	public static void main(String[] args) {
		SpringApplication.run(InterviewGatewayApplication.class, args);
	}

}
