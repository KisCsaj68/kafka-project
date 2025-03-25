package com.kafka.model.mq;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaTaskRequest {

  private UUID taskId;
  private String command;

}
