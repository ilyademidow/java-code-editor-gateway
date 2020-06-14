package ru.idemidov.interviewgateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ru.idemidov.interviewgateway.model.Code;

@Service
@RequiredArgsConstructor
@Log4j2
public class QueueService {

    private final RabbitTemplate template;
    private final Queue queue;

    public void send(Code code) {
        log.info("Sent message '{}' to queue '{}'", code, queue.getName());
        template.convertAndSend(queue.getName(), code);
    }
}
