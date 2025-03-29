package com.kafka.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

  @Bean
  public NewTopic statusTopic() {
    return TopicBuilder.name("status").partitions(3).build();
  }

  @Bean
  public NewTopic resultTopic() {
    return TopicBuilder.name("result").partitions(3).build();
  }

  @Bean
  public NewTopic taskTopic() {
    return TopicBuilder.name("task").partitions(3).build();
  }

}
