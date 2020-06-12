package ru.idemidov.interviewgateway.service;

import lombok.extern.log4j.Log4j2;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.idemidov.interviewgateway.InterviewException;
import ru.idemidov.interviewgateway.model.Result;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Log4j2
@Service
public class Main {
    private static final String TMP_CODE_FILE_NAME = "java_code_tmp.txt";
    private static final String TMP_CODE_PATH = "interview/";

    @Value("${redis.url}")
    private String redisUrl;
    @Value("${redis.map}")
    private String redisMapName;

    /**
     * Read temporary code (can be use for an interviewer)
     * @param username Name of a user who's code is required
     * @param rawCode Code is been typing by candidate
     */
    public void saveTmpCodeFile(final String username, final String rawCode) {
        try {
            File dir = new File(TMP_CODE_PATH, username);
            final String filePath = TMP_CODE_PATH + username;
            if (!Files.exists(Paths.get(filePath))) {
                if(!dir.mkdir()) {
                    log.error("Unable to create dir " + TMP_CODE_PATH + filePath);
                    throw new InterviewException("Sorry... Try again later!");
                }
            }
            Files.write(Paths.get(filePath, TMP_CODE_FILE_NAME), rawCode.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read temporary code (can be use for an interviewer)
     * @param username Name of a user who's code is required
     * @return Code as text
     */
    public String getTmpCodeFile(final String username) {
        final String filePath = TMP_CODE_PATH + username;
        byte[] b = new byte[1];
        try {
            b = Files.readAllBytes(Paths.get(filePath, TMP_CODE_FILE_NAME));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return new String(b);
    }

    /**
     * Returns code execution result by code MD5 hash
     * @param codeHash MD5 code hash
     * @return code execution result
     */
    public Result getResultByCodeHash(String codeHash) {
        Config config = new Config();
        config.useSingleServer().setAddress(redisUrl);
        RedissonClient redisson = Redisson.create(config);
        RMap<String, String> map = redisson.getMap(redisMapName);
        String mapValue = map.get(codeHash);
        log.info("stored map value: " + mapValue);
        redisson.shutdown();
        if (mapValue.contains("Exit code 0")) {
            return new Result(mapValue,"");
        } else {
            return new Result("", mapValue);
        }
    }
}
