package com.kafka.model.mq;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import com.kafka.model.common.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaTaskStatus implements Serializable {

  private UUID taskId;
  private Status status;
  private Date changedAt;

}
