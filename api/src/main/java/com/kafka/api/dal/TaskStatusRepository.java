package com.kafka.api.dal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kafka.model.db.TaskStatusEntity;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatusEntity, UUID> {

  @Query("from TaskStatusEntity ts where ts.task.id = :taskId order by ts.changeAt desc limit 1")
  Optional<TaskStatusEntity> getLatestStatusById(@Param("taskId") UUID taskId);

}
