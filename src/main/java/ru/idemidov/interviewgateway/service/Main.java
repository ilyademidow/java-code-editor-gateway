package ru.idemidov.interviewgateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.idemidov.interviewgateway.exceptions.BadRequestException;
import ru.idemidov.interviewgateway.exceptions.InternalException;
import ru.idemidov.interviewgateway.model.Code;
import ru.idemidov.interviewgateway.model.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class Main {
    private static final String TMP_CODE_FILE_NAME = "java_code_tmp.txt";
    private static final String TMP_CODE_PATH = "interview";

    // L10n message codes
    private static final String ERROR_SHORTEN_YOUR_CODE = "error.bad-request.shorten-your-code";
    private static final String ERROR_SHORTEN_USERNAME = "error.bad-request.shorten-username";
    private static final String ERROR_TRY_AGAIN = "error.internal.try-again";
    private static final String ERROR_TRY_AGAIN_LATER = "error.internal.try-again-later";
    private static final String ERROR_WE_ARE_FIXING = "error.internal.we-are-fixing";

    private final QueueService queueService;
    private final MessageSource messageSource;

    @Value("${redis.url}")
    private String redisUrl;
    @Value("${redis.map}")
    private String redisMapName;

    @Value("${code.max-length}")
    private Integer maxCodeLength;

    /**
     * Handles code execution request. If everything is fine it sends to the queue, otherwise throw an exception
     *
     * @param code Object contains username and code text
     * @return OK
     * @throws BadRequestException Text message if sent data doesn't fit all criteria
     */
    public ResponseEntity<String> handleExecuteRequest(Code code) throws BadRequestException {
        int codeLength = code.getCode().length();
        if (codeLength > maxCodeLength) {
            throw new BadRequestException(
                    messageSource.getMessage(ERROR_SHORTEN_YOUR_CODE, new Object[]{maxCodeLength, codeLength}, LocaleContextHolder.getLocale())
            );
        }
        log.info("[execute] received {}", code);

        if (code.getUsername().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        queueService.send(code);

        return ResponseEntity.ok("Accepted");
    }

    /**
     * Read temporary code (can be use for an interviewer)
     *
     * @param username Name of a user who's code is required
     * @param rawCode  Code is been typing by candidate
     */
    public void saveTmpCodeFile(final String username, final String rawCode) {
        try {
            final Path filePath = Paths.get(TMP_CODE_PATH, username);
            if (!Files.exists(filePath)) {
                try {
                    Files.createDirectories(filePath);
                } catch (IOException ioException) {
                    throw new InternalException(ERROR_TRY_AGAIN_LATER, ioException);
                }
            }
            Files.write(Paths.get(filePath.toString(), TMP_CODE_FILE_NAME), rawCode.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new InternalException(ERROR_TRY_AGAIN_LATER, e);
        }
    }

    /**
     * Read temporary code (can be use for an interviewer)
     *
     * @param username Name of a user who's code is required
     * @return Code as text
     */
    public String getTmpCodeFile(final String username) {
        final Path filePath = Paths.get(TMP_CODE_PATH, username);
        byte[] b = new byte[1];
        try {
            b = Files.readAllBytes(Paths.get(filePath.toString(), TMP_CODE_FILE_NAME));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return new String(b);
    }

    /**
     * Returns code execution result by username MD5 hash
     *
     * @param userNameMD5Hash MD5 hash of username
     * @return code execution result
     */
    public Result getResultByCodeHash(String userNameMD5Hash) {
        Config config = new Config();
        try {
            config.useSingleServer().setAddress(redisUrl);
            RedissonClient redisson = Redisson.create(config);
            RMap<String, String> map = redisson.getMap(redisMapName);
            String mapValue = map.get(userNameMD5Hash);
            log.info("Value from map {} is {} by key {}", redisMapName, mapValue, userNameMD5Hash);
            redisson.shutdown();
            if (mapValue != null) {
                if (mapValue.contains("Exit code 0")) {
                    return new Result(mapValue, "");
                } else {
                    return new Result("", mapValue);
                }
            } else {
                throw new InternalException(messageSource.getMessage(ERROR_TRY_AGAIN, null, LocaleContextHolder.getLocale()), null);
            }
        } catch (Exception e) {
            throw new InternalException(messageSource.getMessage(ERROR_TRY_AGAIN, null, LocaleContextHolder.getLocale()), null);
        }
    }
}

