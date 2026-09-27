package com.shaker.repository;

import com.shaker.entity.diary.DiaryEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends MongoRepository<DiaryEntry, String> {

    List<DiaryEntry> findByOwnerUsernameOrderByLoggedOnDescCreatedAtDesc(String ownerUsername);

    Optional<DiaryEntry> findByIdAndOwnerUsername(String id, String ownerUsername);
}
