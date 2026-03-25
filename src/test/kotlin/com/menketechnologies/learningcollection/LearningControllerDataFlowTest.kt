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
 * Data flow tests verifying that data passes correctly through
 * controller methods with various transformations.
 */
@ExtendWith(MockitoExtension::class)
class LearningControllerDataFlowTest {

    @Mock
    private lateinit var lcRepo: LCRepo

    @InjectMocks
    private lateinit var controller: LearningController

    private fun makeLc(id: Long, learning: String, category: String = DEFAULT_CAT, date: Date = Date()): LearningCollection {
        val lc = LearningCollection(learning, category, date)
        lc.id = id
        return lc
    }

    private fun makeItems(size: Int, prefix: String = "learning"): List<LearningCollection> =
        (1..size).map { makeLc(it.toLong(), "${prefix}_$it", "cat_${it % 30}", Date(it.toLong() * 1000)) }

    // ===== ADD: input flows through to saved entity =====

    @TestFactory
    fun `add data flow - learning string preserved for 500 inputs`(): List<DynamicTest> =
        (1..500).map { i ->
            dynamicTest("add flow learning #$i") {
                val input = "flow_test_$i"
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val result = controller.add(input)
                assertEquals(input, result.learning)
            }
        }

    // ===== FILTER: maps entities to learning strings =====

    @TestFactory
    fun `filter maps entity to string for sizes 1 to 200`(): List<DynamicTest> =
        (1..200).map { size ->
            dynamicTest("filter entity-to-string size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAllByLearningContaining("learning")).thenReturn(items)
                val result = controller.filterLearn("learning")
                assertEquals(size, result.size)
                result.forEachIndexed { idx, s ->
                    assertEquals(items[idx].learning, s)
                }
            }
        }

    // ===== RECENTS: maps then reverses then takes =====

    @TestFactory
    fun `recents applies map-reverse-take correctly for sizes 1 to 200`(): List<DynamicTest> =
        (1..200).map { size ->
            dynamicTest("recents map-reverse-take size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemRecentShortDefault()
                val expected = items.map { it.learning }.reversed().take(SHORT_CNT)
                assertEquals(expected, result)
            }
        }

    // ===== RECENTS/{count}: maps then reverses then takes count =====

    @TestFactory
    fun `recents-count applies map-reverse-take for 200 combos`(): List<DynamicTest> {
        val combos = (1..20).flatMap { size -> (0..9).map { count -> Pair(size * 10, count * 5) } }
        return combos.map { (size, count) ->
            dynamicTest("recents-count size=$size count=$count") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecentShort(count)
                val expected = items.map { it.learning }.reversed().take(count)
                assertEquals(expected, result)
            }
        }
    }

    // ===== RECENT/{count}: reverses then takes =====

    @TestFactory
    fun `recent-count applies reverse-take for 200 combos`(): List<DynamicTest> {
        val combos = (1..20).flatMap { size -> (0..9).map { count -> Pair(size * 10, count * 5) } }
        return combos.map { (size, count) ->
            dynamicTest("recent-count size=$size count=$count") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(count)
                val expected = items.reversed().take(count)
                assertEquals(expected.size, result.size)
                result.forEachIndexed { idx, lc ->
                    assertEquals(expected[idx].learning, lc.learning)
                    assertEquals(expected[idx].id, lc.id)
                }
            }
        }
    }

    // ===== RANDOMS: shuffles then maps then first =====

    @TestFactory
    fun `randoms returns valid learning string from repo for 300 calls`(): List<DynamicTest> {
        val items = makeItems(100)
        return (1..300).map { i ->
            dynamicTest("randoms valid string #$i") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemCountShort()
                assertTrue(items.any { it.learning == result })
            }
        }
    }

    // ===== RANDOMS/{count}: shuffles then takes then maps =====

    @TestFactory
    fun `randoms-count returns valid strings for 200 combos`(): List<DynamicTest> {
        val combos = (1..20).flatMap { size -> (1..10).map { count -> Pair(size * 5, count) } }
        return combos.map { (size, count) ->
            dynamicTest("randoms-count size=$size count=$count") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCountShort(count)
                val validLearnings = items.map { it.learning }.toSet()
                result.forEach { s -> assertTrue(s in validLearnings) }
            }
        }
    }

    // ===== RANDOM/{count}: shuffles then takes =====

    @TestFactory
    fun `random-count returns valid entities for 200 combos`(): List<DynamicTest> {
        val combos = (1..20).flatMap { size -> (1..10).map { count -> Pair(size * 5, count) } }
        return combos.map { (size, count) ->
            dynamicTest("random-count size=$size count=$count") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCount(count)
                val validIds = items.map { it.id }.toSet()
                result.forEach { lc -> assertTrue(lc.id in validIds) }
            }
        }
    }

    // ===== RANDOM: shuffles then first =====

    @TestFactory
    fun `random returns valid entity from repo for 300 calls`(): List<DynamicTest> {
        val items = makeItems(100)
        return (1..300).map { i ->
            dynamicTest("random valid entity #$i") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItem()
                assertTrue(items.any { it.id == result.id })
            }
        }
    }

    // ===== FILTER PRESERVES ORDER =====

    @TestFactory
    fun `filter preserves repo order for sizes 1 to 200`(): List<DynamicTest> =
        (1..200).map { size ->
            dynamicTest("filter order preserved size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAllByLearningContaining("learning")).thenReturn(items)
                val result = controller.filterLearn("learning")
                for (i in result.indices) {
                    assertEquals("learning_${i + 1}", result[i])
                }
            }
        }

    // ===== ADD ALWAYS USES DEFAULT_CAT =====

    @TestFactory
    fun `add always sets DEFAULT_CAT regardless of input for 300 tests`(): List<DynamicTest> {
        val inputs = (1..300).map { "input_$it" }
        return inputs.map { input ->
            dynamicTest("add DEFAULT_CAT for '$input'") {
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val result = controller.add(input)
                assertEquals("programming", result.category)
            }
        }
    }

    // ===== ADD ALWAYS SETS DATE =====

    @TestFactory
    fun `add always sets non-null date for 300 tests`(): List<DynamicTest> =
        (1..300).map { i ->
            dynamicTest("add non-null date #$i") {
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val result = controller.add("test_$i")
                assertNotNull(result.dateAdded)
            }
        }

    // ===== RECENTS SIZE NEVER EXCEEDS SHORT_CNT =====

    @TestFactory
    fun `recents default size never exceeds SHORT_CNT for 200 repo sizes`(): List<DynamicTest> =
        (0..199).map { size ->
            dynamicTest("recents max SHORT_CNT size=$size") {
                val items = if (size == 0) emptyList() else makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemRecentShortDefault()
                assertTrue(result.size <= SHORT_CNT)
            }
        }

    // ===== RECENTS-COUNT NEVER EXCEEDS REPO SIZE =====

    @TestFactory
    fun `recents-count never exceeds repo size for 200 combos`(): List<DynamicTest> {
        val combos = (1..20).flatMap { s -> (0..9).map { c -> Pair(s * 5, c * 20) } }
        return combos.map { (size, count) ->
            dynamicTest("recents-count bounded size=$size count=$count") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecentShort(count)
                assertTrue(result.size <= size)
                assertTrue(result.size <= count)
            }
        }
    }

    // ===== RANDOM-COUNT NEVER EXCEEDS REPO SIZE =====

    @TestFactory
    fun `random-count never exceeds repo size for 200 combos`(): List<DynamicTest> {
        val combos = (1..20).flatMap { s -> (0..9).map { c -> Pair(s * 5, c * 20) } }
        return combos.map { (size, count) ->
            dynamicTest("random-count bounded size=$size count=$count") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCount(count)
                assertTrue(result.size <= size)
                assertTrue(result.size <= count)
            }
        }
    }

    // ===== ALL RANDOM RESULTS COME FROM REPO =====

    @TestFactory
    fun `all random entities come from repo for 200 sizes`(): List<DynamicTest> =
        (1..200).map { size ->
            dynamicTest("random entities from repo size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCount(minOf(size, 10))
                val allIds = items.map { it.id }.toSet()
                result.forEach { lc -> assertTrue(lc.id in allIds) }
            }
        }

    // ===== RECENTS RETURNS CORRECT DATA FROM REPO =====

    @TestFactory
    fun `recents returns data matching repo for 100 sizes`(): List<DynamicTest> =
        (1..100).map { size ->
            dynamicTest("recents data matches repo size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemRecentShortDefault()
                assertTrue(result.all { s -> items.any { it.learning == s } })
            }
        }

    // ===== FILTER RETURNS DATA FROM REPO =====

    @TestFactory
    fun `filter returns data from repo for 200 queries`(): List<DynamicTest> =
        (1..200).map { i ->
            val query = "q_$i"
            dynamicTest("filter data from repo '$query'") {
                val items = (1..3).map { makeLc(it.toLong(), "${query}_item_$it") }
                whenever(lcRepo.findAllByLearningContaining(query)).thenReturn(items)
                val result = controller.filterLearn(query)
                assertEquals(3, result.size)
                result.forEach { s -> assertTrue(s.contains(query)) }
            }
        }

    // ===== EMPTY REPO BEHAVIOR =====

    @TestFactory
    fun `recents-count with empty repo for counts 0 to 200`(): List<DynamicTest> =
        (0..200).map { count ->
            dynamicTest("recents-count empty repo count=$count") {
                whenever(lcRepo.findAll()).thenReturn(emptyList())
                val result = controller.getLearningItemRecentShort(count)
                assertTrue(result.isEmpty())
            }
        }

    @TestFactory
    fun `recent-count with empty repo for counts 0 to 200`(): List<DynamicTest> =
        (0..200).map { count ->
            dynamicTest("recent-count empty repo count=$count") {
                whenever(lcRepo.findAll()).thenReturn(emptyList())
                val result = controller.getLearningItemRecent(count)
                assertTrue(result.isEmpty())
            }
        }

    @TestFactory
    fun `randoms-count with empty repo for counts 0 to 100`(): List<DynamicTest> =
        (0..100).map { count ->
            dynamicTest("randoms-count empty repo count=$count") {
                whenever(lcRepo.findAll()).thenReturn(emptyList())
                val result = controller.getLearningItemCountShort(count)
                assertTrue(result.isEmpty())
            }
        }

    @TestFactory
    fun `random-count with empty repo for counts 0 to 100`(): List<DynamicTest> =
        (0..100).map { count ->
            dynamicTest("random-count empty repo count=$count") {
                whenever(lcRepo.findAll()).thenReturn(emptyList())
                val result = controller.getLearningItemCount(count)
                assertTrue(result.isEmpty())
            }
        }
}
