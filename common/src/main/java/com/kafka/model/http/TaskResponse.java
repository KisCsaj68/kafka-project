package com.kafka.model.http;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import com.kafka.model.db.TaskEntity;
import com.kafka.model.db.TaskResultEntity;
import com.kafka.model.db.TaskStatusEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TaskResponse {

  @EqualsAndHashCode.Include
  private UUID id;

  private String command;
  private String startedAt;
  private String finishedAt;
  private String status;
  private String stdOut;
  private String stdErr;
  private Integer exitCode;

  public static TaskResponse from(TaskEntity task, Optional<TaskStatusEntity> statusOptional,
      Optional<TaskResultEntity> taskResultEntityOptional) {
    return new TaskResponse(
        task.getId(),
        task.getCommand(),
        dateFormatter(task.getStartedAt()),
        dateFormatter(task.getFinishedAt()),
        statusOptional.orElseGet(() -> new TaskStatusEntity()).getStatus().name(),
        taskResultEntityOptional.orElseGet(() -> new TaskResultEntity()).getStdOut(),
        taskResultEntityOptional.orElseGet(() -> new TaskResultEntity()).getStdErr(),
        taskResultEntityOptional.orElseGet(() -> new TaskResultEntity()).getExitCode());
  }

  private static String dateFormatter(Date dateTime) {
    if (dateTime == null) {
      return null;
    }
    String pattern = "yyyy-MM-dd hh:mm";
    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
    return dateFormat.format(dateTime);
  }
}
