package com.kafka.api.dal;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kafka.model.db.TaskEntity;

import jakarta.transaction.Transactional;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {

  Optional<TaskEntity> findTaskEntityById(UUID id);

  @Modifying
  @Transactional
  @Query("update TaskEntity t set t.startedAt = :date where t.id = :id")
  void updateStartDate(@Param("id") UUID id, @Param("date") Date date);

  @Modifying
  @Transactional
  @Query("update TaskEntity t set t.finishedAt = :date where t.id = :id")
  void updateFinishDate(@Param("id") UUID id, @Param("date") Date date);

  @Query("select new TaskEntity(t.id, t.command, t.startedAt, t.finishedAt, "
      + "new TaskResultEntity(r.id, r.stdOut, r.stdErr, r.exitCode, null), "
      + "new TaskStatusEntity(ts.id, null, ts.status, ts.changeAt)) from TaskEntity t "
      + "left join TaskResultEntity r on r.task.id = t.id " + "left join TaskStatusEntity ts on ts.task.id = t.id "
      + "where t.id = :id and ts.id = (select v.id from TaskStatusEntity v where v.task.id = t.id order by v.changeAt desc limit 1)")
  Optional<TaskEntity> getTaskById(@Param("id") UUID id);

  @Query("select new TaskEntity(t.id, t.command, t.startedAt, t.finishedAt, "
      + "new TaskResultEntity(r.id, r.stdOut, r.stdErr, r.exitCode, null), "
      + "new TaskStatusEntity(ts.id, null, ts.status, ts.changeAt)) from TaskEntity t "
      + "left join TaskResultEntity r on r.task.id = t.id " + "left join TaskStatusEntity ts on ts.task.id = t.id "
      + "where ts.id = (select v.id from TaskStatusEntity v where v.task.id = t.id order by v.changeAt desc limit 1)")
  List<TaskEntity> getAllTasks();

}
