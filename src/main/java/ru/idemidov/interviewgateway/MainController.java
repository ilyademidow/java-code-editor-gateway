package ru.idemidov.interviewgateway;

import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.idemidov.interviewgateway.model.Code;
import ru.idemidov.interviewgateway.model.Result;
import ru.idemidov.interviewgateway.service.Main;
import ru.idemidov.interviewgateway.service.QueueService;

@RestController
@RequestMapping(value = "api/v1", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
public class MainController {
    private final QueueService queueService;
    private final Main codeService;

    @PostMapping("run")
    @ApiParam(
            value = "Execute requested programming code",
            example = "class Main { public static void main(String args[]) { System.out.println(\"asd\"); } }")
    public ResponseEntity<Result> execute(@RequestBody Code code) {
        log.info(("Invoked " + code));

        if (code.getUsername().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            queueService.send(code.getCode());
            return ResponseEntity.ok(new Result(null, null));
        } catch (InterviewException e) {
            return ResponseEntity.ok(new Result(null, e.getMessage()));
        }
    }

    @GetMapping(value = "get_result/{codeHash}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    @ApiParam(value = "Returns code execution result by code MD5 hash")
    public ResponseEntity<Result> getResult(@PathVariable String codeHash) {
        return ResponseEntity.ok(codeService.getResultByCodeHash(codeHash));
    }

    @PostMapping("save_tmp")
    @ApiParam(value = "Saves current code to be able to observe it for others")
    public ResponseEntity<String> saveTmp(@RequestBody Code code) {
        log.info("save_tmp invoked");
        codeService.saveTmpCodeFile(code.getUsername(), code.getCode());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "read_tmp/{userName}")
    @ApiParam(value = "Returns current code")
    public ResponseEntity<Result> readTmp(@PathVariable String userName) {
        log.info("read_tmp invoked");
        return ResponseEntity.ok(new Result(codeService.getTmpCodeFile(userName), ""));
    }
}
