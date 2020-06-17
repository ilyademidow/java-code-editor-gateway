package ru.idemidov.interviewgateway;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.idemidov.interviewgateway.exceptions.BadRequestException;
import ru.idemidov.interviewgateway.exceptions.InternalException;
import ru.idemidov.interviewgateway.model.Code;
import ru.idemidov.interviewgateway.model.Result;
import ru.idemidov.interviewgateway.service.Main;

@RestController
@RequestMapping(value = "api/v1", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final Main codeService;

    @PostMapping(value = "run", produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Executes implementation of a Java code")
    public ResponseEntity<String> execute(@RequestBody Code code) {
        try {
            codeService.handleExecuteRequest(code);
            return ResponseEntity.ok().build();
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (InternalException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping(value = "get_result/{codeHash}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    @ApiOperation(value = "Returns code execution result by code MD5 hash")
    public ResponseEntity<Result> getResult(@PathVariable String codeHash) {
        log.info("[getResult] received {}", codeHash);
        return ResponseEntity.ok(codeService.getResultByCodeHash(codeHash));
    }

    @PostMapping("save_tmp")
    @ApiOperation(value = "Saves current code to be able to observe it for others")
    public ResponseEntity<String> saveTmp(@RequestBody Code code) {
        try {
            log.info("[saveTmp] received {}", code);
            codeService.saveTmpCodeFile(code.getUsername(), code.getCode());
            return ResponseEntity.ok().build();
        } catch (InternalException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "read_tmp/{userName}", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.ALL_VALUE)
    @ApiOperation(value = "Returns current code of requested author")
    public ResponseEntity<String> readTmp(@PathVariable String userName) {
        log.info("[readTmp] received {}", userName);
        return ResponseEntity.ok(codeService.getTmpCodeFile(userName));
    }
}
