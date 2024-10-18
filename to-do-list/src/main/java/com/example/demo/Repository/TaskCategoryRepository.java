package com.example.demo.Repository;

import com.example.demo.model.TaskCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Long> {

    @Query("select c from TaskCategory c where c.id =:id ")
    public TaskCategory findByID(@Param("id") Long id);

    Optional<TaskCategory> findById(Long id);


}
