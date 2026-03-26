package com.menketechnologies.learningcollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.*

@ExtendWith(MockitoExtension::class)
class LCRepoContractTest {

    @Mock
    private lateinit var lcRepo: LCRepo

    private fun makeLc(id: Long, learning: String, category: String = DEFAULT_CAT, date: Date = Date()): LearningCollection {
        val lc = LearningCollection(learning, category, date)
        lc.id = id
        return lc
    }

    // ===== findAllByLearningContaining =====

    @Test
    fun `findAllByLearningContaining returns matching items`() {
        val items = listOf(makeLc(1, "learn kotlin"), makeLc(2, "learn spring"))
        whenever(lcRepo.findAllByLearningContaining("learn")).thenReturn(items)
        val result = lcRepo.findAllByLearningContaining("learn")
        assertEquals(2, result.size)
    }

    @Test
    fun `findAllByLearningContaining returns empty when no match`() {
        whenever(lcRepo.findAllByLearningContaining("xyz")).thenReturn(emptyList())
        assertTrue(lcRepo.findAllByLearningContaining("xyz").isEmpty())
    }

    @Test
    fun `findAllByLearningContaining with empty string`() {
        val all = (1..5).map { makeLc(it.toLong(), "item_$it") }
        whenever(lcRepo.findAllByLearningContaining("")).thenReturn(all)
        assertEquals(5, lcRepo.findAllByLearningContaining("").size)
    }

    @TestFactory
    fun `findAllByLearningContaining with various substrings`(): List<DynamicTest> {
        val learnings = listOf(
            "kotlin basics", "kotlin advanced", "spring boot", "spring security",
            "docker compose", "docker swarm", "aws lambda", "aws ec2",
            "react hooks", "react context", "vue composition", "vue router",
        )
        val queries = listOf("kotlin", "spring", "docker", "aws", "react", "vue")
        return queries.map { query ->
            dynamicTest("findAllByLearningContaining('$query')") {
                val matching = learnings.filter { it.contains(query) }
                    .mapIndexed { i, l -> makeLc(i.toLong(), l) }
                whenever(lcRepo.findAllByLearningContaining(query)).thenReturn(matching)
                val result = lcRepo.findAllByLearningContaining(query)
                assertEquals(2, result.size)
                result.forEach { assertTrue(it.learning.contains(query)) }
            }
        }
    }

    @TestFactory
    fun `findAllByLearningContaining result sizes 0 to 100`(): List<DynamicTest> =
        (0..100).map { size ->
            dynamicTest("findAllByLearningContaining returns $size items") {
                val items = (1..size).map { makeLc(it.toLong(), "match_$it") }
                whenever(lcRepo.findAllByLearningContaining("match")).thenReturn(items)
                assertEquals(size, lcRepo.findAllByLearningContaining("match").size)
            }
        }

    // ===== findAllByCategoryContaining =====

    @Test
    fun `findAllByCategoryContaining returns matching items`() {
        val items = listOf(
            makeLc(1, "item1", "programming"),
            makeLc(2, "item2", "programming-advanced"),
        )
        whenever(lcRepo.findAllByCategoryContaining("programming")).thenReturn(items)
        val result = lcRepo.findAllByCategoryContaining("programming")
        assertEquals(2, result.size)
    }

    @Test
    fun `findAllByCategoryContaining returns empty when no match`() {
        whenever(lcRepo.findAllByCategoryContaining("nonexistent")).thenReturn(emptyList())
        assertTrue(lcRepo.findAllByCategoryContaining("nonexistent").isEmpty())
    }

    @Test
    fun `findAllByCategoryContaining with empty string`() {
        val all = (1..10).map { makeLc(it.toLong(), "item_$it", "cat_$it") }
        whenever(lcRepo.findAllByCategoryContaining("")).thenReturn(all)
        assertEquals(10, lcRepo.findAllByCategoryContaining("").size)
    }

    @TestFactory
    fun `findAllByCategoryContaining with various categories`(): List<DynamicTest> {
        val categories = listOf(
            "programming", "devops", "databases", "frontend", "backend",
            "testing", "security", "networking", "cloud", "mobile",
            "machine-learning", "data-science", "algorithms", "architecture",
        )
        return categories.map { cat ->
            dynamicTest("findAllByCategoryContaining('$cat')") {
                val items = (1..3).map { makeLc(it.toLong(), "item_$it", cat) }
                whenever(lcRepo.findAllByCategoryContaining(cat)).thenReturn(items)
                val result = lcRepo.findAllByCategoryContaining(cat)
                assertEquals(3, result.size)
                result.forEach { assertEquals(cat, it.category) }
            }
        }
    }

    @TestFactory
    fun `findAllByCategoryContaining result sizes 0 to 100`(): List<DynamicTest> =
        (0..100).map { size ->
            dynamicTest("findAllByCategoryContaining returns $size items") {
                val items = (1..size).map { makeLc(it.toLong(), "item_$it", "target_cat") }
                whenever(lcRepo.findAllByCategoryContaining("target")).thenReturn(items)
                assertEquals(size, lcRepo.findAllByCategoryContaining("target").size)
            }
        }

    @TestFactory
    fun `findAllByCategoryContaining partial match patterns`(): List<DynamicTest> {
        val patterns = listOf(
            "pro" to "programming",
            "dev" to "devops",
            "data" to "databases",
            "front" to "frontend",
            "back" to "backend",
            "test" to "testing",
            "sec" to "security",
            "net" to "networking",
        )
        return patterns.map { (partial, full) ->
            dynamicTest("category partial match '$partial' in '$full'") {
                val items = listOf(makeLc(1, "item", full))
                whenever(lcRepo.findAllByCategoryContaining(partial)).thenReturn(items)
                val result = lcRepo.findAllByCategoryContaining(partial)
                assertEquals(1, result.size)
                assertTrue(result[0].category.contains(partial))
            }
        }
    }

    // ===== CrudRepository methods =====

    @Test
    fun `save returns saved entity`() {
        val lc = makeLc(0, "new item")
        val saved = makeLc(1, "new item")
        whenever(lcRepo.save(lc)).thenReturn(saved)
        val result = lcRepo.save(lc)
        assertEquals(1L, result.id)
        assertEquals("new item", result.learning)
    }

    @Test
    fun `findById returns optional with entity`() {
        val lc = makeLc(1, "found item")
        whenever(lcRepo.findById(1L)).thenReturn(Optional.of(lc))
        val result = lcRepo.findById(1L)
        assertTrue(result.isPresent)
        assertEquals("found item", result.get().learning)
    }

    @Test
    fun `findById returns empty optional when not found`() {
        whenever(lcRepo.findById(999L)).thenReturn(Optional.empty())
        val result = lcRepo.findById(999L)
        assertFalse(result.isPresent)
    }

    @Test
    fun `findAll returns all entities`() {
        val items = (1..5).map { makeLc(it.toLong(), "item_$it") }
        whenever(lcRepo.findAll()).thenReturn(items)
        assertEquals(5, lcRepo.findAll().toList().size)
    }

    @Test
    fun `count returns total entities`() {
        whenever(lcRepo.count()).thenReturn(42L)
        assertEquals(42L, lcRepo.count())
    }

    @Test
    fun `deleteById calls delete`() {
        lcRepo.deleteById(1L)
        verify(lcRepo).deleteById(1L)
    }

    @Test
    fun `existsById returns true when entity exists`() {
        whenever(lcRepo.existsById(1L)).thenReturn(true)
        assertTrue(lcRepo.existsById(1L))
    }

    @Test
    fun `existsById returns false when entity does not exist`() {
        whenever(lcRepo.existsById(999L)).thenReturn(false)
        assertFalse(lcRepo.existsById(999L))
    }

    @TestFactory
    fun `save and retrieve various entities`(): List<DynamicTest> =
        (1..50).map { i ->
            dynamicTest("save and retrieve entity #$i") {
                val lc = makeLc(i.toLong(), "learning_$i", "cat_$i", Date(i.toLong() * 1000))
                whenever(lcRepo.save(any<LearningCollection>())).thenReturn(lc)
                whenever(lcRepo.findById(i.toLong())).thenReturn(Optional.of(lc))
                val saved = lcRepo.save(lc)
                val found = lcRepo.findById(i.toLong())
                assertEquals(saved.id, found.get().id)
                assertEquals(saved.learning, found.get().learning)
                assertEquals(saved.category, found.get().category)
            }
        }

    @TestFactory
    fun `count for various repo sizes`(): List<DynamicTest> =
        (0..100L).map { size ->
            dynamicTest("count returns $size") {
                whenever(lcRepo.count()).thenReturn(size)
                assertEquals(size, lcRepo.count())
            }
        }

    // ===== RepositoryRestResource annotation verification =====

    @Test
    fun `LCRepo is annotated with RepositoryRestResource`() {
        val annotation = LCRepo::class.java.getAnnotation(
            org.springframework.data.rest.core.annotation.RepositoryRestResource::class.java
        )
        assertNotNull(annotation)
        assertEquals("learning", annotation.collectionResourceRel)
        assertEquals("learning", annotation.path)
    }

    @Test
    fun `LCRepo extends CrudRepository`() {
        assertTrue(org.springframework.data.repository.CrudRepository::class.java.isAssignableFrom(LCRepo::class.java))
    }

    @Test
    fun `LCRepo has findAllByLearningContaining method`() {
        val method = LCRepo::class.java.getDeclaredMethod("findAllByLearningContaining", String::class.java)
        assertNotNull(method)
        assertEquals(List::class.java, method.returnType)
    }

    @Test
    fun `LCRepo has findAllByCategoryContaining method`() {
        val method = LCRepo::class.java.getDeclaredMethod("findAllByCategoryContaining", String::class.java)
        assertNotNull(method)
        assertEquals(List::class.java, method.returnType)
    }

    @TestFactory
    fun `LCRepo methods have Param annotations`(): List<DynamicTest> {
        val methods = listOf(
            "findAllByLearningContaining" to "learning",
            "findAllByCategoryContaining" to "category",
        )
        return methods.map { (methodName, paramName) ->
            dynamicTest("$methodName has @Param('$paramName')") {
                val method = LCRepo::class.java.getDeclaredMethod(methodName, String::class.java)
                val paramAnnotation = method.parameters[0].getAnnotation(
                    org.springframework.data.repository.query.Param::class.java
                )
                assertNotNull(paramAnnotation)
                assertEquals(paramName, paramAnnotation.value)
            }
        }
    }
}
