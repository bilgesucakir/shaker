package com.shaker.repository;

import com.shaker.entity.guideline.Guideline;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface GuidelineRepository extends MongoRepository<Guideline, String> {

    List<Guideline> findAllByOrderBySortOrderAscTitleAsc();

    Optional<Guideline> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
