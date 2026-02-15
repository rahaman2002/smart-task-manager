package org.example.smarttaskmanager.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue taskQueue() {
        return new Queue("task_notifications", false);
    }
}
