package ru.idemidov.interviewgateway.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("${rabbitmq.queue.code}")
    private String codeSentQueueName;

    @Bean
    public Queue initResultQueue() {
        return new Queue(codeSentQueueName);
    }
}
