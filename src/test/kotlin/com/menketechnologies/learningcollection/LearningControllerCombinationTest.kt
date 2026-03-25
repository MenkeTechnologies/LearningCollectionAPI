package com.menketechnologies.learningcollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.*

/**
 * Combinatorial tests for LearningController.
 * Tests endpoint behavior with various combinations of repo size and request parameters.
 */
@ExtendWith(MockitoExtension::class)
class LearningControllerCombinationTest {

    @Mock
    private lateinit var lcRepo: LCRepo

    @InjectMocks
    private lateinit var controller: LearningController

    private fun makeLc(id: Long, learning: String, category: String = DEFAULT_CAT, date: Date = Date()): LearningCollection {
        val lc = LearningCollection(learning, category, date)
        lc.id = id
        return lc
    }

    private fun makeItems(size: Int): List<LearningCollection> =
        (1..size).map { makeLc(it.toLong(), "learning_$it", "cat_${it % 10}", Date(it.toLong() * 1000)) }

    // ===== RECENTS SHORT: repo_size × count combinations =====

    @TestFactory
    fun `recents-count - repo size x count combinations`(): List<DynamicTest> {
        val repoSizes = listOf(0, 1, 2, 5, 10, 19, 20, 21, 50, 100)
        val counts = listOf(0, 1, 2, 5, 10, 19, 20, 21, 50, 100, 200)
        return repoSizes.flatMap { repoSize ->
            counts.map { count ->
                dynamicTest("recents-short: repoSize=$repoSize, count=$count") {
                    val items = makeItems(repoSize)
                    whenever(lcRepo.findAll()).thenReturn(items)
                    val result = controller.getLearningItemRecentShort(count)
                    assertEquals(minOf(count, repoSize), result.size)
                    if (result.isNotEmpty()) {
                        assertEquals("learning_$repoSize", result[0])
                    }
                    result.forEach { assertTrue(it is String) }
                }
            }
        }
    }

    // ===== RECENT: repo_size × count combinations =====

    @TestFactory
    fun `recent-count - repo size x count combinations`(): List<DynamicTest> {
        val repoSizes = listOf(0, 1, 2, 5, 10, 19, 20, 21, 50, 100)
        val counts = listOf(0, 1, 2, 5, 10, 19, 20, 21, 50, 100, 200)
        return repoSizes.flatMap { repoSize ->
            counts.map { count ->
                dynamicTest("recent: repoSize=$repoSize, count=$count") {
                    val items = makeItems(repoSize)
                    whenever(lcRepo.findAll()).thenReturn(items)
                    val result = controller.getLearningItemRecent(count)
                    assertEquals(minOf(count, repoSize), result.size)
                    if (result.isNotEmpty()) {
                        assertEquals(repoSize.toLong(), result[0].id)
                        assertEquals("learning_$repoSize", result[0].learning)
                    }
                }
            }
        }
    }

    // ===== RANDOMS COUNT: repo_size × count combinations =====

    @TestFactory
    fun `randoms-count - repo size x count combinations`(): List<DynamicTest> {
        val repoSizes = listOf(1, 2, 5, 10, 20, 50, 100)
        val counts = listOf(0, 1, 2, 5, 10, 20, 50, 100, 200)
        return repoSizes.flatMap { repoSize ->
            counts.map { count ->
                dynamicTest("randoms-short: repoSize=$repoSize, count=$count") {
                    val items = makeItems(repoSize)
                    val allLearnings = items.map { it.learning }.toSet()
                    whenever(lcRepo.findAll()).thenReturn(items)
                    val result = controller.getLearningItemCountShort(count)
                    assertEquals(minOf(count, repoSize), result.size)
                    result.forEach { assertTrue(allLearnings.contains(it)) }
                }
            }
        }
    }

    // ===== RANDOM COUNT: repo_size × count combinations =====

    @TestFactory
    fun `random-count - repo size x count combinations`(): List<DynamicTest> {
        val repoSizes = listOf(1, 2, 5, 10, 20, 50, 100)
        val counts = listOf(0, 1, 2, 5, 10, 20, 50, 100, 200)
        return repoSizes.flatMap { repoSize ->
            counts.map { count ->
                dynamicTest("random: repoSize=$repoSize, count=$count") {
                    val items = makeItems(repoSize)
                    val allIds = items.map { it.id }.toSet()
                    whenever(lcRepo.findAll()).thenReturn(items)
                    val result = controller.getLearningItemCount(count)
                    assertEquals(minOf(count, repoSize), result.size)
                    result.forEach { assertTrue(allIds.contains(it.id)) }
                }
            }
        }
    }

    // ===== FILTER: various patterns × repo contents =====

    @TestFactory
    fun `filter - pattern matching combinations`(): List<DynamicTest> {
        val patterns = listOf(
            "learn", "kotlin", "spring", "docker", "test",
            "item", "cat", "a", "e", "ing",
            "1", "2", "3", "10", "100",
        )
        val repoSizes = listOf(0, 1, 5, 10, 20, 50)
        return patterns.flatMap { pattern ->
            repoSizes.map { repoSize ->
                dynamicTest("filter '$pattern' with $repoSize items") {
                    val items = makeItems(repoSize)
                    val matching = items.filter { it.learning.contains(pattern) }
                    whenever(lcRepo.findAllByLearningContaining(pattern)).thenReturn(matching)
                    val result = controller.filterLearn(pattern)
                    assertEquals(matching.size, result.size)
                    result.forEach { assertTrue(it.contains(pattern)) }
                }
            }
        }
    }

    // ===== ADD: with various strings and verify save =====

    @TestFactory
    fun `add - various learning strings verify category and date`(): List<DynamicTest> {
        val inputs = (1..100).map { "learning item #$it with some content about topic ${it % 20}" }
        return inputs.mapIndexed { i, input ->
            dynamicTest("add learning #$i") {
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val before = Date()
                val result = controller.add(input)
                val after = Date()
                assertEquals(input, result.learning)
                assertEquals(DEFAULT_CAT, result.category)
                assertTrue(result.dateAdded.time >= before.time)
                assertTrue(result.dateAdded.time <= after.time)
            }
        }
    }

    // ===== RECENTS DEFAULT: verify SHORT_CNT boundary =====

    @TestFactory
    fun `recents default - boundary around SHORT_CNT`(): List<DynamicTest> =
        (15..25).map { size ->
            dynamicTest("recents default boundary: repoSize=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemRecentShortDefault()
                assertEquals(minOf(size, SHORT_CNT), result.size)
                assertEquals("learning_$size", result[0])
            }
        }

    // ===== VERIFY ALL ENDPOINTS CALL findAll =====

    @TestFactory
    fun `all findAll endpoints call repo`(): List<DynamicTest> {
        data class EndpointCall(val name: String, val invoke: () -> Any)
        return listOf(
            EndpointCall("learningItemRecentShortDefault") { controller.learningItemRecentShortDefault() },
            EndpointCall("getLearningItemRecentShort(5)") { controller.getLearningItemRecentShort(5) },
            EndpointCall("getLearningItemRecent(5)") { controller.getLearningItemRecent(5) },
            EndpointCall("learningItemCountShort") { controller.learningItemCountShort() },
            EndpointCall("getLearningItemCountShort(5)") { controller.getLearningItemCountShort(5) },
            EndpointCall("getLearningItemCount(5)") { controller.getLearningItemCount(5) },
            EndpointCall("learningItem") { controller.learningItem() },
        ).map { (name, invoke) ->
            dynamicTest("$name calls findAll") {
                whenever(lcRepo.findAll()).thenReturn(makeItems(10))
                invoke()
                verify(lcRepo, atLeastOnce()).findAll()
            }
        }
    }

    // ===== RECENT SHORT returns strings, RECENT returns entities =====

    @TestFactory
    fun `recents-short vs recent - type difference across sizes`(): List<DynamicTest> =
        (1..30).map { size ->
            dynamicTest("type comparison for size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)

                val shortResult = controller.getLearningItemRecentShort(size)
                shortResult.forEach { assertTrue(it is String) }

                val fullResult = controller.getLearningItemRecent(size)
                fullResult.forEach { entity ->
                    assertNotNull(entity.learning)
                    assertNotNull(entity.category)
                    assertNotNull(entity.dateAdded)
                }
            }
        }

    // ===== RANDOMS SHORT returns strings, RANDOM returns entities =====

    @TestFactory
    fun `randoms-short vs random - type difference across sizes`(): List<DynamicTest> =
        (1..30).map { size ->
            dynamicTest("random type comparison for size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)

                val shortResult = controller.getLearningItemCountShort(size)
                shortResult.forEach { assertTrue(it is String) }

                val fullResult = controller.getLearningItemCount(size)
                fullResult.forEach { entity ->
                    assertNotNull(entity.learning)
                    assertNotNull(entity.category)
                    assertNotNull(entity.dateAdded)
                    assertTrue(entity.id > 0)
                }
            }
        }

    // ===== RECENTS with identical learning strings =====

    @TestFactory
    fun `recents with duplicate learning strings`(): List<DynamicTest> =
        (1..20).map { dupeCount ->
            dynamicTest("recents with $dupeCount duplicates") {
                val items = (1..dupeCount).map { makeLc(it.toLong(), "same learning") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemRecentShortDefault()
                assertEquals(minOf(dupeCount, SHORT_CNT), result.size)
                result.forEach { assertEquals("same learning", it) }
            }
        }

    // ===== FILTER with matching substrings =====

    @TestFactory
    fun `filter with substring at various positions`(): List<DynamicTest> {
        val base = "abcdefghijklmnopqrstuvwxyz"
        return (0 until base.length).flatMap { start ->
            (start + 1..minOf(start + 5, base.length)).map { end ->
                val sub = base.substring(start, end)
                dynamicTest("filter substring '$sub' (pos $start-$end)") {
                    val item = makeLc(1, base)
                    whenever(lcRepo.findAllByLearningContaining(sub)).thenReturn(listOf(item))
                    val result = controller.filterLearn(sub)
                    assertEquals(1, result.size)
                    assertTrue(result[0].contains(sub))
                }
            }
        }
    }

    // ===== RECENTS ordering consistency =====

    @TestFactory
    fun `recents ordering is consistently reversed for various sizes`(): List<DynamicTest> =
        (2..50).map { size ->
            dynamicTest("recents order consistency for $size items") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecentShort(size)
                for (i in 0 until result.size - 1) {
                    val currentNum = result[i].removePrefix("learning_").toInt()
                    val nextNum = result[i + 1].removePrefix("learning_").toInt()
                    assertTrue(currentNum > nextNum, "Expected descending order")
                }
            }
        }

    // ===== RECENT entity ordering consistency =====

    @TestFactory
    fun `recent entity ordering is consistently reversed for various sizes`(): List<DynamicTest> =
        (2..50).map { size ->
            dynamicTest("recent entity order consistency for $size items") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(size)
                for (i in 0 until result.size - 1) {
                    assertTrue(result[i].id > result[i + 1].id, "Expected descending ID order")
                }
            }
        }

    // ===== ENTITY DATA INTEGRITY across all endpoints =====

    @TestFactory
    fun `entity data integrity across recent and random endpoints`(): List<DynamicTest> {
        val sizes = listOf(1, 5, 10, 20, 50)
        val endpoints = listOf("recent", "random")
        return sizes.flatMap { size ->
            endpoints.map { endpoint ->
                dynamicTest("data integrity: $endpoint with $size items") {
                    val items = makeItems(size)
                    whenever(lcRepo.findAll()).thenReturn(items)
                    val result = when (endpoint) {
                        "recent" -> controller.getLearningItemRecent(size)
                        "random" -> controller.getLearningItemCount(size)
                        else -> emptyList()
                    }
                    assertEquals(size, result.size)
                    result.forEach { entity ->
                        val original = items.find { it.id == entity.id }
                        assertNotNull(original)
                        assertEquals(original!!.learning, entity.learning)
                        assertEquals(original.category, entity.category)
                        assertEquals(original.dateAdded, entity.dateAdded)
                    }
                }
            }
        }
    }
}
