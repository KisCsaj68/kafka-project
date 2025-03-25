package com.kafka.model.db;

import java.io.Serializable;
import java.util.UUID;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "task_result")
public class TaskResultEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid", name = "result_id")
  private UUID id;

  @Column(columnDefinition = "TEXT")
  private String stdOut;

  @Column(columnDefinition = "TEXT")
  private String stdErr;

  @Nullable
  private Integer exitCode;

  @OneToOne()
  @JoinColumn(name = "task_id", nullable = false)
  private TaskEntity task;
}
