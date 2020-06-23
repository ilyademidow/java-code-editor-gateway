package ru.idemidov.interviewgateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import ru.idemidov.interviewgateway.exceptions.BadRequestException;
import ru.idemidov.interviewgateway.model.ApiKeySettings;
import ru.idemidov.interviewgateway.repository.ApiKeySettingsRepository;

import java.util.NoSuchElementException;

/**
 * Each API key has a various parameters. E.g. code length, invocation frequency, total invocation quantity.
 * This class manage this parameters. It updates it, checks limit etc
 */
@Service
@RequiredArgsConstructor
public class ApiKeySettingsManager {
    private static final String DEMO_KEY = "demo";

    // L10n message codes
    private static final String ERR_BLANK_USERNAME = "error.bad-request.blank-username";
    private static final String ERR_TOO_LONG_USERNAME = "error.bad-request.too-long-username";
    private static final String ERR_INVALID_API_KEY = "error.bad-request.invalid-api-key";

    private final ApiKeySettingsRepository apiKeySettingsRepository;

    @Value("${username.max-length}")
    private Integer maxUsernameLength = 32;

    public boolean checkProvidedData(String username, String apiKey) throws BadRequestException {
        return validateProvidedData(username, apiKey);
    }

    public ApiKeySettings getSettings(String username, String apiKey) throws BadRequestException {
        validateProvidedData(username, apiKey);
        try {
            return apiKeySettingsRepository.findById(apiKey).orElseThrow();
        } catch (NoSuchElementException e) {
            throw new BadRequestException(ERR_INVALID_API_KEY);
        }
    }

    public void handleApiKeyUsage(String username, String apiKey) {
        throw new RuntimeException("No implementation");
    }

    private boolean validateProvidedData(String username, String apiKey) throws BadRequestException {
        if (username == null || username.isBlank()) {
            throw new BadRequestException(ERR_BLANK_USERNAME);
        }
        if (username.length() > maxUsernameLength) {
            throw new BadRequestException(ERR_TOO_LONG_USERNAME);
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException(ERR_INVALID_API_KEY);
        }
        if (!DEMO_KEY.equals(apiKey)) {
            ApiKeySettings settings = apiKeySettingsRepository.findById(apiKey).orElse(null);
            if (settings != null) {
                if (!username.equals(settings.getUsername())) {
                    throw new BadRequestException(ERR_INVALID_API_KEY);
                }
            } else {
                throw new BadRequestException(ERR_INVALID_API_KEY);
            }
        }

        return true;
    }
}
