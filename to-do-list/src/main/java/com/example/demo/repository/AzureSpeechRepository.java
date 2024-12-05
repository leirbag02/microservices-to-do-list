package com.example.demo.repository;
import com.example.demo.model.AzureSpeech;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AzureSpeechRepository extends JpaRepository<AzureSpeech, Long> {
}
