package com.example.demo.repository;

import com.example.demo.model.TaskState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskStateRepository extends JpaRepository<TaskState, Long> {

    @Query("SELECT TS FROM TaskState TS WHERE TS.taskStatus =:status")
    public TaskState findByStatus(@Param("status") String status);

    @Query("SELECT t FROM TaskState t WHERE t.id = :stateId")
    public TaskState findByID(@Param("stateId") Long stateId);
}
