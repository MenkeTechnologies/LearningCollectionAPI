package com.menketechnologies.learningcollection;

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(collectionResourceRel = "learning", path = "learning")
interface LCRepo : CrudRepository<LearningCollection, Long> {
    fun findAllByLearningContaining(@Param("learning") learning: String): List<LearningCollection>
    fun findAllByCategoryContaining(@Param("category") category: String): List<LearningCollection>
}
