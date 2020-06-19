package ru.idemidov.interviewgateway;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.idemidov.interviewgateway.exceptions.BadRequestException;
import ru.idemidov.interviewgateway.exceptions.InternalException;
import ru.idemidov.interviewgateway.model.Code;
import ru.idemidov.interviewgateway.model.Result;
import ru.idemidov.interviewgateway.service.Main;

import java.util.Arrays;

@RestController
@RequestMapping(value = "api/v1", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final Main codeService;
    private final MessageSource messageSource;

    @Value("${code.max-length}")
    private Integer maxCodeLength;
    @Value("${username.max-length}")
    private Integer maxUsernameLength;

    @PostMapping(value = "run")
    @ApiOperation(value = "Executes implementation of a Java code")
    public ResponseEntity<Result> execute(@RequestBody Code code) {
        log.info("[execute] received {}", code);
        try {
            codeService.handleExecuteRequest(code);
            return ResponseEntity.ok(new Result("accepted", ""));
        } catch (BadRequestException e) {
            log.warn(e.getMessage());
            StringBuilder msgStack = new StringBuilder();
            Arrays.stream(e.getMessage().split(";")).forEach(
                    message ->
                            msgStack.append(messageSource.getMessage(message, new Object[] {maxCodeLength, maxUsernameLength}, LocaleContextHolder.getLocale())).append("; ")
            );
            return ResponseEntity.badRequest().body(new Result("", msgStack.toString()));
        } catch (InternalException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(new Result("", messageSource.getMessage(e.getMessage(), null, LocaleContextHolder.getLocale())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "get_result/{codeHash}", consumes = MediaType.ALL_VALUE)
    @ApiOperation(value = "Returns code execution result by code MD5 hash")
    public ResponseEntity<Result> getResult(@PathVariable String codeHash) {
        log.info("[getResult] received {}", codeHash);
        try {
            return ResponseEntity.ok(codeService.getResultByCodeHash(codeHash));
        } catch (BadRequestException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(new Result("", messageSource.getMessage(e.getMessage(), null, LocaleContextHolder.getLocale())));
        } catch (InternalException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(new Result("", messageSource.getMessage(e.getMessage(), null, LocaleContextHolder.getLocale())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("save_tmp")
    @ApiOperation(value = "Saves current code to be able to observe it for others")
    public ResponseEntity<Result> saveTmp(@RequestBody Code code) {
        log.info("[saveTmp] received {}", code);
        try {
            codeService.saveTmpCodeFile(code);
            return ResponseEntity.ok().build();
        } catch (BadRequestException e) {
            log.warn(e.getMessage());
            StringBuilder msgStack = new StringBuilder();
            Arrays.stream(e.getMessage().split(";")).forEach(
                    message ->
                            msgStack.append(messageSource.getMessage(message, new Object[] {maxCodeLength, maxUsernameLength}, LocaleContextHolder.getLocale())).append("; ")
            );
            return ResponseEntity.badRequest().body(new Result("", msgStack.toString()));
        } catch (InternalException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(new Result("", messageSource.getMessage(e.getMessage(), null, LocaleContextHolder.getLocale())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "read_tmp/{userName}", consumes = MediaType.ALL_VALUE)
    @ApiOperation(value = "Returns current code of requested author")
    public ResponseEntity<Result> readTmp(@PathVariable String userName) {
        log.info("[readTmp] received {}", userName);
        try {
            return ResponseEntity.ok(codeService.getTmpCodeFile(userName));
        } catch (BadRequestException e) {
            log.warn(e.getMessage());
            return ResponseEntity.badRequest().body(new Result("", messageSource.getMessage(e.getMessage(), null, LocaleContextHolder.getLocale())));
        } catch (InternalException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(new Result("", messageSource.getMessage(e.getMessage(), null, LocaleContextHolder.getLocale())), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
