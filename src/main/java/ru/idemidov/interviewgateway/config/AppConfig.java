package ru.idemidov.interviewgateway.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class AppConfig {
    @Value("${rabbitmq.queue.code}")
    private String codeSentQueueName;
    @Value("${redis.host}")
    private String redisUrl;
    @Value("${redis.port}")
    private Integer redisPort;

    @Bean
    public Queue initResultQueue() {
        return new Queue(codeSentQueueName);
    }

    @Bean
    public MessageConverter initMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate initRabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplateWithMessageConverter = new RabbitTemplate(connectionFactory);
        rabbitTemplateWithMessageConverter.setMessageConverter(messageConverter);

        return rabbitTemplateWithMessageConverter;
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConFactory = new JedisConnectionFactory();
        jedisConFactory.getStandaloneConfiguration().setHostName(redisUrl);
        jedisConFactory.getStandaloneConfiguration().setPort(redisPort);
        return jedisConFactory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
}
