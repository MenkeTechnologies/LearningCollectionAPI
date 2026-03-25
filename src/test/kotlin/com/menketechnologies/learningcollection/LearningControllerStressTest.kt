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
 * Stress and boundary tests for LearningController with large data sets.
 */
@ExtendWith(MockitoExtension::class)
class LearningControllerStressTest {

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
        (1..size).map { makeLc(it.toLong(), "learning_$it", "cat_${it % 50}", Date(it.toLong() * 1000)) }

    // ===== LARGE REPO: recents default with big repos =====

    @TestFactory
    fun `recents default with large repos`(): List<DynamicTest> =
        listOf(100, 200, 500, 1000).map { size ->
            dynamicTest("recents default with $size items") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemRecentShortDefault()
                assertEquals(SHORT_CNT, result.size)
                assertEquals("learning_$size", result[0])
                assertEquals("learning_${size - SHORT_CNT + 1}", result[SHORT_CNT - 1])
            }
        }

    // ===== MANY SEQUENTIAL ADD CALLS =====

    @TestFactory
    fun `sequential add calls`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("sequential add #$i") {
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val result = controller.add("learning #$i")
                assertEquals("learning #$i", result.learning)
                assertEquals(DEFAULT_CAT, result.category)
            }
        }

    // ===== FILTER WITH VARIOUS RESULT SIZES =====

    @TestFactory
    fun `filter with result sizes 0 to 200`(): List<DynamicTest> =
        (0..200).map { resultSize ->
            dynamicTest("filter returns $resultSize results") {
                val items = (1..resultSize).map { makeLc(it.toLong(), "match_$it") }
                whenever(lcRepo.findAllByLearningContaining("match")).thenReturn(items)
                val result = controller.filterLearn("match")
                assertEquals(resultSize, result.size)
            }
        }

    // ===== RECENTS SHORT: fine-grained count sweep =====

    @TestFactory
    fun `recents-short count sweep 0 to 200`(): List<DynamicTest> {
        val items = makeItems(150)
        return (0..200).map { count ->
            dynamicTest("recents-short count=$count") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecentShort(count)
                assertEquals(minOf(count, 150), result.size)
            }
        }
    }

    // ===== RECENT: fine-grained count sweep =====

    @TestFactory
    fun `recent count sweep 0 to 200`(): List<DynamicTest> {
        val items = makeItems(150)
        return (0..200).map { count ->
            dynamicTest("recent count=$count") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(count)
                assertEquals(minOf(count, 150), result.size)
            }
        }
    }

    // ===== RANDOMS SHORT: fine-grained count sweep =====

    @TestFactory
    fun `randoms-short count sweep 0 to 200`(): List<DynamicTest> {
        val items = makeItems(150)
        return (0..200).map { count ->
            dynamicTest("randoms-short count=$count") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCountShort(count)
                assertEquals(minOf(count, 150), result.size)
            }
        }
    }

    // ===== RANDOM: fine-grained count sweep =====

    @TestFactory
    fun `random count sweep 0 to 200`(): List<DynamicTest> {
        val items = makeItems(150)
        return (0..200).map { count ->
            dynamicTest("random count=$count") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCount(count)
                assertEquals(minOf(count, 150), result.size)
            }
        }
    }

    // ===== RANDOM SINGLE: repeated calls produce valid results =====

    @TestFactory
    fun `random single repeated 200 times`(): List<DynamicTest> {
        val items = makeItems(20)
        val allIds = items.map { it.id }.toSet()
        return (1..200).map { i ->
            dynamicTest("random single call #$i") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItem()
                assertTrue(allIds.contains(result.id))
            }
        }
    }

    // ===== RANDOMS SINGLE: repeated calls produce valid results =====

    @TestFactory
    fun `randoms single repeated 200 times`(): List<DynamicTest> {
        val items = makeItems(20)
        val allLearnings = items.map { it.learning }.toSet()
        return (1..200).map { i ->
            dynamicTest("randoms single call #$i") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemCountShort()
                assertTrue(allLearnings.contains(result))
            }
        }
    }

    // ===== ADD WITH LONG STRINGS =====

    @TestFactory
    fun `add with strings of increasing length`(): List<DynamicTest> =
        (1..100).map { len ->
            val multiplier = len * 10
            dynamicTest("add with string length $multiplier") {
                val longStr = "x".repeat(multiplier)
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val result = controller.add(longStr)
                assertEquals(longStr, result.learning)
                assertEquals(multiplier, result.learning.length)
            }
        }

    // ===== FILTER WITH LONG QUERIES =====

    @TestFactory
    fun `filter with queries of increasing length`(): List<DynamicTest> =
        (1..100).map { len ->
            dynamicTest("filter with query length $len") {
                val query = "q".repeat(len)
                whenever(lcRepo.findAllByLearningContaining(query)).thenReturn(emptyList())
                val result = controller.filterLearn(query)
                assertTrue(result.isEmpty())
                verify(lcRepo).findAllByLearningContaining(query)
            }
        }

    // ===== RECENTS ORDER INVARIANT ACROSS REPO SIZES =====

    @TestFactory
    fun `recents ordering invariant across sizes 2 to 100`(): List<DynamicTest> =
        (2..100).map { size ->
            dynamicTest("recents order invariant size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecentShort(size)
                // verify strictly descending
                for (i in 0 until result.size - 1) {
                    val curr = result[i].removePrefix("learning_").toInt()
                    val next = result[i + 1].removePrefix("learning_").toInt()
                    assertTrue(curr > next)
                }
            }
        }

    // ===== RECENT ENTITY ORDER INVARIANT =====

    @TestFactory
    fun `recent entity ordering invariant across sizes 2 to 100`(): List<DynamicTest> =
        (2..100).map { size ->
            dynamicTest("recent entity order invariant size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(size)
                for (i in 0 until result.size - 1) {
                    assertTrue(result[i].id > result[i + 1].id)
                }
            }
        }

    // ===== CATEGORY PRESERVATION =====

    @TestFactory
    fun `recent preserves category across sizes`(): List<DynamicTest> =
        (1..50).map { size ->
            dynamicTest("category preservation for size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(size)
                result.forEach { entity ->
                    val original = items.find { it.id == entity.id }!!
                    assertEquals(original.category, entity.category)
                }
            }
        }

    // ===== DATE PRESERVATION =====

    @TestFactory
    fun `recent preserves dateAdded across sizes`(): List<DynamicTest> =
        (1..50).map { size ->
            dynamicTest("date preservation for size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(size)
                result.forEach { entity ->
                    val original = items.find { it.id == entity.id }!!
                    assertEquals(original.dateAdded, entity.dateAdded)
                }
            }
        }
}
