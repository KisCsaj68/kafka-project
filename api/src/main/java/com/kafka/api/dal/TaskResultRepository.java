package com.kafka.api.dal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kafka.model.db.TaskResultEntity;

public interface TaskResultRepository extends JpaRepository<TaskResultEntity, UUID> {

  @Query("from TaskResultEntity t where t.task.Id = :taskId")
  public Optional<TaskResultEntity> findByTaskId(@Param("taskId") UUID taskId);

}
