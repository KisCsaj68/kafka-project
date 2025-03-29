package com.kafka.api.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.kafka.model.topic.KafkaTaskRequest;
import com.kafka.model.topic.KafkaTaskResult;
import com.kafka.model.topic.KafkaTaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.kafka.api.dal.TaskRepository;
import com.kafka.api.dal.TaskResultSaverRepository;
import com.kafka.api.dal.TaskStatusRepository;
import com.kafka.model.common.Status;
import com.kafka.model.db.TaskEntity;
import com.kafka.model.db.TaskResultEntity;
import com.kafka.model.db.TaskStatusEntity;
import com.kafka.model.http.TaskResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private TaskRepository taskRepository;
    private TaskStatusRepository taskStatusRepository;
    private TaskResultSaverRepository taskResultSaverRepository;
    private KafkaTemplate<UUID, Object> kafkaTemplate;

    @Autowired
    public TaskService(TaskRepository taskRepository,
                       TaskStatusRepository taskStatusRepository,
                       TaskResultSaverRepository taskResultSaverRepository,
                       KafkaTemplate<UUID, Object> kafkaTemplate
    ) {
        this.taskRepository = taskRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.taskResultSaverRepository = taskResultSaverRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public TaskResponse saveTaskEntity(String command) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setCommand(command);
        TaskStatusEntity taskStatusEntity = new TaskStatusEntity();
        taskStatusEntity.setStatus(Status.QUEUED);
        taskStatusEntity.setChangeAt(new Date());
        try {
            taskRepository.saveAndFlush(taskEntity);
            taskStatusEntity.setTask(taskEntity);
            taskStatusRepository.saveAndFlush(taskStatusEntity);
            kafkaTemplate.send("task", taskEntity.getId(), new KafkaTaskRequest(taskEntity.getId(), taskEntity.getCommand())).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Sent message=[ {} ] with offset=[ {} ]", taskEntity, result.getRecordMetadata().offset());
                } else {
                    log.error("Unable to send message=[ {} ] due to : {}", taskEntity, ex.getMessage());
                }
            });
            TaskResultEntity taskResultEntity = new TaskResultEntity();
            TaskResponse response = TaskResponse.from(taskEntity, Optional.of(taskStatusEntity),
                    Optional.of(taskResultEntity));
            return response;
        } catch (Exception ex) {
            log.error("Unexpected error happened. Error message: {}", ex.getMessage());
        }
        return null;
    }

    public List<TaskResponse> getAllTasks() {
        try {
            List<TaskResponse> res = new ArrayList<>();
            for (TaskEntity task : taskRepository.getAllTasks()) {
                res.add(TaskResponse.from(task, Optional.ofNullable(task.getStatus()), Optional.ofNullable(task.getResult())));
            }
            return res;
        } catch (Exception ex) {
            log.error("Unexpected error happened. Error message: {}", ex.getMessage());
        }
        return null;
    }

    public TaskResponse getTaskById(UUID id) {
        try {
            Optional<TaskEntity> taskOptional = taskRepository.getTaskById(id);
            if (taskOptional.isEmpty()) {
                log.error("No db entry for id: {}", id);
                return new TaskResponse();
            }
            TaskEntity task = taskOptional.get();
            return TaskResponse.from(task, Optional.ofNullable(task.getStatus()), Optional.ofNullable(task.getResult()));
        } catch (Exception ex) {
            log.error("Unexpected error happened. Error message: {}", ex.getMessage());
        }
        return null;
    }

    @KafkaListener(topics = "result")
    private void saveTaskResult(KafkaTaskResult taskResult) {
        log.info("Task Result received from: {}", "task result topic");
        try {
            TaskResultEntity result = new TaskResultEntity(null, taskResult.getStdOut(), taskResult.getStdErr(),
                    taskResult.getExitCode(), null);
            taskResultSaverRepository.saveResult(result, taskResult.getTaskId());
        } catch (Exception ex) {
            log.error("Not able to process the result. Error message: {}", ex.getMessage());
        }
    }

    @KafkaListener(topics = "status")
    private void statusUpdate(KafkaTaskStatus status) {
        log.info("Status received from: {}", "task status topic");
        try {
            TaskEntity task = new TaskEntity();
            task.setId(status.getTaskId());
            TaskStatusEntity statusEntity = new TaskStatusEntity(null, task, status.getStatus(), status.getChangedAt());
            taskStatusRepository.saveAndFlush(statusEntity);
            log.debug("Status saved to db with id: {}", statusEntity.getId());
            switch (status.getStatus()) {
                case IN_PROGRESS:
                    taskRepository.updateStartDate(status.getTaskId(), status.getChangedAt());
                    log.debug("Task: {} updated with startAt.", status.getTaskId());
                    break;
                case FINISHED:
                    taskRepository.updateFinishDate(status.getTaskId(), status.getChangedAt());
                    log.debug("Task: {} updated with finishedAt.", status.getTaskId());
                    break;
                case QUEUED:
                    break;
            }
        } catch (Exception ex) {
            log.error("Not able to proccess the status. Error message: {}", ex.getMessage());
        }
    }
}
