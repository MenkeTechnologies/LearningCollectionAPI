package com.jakobmenke.lc;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "learning", path = "learning")
public interface LCRepo extends CrudRepository<LearningCollection, Long> {
    List<LearningCollection> findAllByLearningContaining(@Param("learning") String learning);

    List<LearningCollection> findAllByCategoryContaining(@Param("category") String category);
}
