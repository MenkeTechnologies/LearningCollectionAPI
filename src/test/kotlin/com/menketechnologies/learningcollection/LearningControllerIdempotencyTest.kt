package com.menketechnologies.learningcollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.*

@ExtendWith(MockitoExtension::class)
class LearningControllerIdempotencyTest {

    @Mock
    private lateinit var lcRepo: LCRepo

    @InjectMocks
    private lateinit var controller: LearningController

    private fun makeLc(id: Long, learning: String, category: String = DEFAULT_CAT, date: Date = Date()): LearningCollection {
        val lc = LearningCollection(learning, category, date)
        lc.id = id
        return lc
    }

    // ===== RECENTS IDEMPOTENCY =====

    @TestFactory
    fun `recents returns same result on repeated calls with same data`(): List<DynamicTest> =
        (1..50).map { i ->
            dynamicTest("recents idempotency iteration $i") {
                val items = (1..10).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val first = controller.learningItemRecentShortDefault()
                val second = controller.learningItemRecentShortDefault()
                assertEquals(first, second)
            }
        }

    @Test
    fun `recents reversal is stable across calls`() {
        val items = (1..5).map { makeLc(it.toLong(), "item_$it") }
        whenever(lcRepo.findAll()).thenReturn(items)
        val results = (1..20).map { controller.learningItemRecentShortDefault() }
        results.forEach { assertEquals(results[0], it) }
    }

    @TestFactory
    fun `recents-count returns consistent results`(): List<DynamicTest> =
        (1..30).map { count ->
            dynamicTest("recents/$count is consistent across calls") {
                val items = (1..50).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val first = controller.getLearningItemRecentShort(count)
                val second = controller.getLearningItemRecentShort(count)
                assertEquals(first, second)
            }
        }

    @TestFactory
    fun `recent-count returns consistent entity results`(): List<DynamicTest> =
        (1..30).map { count ->
            dynamicTest("recent/$count entities are consistent") {
                val items = (1..50).map { makeLc(it.toLong(), "item_$it", "cat_$it", Date(it.toLong() * 1000)) }
                whenever(lcRepo.findAll()).thenReturn(items)
                val first = controller.getLearningItemRecent(count)
                val second = controller.getLearningItemRecent(count)
                assertEquals(first.size, second.size)
                first.zip(second).forEach { (a, b) ->
                    assertEquals(a.id, b.id)
                    assertEquals(a.learning, b.learning)
                }
            }
        }

    // ===== FILTER IDEMPOTENCY =====

    @TestFactory
    fun `filter returns same result on repeated calls`(): List<DynamicTest> {
        val queries = listOf("kotlin", "spring", "docker", "test", "learn", "api", "rest", "data")
        return queries.map { query ->
            dynamicTest("filter('$query') is idempotent") {
                val items = listOf(makeLc(1, "$query basics"), makeLc(2, "$query advanced"))
                whenever(lcRepo.findAllByLearningContaining(query)).thenReturn(items)
                val first = controller.filterLearn(query)
                val second = controller.filterLearn(query)
                assertEquals(first, second)
            }
        }
    }

    @TestFactory
    fun `filter result order is stable`(): List<DynamicTest> =
        (1..50).map { size ->
            dynamicTest("filter order stable for $size items") {
                val items = (1..size).map { makeLc(it.toLong(), "match_$it") }
                whenever(lcRepo.findAllByLearningContaining("match")).thenReturn(items)
                val results = (1..5).map { controller.filterLearn("match") }
                results.forEach { assertEquals(results[0], it) }
            }
        }

    // ===== RANDOM ENDPOINTS BOUNDS =====

    @TestFactory
    fun `random count never exceeds repo size`(): List<DynamicTest> =
        (1..50).map { repoSize ->
            dynamicTest("random count bounded by repo size $repoSize") {
                val items = (1..repoSize).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCount(repoSize + 100)
                assertTrue(result.size <= repoSize)
            }
        }

    @TestFactory
    fun `randoms count never exceeds repo size`(): List<DynamicTest> =
        (1..50).map { repoSize ->
            dynamicTest("randoms count bounded by repo size $repoSize") {
                val items = (1..repoSize).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCountShort(repoSize + 100)
                assertTrue(result.size <= repoSize)
            }
        }

    @TestFactory
    fun `random always returns subset of repo items`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("random returns repo subset iteration $i") {
                val items = (1..20).map { makeLc(it.toLong(), "item_$it") }
                val allIds = items.map { it.id }.toSet()
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCount(10)
                result.forEach { assertTrue(it.id in allIds) }
            }
        }

    @TestFactory
    fun `randoms always returns subset of repo learning strings`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("randoms returns repo strings iteration $i") {
                val items = (1..20).map { makeLc(it.toLong(), "item_$it") }
                val allLearnings = items.map { it.learning }.toSet()
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCountShort(10)
                result.forEach { assertTrue(it in allLearnings) }
            }
        }

    // ===== ENDPOINT TYPE CONTRACTS =====

    @TestFactory
    fun `recents endpoints return strings while recent returns entities`(): List<DynamicTest> =
        (1..30).map { count ->
            dynamicTest("type contract for count=$count") {
                val items = (1..50).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val recentsResult = controller.getLearningItemRecentShort(count)
                val recentResult = controller.getLearningItemRecent(count)
                recentsResult.forEach { assertTrue(it is String) }
                recentResult.forEach { assertTrue(it is LearningCollection) }
                assertEquals(recentsResult.size, recentResult.size)
            }
        }

    @TestFactory
    fun `randoms endpoints return strings while random returns entities`(): List<DynamicTest> =
        (1..30).map { count ->
            dynamicTest("type contract randoms vs random for count=$count") {
                val items = (1..50).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val randomsResult = controller.getLearningItemCountShort(count)
                val randomResult = controller.getLearningItemCount(count)
                randomsResult.forEach { assertTrue(it is String) }
                randomResult.forEach { assertTrue(it is LearningCollection) }
                assertEquals(randomsResult.size, randomResult.size)
            }
        }

    // ===== ADD CONSISTENCY =====

    @TestFactory
    fun `add always sets DEFAULT_CAT category`(): List<DynamicTest> {
        val inputs = (1..100).map { "learning_$it" }
        return inputs.mapIndexed { i, input ->
            dynamicTest("add('$input') uses DEFAULT_CAT") {
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val result = controller.add(input)
                assertEquals(DEFAULT_CAT, result.category)
                assertEquals(input, result.learning)
            }
        }
    }

    @TestFactory
    fun `add date is always recent`(): List<DynamicTest> =
        (1..50).map { i ->
            dynamicTest("add date is recent iteration $i") {
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val before = Date().time
                val result = controller.add("test_$i")
                val after = Date().time
                assertTrue(result.dateAdded.time in before..after)
            }
        }

    // ===== RECENTS vs RECENT CONSISTENCY =====

    @TestFactory
    fun `recents and recent return same items in same order`(): List<DynamicTest> =
        (1..30).map { count ->
            dynamicTest("recents/$count and recent/$count same order") {
                val items = (1..50).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val recentsResult = controller.getLearningItemRecentShort(count)
                val recentResult = controller.getLearningItemRecent(count)
                assertEquals(recentsResult.size, recentResult.size)
                recentsResult.zip(recentResult).forEach { (s, e) ->
                    assertEquals(s, e.learning)
                }
            }
        }

    @Test
    fun `recents default returns same as recents with SHORT_CNT`() {
        val items = (1..50).map { makeLc(it.toLong(), "item_$it") }
        whenever(lcRepo.findAll()).thenReturn(items)
        val defaultResult = controller.learningItemRecentShortDefault()
        val explicitResult = controller.getLearningItemRecentShort(SHORT_CNT)
        assertEquals(defaultResult, explicitResult)
    }

    @TestFactory
    fun `recents default equals recents SHORT_CNT for various repo sizes`(): List<DynamicTest> =
        (0..60).map { size ->
            dynamicTest("recents default == recents/$SHORT_CNT for repo size $size") {
                val items = (1..size).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val defaultResult = controller.learningItemRecentShortDefault()
                val explicitResult = controller.getLearningItemRecentShort(SHORT_CNT)
                assertEquals(defaultResult, explicitResult)
            }
        }

    // ===== RANDOM NO DUPLICATES =====

    @TestFactory
    fun `random count returns no duplicate entities`(): List<DynamicTest> =
        (1..50).map { i ->
            dynamicTest("random no duplicates iteration $i") {
                val items = (1..20).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCount(10)
                val ids = result.map { it.id }
                assertEquals(ids.size, ids.toSet().size)
            }
        }

    @TestFactory
    fun `randoms count returns no duplicate strings`(): List<DynamicTest> =
        (1..50).map { i ->
            dynamicTest("randoms no duplicates iteration $i") {
                val items = (1..20).map { makeLc(it.toLong(), "unique_item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCountShort(10)
                assertEquals(result.size, result.toSet().size)
            }
        }
}
