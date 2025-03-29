package com.kafka.agent.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.kafka.model.common.Status;
import com.kafka.model.topic.KafkaTaskRequest;
import com.kafka.model.topic.KafkaTaskResult;
import com.kafka.model.topic.KafkaTaskStatus;

import lombok.extern.slf4j.Slf4j;
import com.google.common.io.CharStreams;

@Component
@Slf4j
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);
    private KafkaTemplate<UUID, Object> kafkaTemplate;

    @Autowired
    public AgentService(KafkaTemplate<UUID, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "task")
    public void executeCommand(KafkaTaskRequest taskRequest) {
        KafkaTaskStatus status = new KafkaTaskStatus(taskRequest.getTaskId(), Status.IN_PROGRESS, new Date());
        try {
            // publish status
            kafkaTemplate.send("status", status.getTaskId(), status).whenComplete((result, ex) -> {
                logResult(ex, result);
            });
            KafkaTaskResult result = executeInProcess(taskRequest.getCommand());
            if (result == null) {
                log.error("No task execution result.");
                return;
            }
            result.setTaskId(taskRequest.getTaskId());
            // publish task result
            kafkaTemplate.send("result", result.getTaskId(), result).whenComplete((taskResult, ex) -> {
                logResult(ex, taskResult);
            });
            KafkaTaskStatus finishedStatus = new KafkaTaskStatus(taskRequest.getTaskId(), Status.FINISHED, new Date());
            // publish task status
            kafkaTemplate.send("status", status.getTaskId(), finishedStatus).whenComplete((statusResult, ex) -> {
                logResult(ex, statusResult);
            });
        } catch (IOException ex) {
            log.error("Unable to push data to the queue. Error message: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error happened. Error message: {}", ex.getMessage());
        }
    }

    private KafkaTaskResult executeInProcess(String command) throws Exception {
        Process process = null;
        BufferedReader outputReader = null;
        BufferedReader errorReader = null;
        KafkaTaskResult result = new KafkaTaskResult();
        try {
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command);
            process = builder.start();
            outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            result.setExitCode(process.waitFor());
            result.setStdOut(CharStreams.toString(outputReader).replace("/n", ""));
            result.setStdErr(CharStreams.toString(errorReader));
            return result;
        } catch (Exception e) {
            log.error("Unable to execute command. Error message: {}", e.getMessage());
        } finally {
            if (outputReader != null) {
                outputReader.close();
            }
            if (errorReader != null) {
                errorReader.close();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return null;
    }

    private static void logResult(Throwable ex, SendResult<UUID, Object> sendResult) {
        if (ex == null) {
            log.info("Sent message=[ {} ] with offset=[ {} ]", sendResult.getProducerRecord(), sendResult.getRecordMetadata().offset());
        } else {
            log.error("Unable to send message=[ {} ] due to : {}", sendResult.getProducerRecord(), ex.getMessage());
        }
    }
}
