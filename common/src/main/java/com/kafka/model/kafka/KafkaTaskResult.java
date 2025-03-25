package com.kafka.model.mq;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaTaskResult implements Serializable {

  private UUID taskId;
  private String stdOut;
  private String stdErr;
  private Integer exitCode;

}
