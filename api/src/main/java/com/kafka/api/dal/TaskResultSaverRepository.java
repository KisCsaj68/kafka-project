package com.kafka.api.dal;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.kafka.model.db.TaskResultEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
public class TaskResultSaverRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Transactional
  public void saveResult(TaskResultEntity taskResult, UUID taskId) {
    entityManager
        .createNativeQuery("INSERT INTO task_result (result_id, exit_code, std_err, std_out, task_id) VALUES (?, ?, ?, ?, ?)")
        .setParameter(1, UUID.randomUUID())
        .setParameter(2, taskResult.getExitCode())
        .setParameter(3, taskResult.getStdErr())
        .setParameter(4, taskResult.getStdOut())
        .setParameter(5, taskId)
        .executeUpdate();
  }

}
