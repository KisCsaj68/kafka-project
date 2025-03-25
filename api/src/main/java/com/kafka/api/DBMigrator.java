package com.kafka.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.kafka.model")
public class DBMigrator {
  public static void main(String[] args) {
    SpringApplication.run(DBMigrator.class, args);
    System.out.println("db migration complete");
    System.exit(0);
  }
}
