package ru.idemidov.interviewgateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Value;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class Main {
    private static final String TMP_CODE_FILE_NAME = "java_code_tmp.txt";
    private static final String TMP_CODE_PATH = "interview";

    // L10n message codes
    private static final String ERR_TOO_LONG_CODE = "error.bad-request.too-long-code";
    private static final String ERR_BLANK_USERNAME = "error.bad-request.blank-username";
    private static final String ERR_TOO_LONG_USERNAME = "error.bad-request.too-long-username";
    private static final String ERR_NO_STORED_CODE_FOR_THIS_USER = "error.bad-request.no-stored-code-for-this-user";
    private static final String ERR_NO_RESULT_FOR_THIS_KEY = "error.internal.no-result-for-this-key";
    private static final String ERR_SERVICE_UNAVAILABLE = "error.internal.service-unavailable";

    private final QueueService queueService;

    @Value("${redis.url}")
    private String redisUrl;
    @Value("${redis.map}")
    private String redisMapName;

    @Value("${code.max-length}")
    private Integer maxCodeLength;
    @Value("${username.max-length}")
    private Integer maxUsernameLength;

    /**
     * Handles code execution request. If everything is fine it sends to the queue, otherwise throw an exception
     *
     * @param code Object contains username and code text
     * @throws BadRequestException Text message if sent data doesn't fit all criteria
     * @throws InternalException Text message if AMQP service is unavailable
     */
    public void handleExecuteRequest(Code code) throws BadRequestException, InternalException {
        StringBuilder errorStack = new StringBuilder();
        int codeLength = code.getCode().length();
        if (!code.getUsername().isBlank()) {
            if (code.getUsername().length() > maxUsernameLength) {
                errorStack.append(ERR_TOO_LONG_USERNAME).append(";");
            }
        } else {
            errorStack.append(ERR_BLANK_USERNAME).append(";");
        }
        if (codeLength > maxCodeLength) {
            errorStack.append(ERR_TOO_LONG_CODE).append(";");
        }
        if (errorStack.length() > 0) {
            throw new BadRequestException(errorStack.toString());
        }
        try {
            queueService.send(code);
        } catch (AmqpException e) {
            throw new InternalException(ERR_SERVICE_UNAVAILABLE, e);
        }
    }

    /**
     * Read temporary code (can be use for an interviewer)
     *
     * @param username Name of a user who's code is required
     * @param rawCode  Code is been typing by candidate
     * @throws InternalException Text message if it's can't to create a file
     */
    public void saveTmpCodeFile(final String username, final String rawCode) throws InternalException {
        try {
            final Path filePath = Paths.get(TMP_CODE_PATH, username);
            if (!Files.exists(filePath)) {
                try {
                    Files.createDirectories(filePath);
                } catch (IOException ioException) {
                    throw new InternalException(ERR_SERVICE_UNAVAILABLE, ioException);
                }
            }
            Files.write(Paths.get(filePath.toString(), TMP_CODE_FILE_NAME), rawCode.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new InternalException(ERR_SERVICE_UNAVAILABLE, e);
        }
    }

    /**
     * Read temporary code (can be use for an interviewer)
     *
     * @param username Name of a user who's code is required
     * @return Code as text
     * @throws BadRequestException Text message if no stored code for provided username
     * @throws InternalException Text message if file doesn't exist
     */
    public Result getTmpCodeFile(final String username) throws BadRequestException, InternalException {
        final Path filePath = Paths.get(TMP_CODE_PATH, username);
        if (!Files.exists(filePath)) {
            throw new BadRequestException(ERR_NO_STORED_CODE_FOR_THIS_USER);
        }
        try {
            return new Result(new String(Files.readAllBytes(Paths.get(filePath.toString(), TMP_CODE_FILE_NAME))), "");
        } catch (IOException e) {
            throw new InternalException(ERR_SERVICE_UNAVAILABLE, e);
        }
    }

    /**
     * Returns code execution result by username MD5 hash
     *
     * @param userNameMD5Hash MD5 hash of username
     * @return Code execution result
     * @throws BadRequestException Text message if no stored result for provided username
     * @throws InternalException Text message if storage is unavailable
     */
    public Result getResultByCodeHash(String userNameMD5Hash) throws BadRequestException, InternalException {
        Config config = new Config();
        try {
            config.useSingleServer().setAddress(redisUrl);
            RedissonClient redisson = Redisson.create(config);
            RMap<String, String> map = redisson.getMap(redisMapName);
            if (!map.containsKey(userNameMD5Hash)) {
                throw new BadRequestException(ERR_NO_STORED_CODE_FOR_THIS_USER);
            }
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
                throw new InternalException(ERR_NO_RESULT_FOR_THIS_KEY, null);
            }
        } catch (Exception e) {
            throw new InternalException(ERR_SERVICE_UNAVAILABLE, e);
        }
    }
}

