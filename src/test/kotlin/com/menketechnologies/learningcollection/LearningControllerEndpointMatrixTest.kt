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
 * Matrix tests covering cross-product combinations across
 * repo sizes, request counts, and endpoint behaviors.
 */
@ExtendWith(MockitoExtension::class)
class LearningControllerEndpointMatrixTest {

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

    // ===== RECENTS-SHORT MATRIX: 30 repo sizes x 30 counts =====

    @TestFactory
    fun `recents-short matrix repo x count`(): List<DynamicTest> {
        val repoSizes = (0..29).map { it * 10 }  // 0, 10, 20, ..., 290
        val counts = (0..29).map { it * 5 }  // 0, 5, 10, ..., 145
        return repoSizes.flatMap { size ->
            counts.map { count ->
                dynamicTest("recents-short repo=$size count=$count") {
                    val items = if (size == 0) emptyList() else makeItems(size)
                    whenever(lcRepo.findAll()).thenReturn(items)
                    val result = controller.getLearningItemRecentShort(count)
                    assertEquals(minOf(count, size), result.size)
                    result.forEach { s -> assertTrue(s is String) }
                }
            }
        }
    }

    // ===== RECENT ENTITY MATRIX: 30 repo sizes x 30 counts =====

    @TestFactory
    fun `recent entity matrix repo x count`(): List<DynamicTest> {
        val repoSizes = (0..29).map { it * 10 }
        val counts = (0..29).map { it * 5 }
        return repoSizes.flatMap { size ->
            counts.map { count ->
                dynamicTest("recent entity repo=$size count=$count") {
                    val items = if (size == 0) emptyList() else makeItems(size)
                    whenever(lcRepo.findAll()).thenReturn(items)
                    val result = controller.getLearningItemRecent(count)
                    assertEquals(minOf(count, size), result.size)
                    result.forEach { lc ->
                        assertNotNull(lc.learning)
                        assertNotNull(lc.category)
                    }
                }
            }
        }
    }

    // ===== RANDOMS-COUNT MATRIX: 20 repo sizes x 20 counts =====

    @TestFactory
    fun `randoms-count matrix repo x count`(): List<DynamicTest> {
        val repoSizes = (1..20).map { it * 15 }  // 15, 30, ..., 300
        val counts = (0..19).map { it * 5 }  // 0, 5, ..., 95
        return repoSizes.flatMap { size ->
            counts.map { count ->
                dynamicTest("randoms-count repo=$size count=$count") {
                    val items = makeItems(size)
                    whenever(lcRepo.findAll()).thenReturn(items)
                    val result = controller.getLearningItemCountShort(count)
                    assertEquals(minOf(count, size), result.size)
                    result.forEach { s -> assertTrue(s.startsWith("learning_")) }
                }
            }
        }
    }

    // ===== RANDOM-COUNT MATRIX: 20 repo sizes x 20 counts =====

    @TestFactory
    fun `random-count entity matrix repo x count`(): List<DynamicTest> {
        val repoSizes = (1..20).map { it * 15 }
        val counts = (0..19).map { it * 5 }
        return repoSizes.flatMap { size ->
            counts.map { count ->
                dynamicTest("random-count entity repo=$size count=$count") {
                    val items = makeItems(size)
                    whenever(lcRepo.findAll()).thenReturn(items)
                    val result = controller.getLearningItemCount(count)
                    assertEquals(minOf(count, size), result.size)
                    result.forEach { lc ->
                        assertTrue(lc.learning.startsWith("learning_"))
                        assertTrue(lc.id > 0)
                    }
                }
            }
        }
    }

    // ===== FILTER PATTERN x REPO SIZE MATRIX =====

    @TestFactory
    fun `filter matrix pattern x repo size`(): List<DynamicTest> {
        val patterns = listOf("learn", "spring", "kotlin", "docker", "test", "git",
            "react", "java", "python", "rust", "go", "sql", "api", "web", "cloud")
        val repoSizes = listOf(0, 1, 5, 10, 25, 50, 100, 200)
        return patterns.flatMap { pattern ->
            repoSizes.map { size ->
                dynamicTest("filter pattern='$pattern' repoMatches=$size") {
                    val items = (1..size).map { makeLc(it.toLong(), "${pattern}_item_$it") }
                    whenever(lcRepo.findAllByLearningContaining(pattern)).thenReturn(items)
                    val result = controller.filterLearn(pattern)
                    assertEquals(size, result.size)
                    result.forEach { s -> assertTrue(s.contains(pattern)) }
                }
            }
        }
    }

    // ===== ADD x CATEGORY VERIFICATION MATRIX =====

    @TestFactory
    fun `add matrix 300 different inputs all get DEFAULT_CAT`(): List<DynamicTest> =
        (1..300).map { i ->
            dynamicTest("add input #$i gets DEFAULT_CAT") {
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val result = controller.add("input_$i")
                assertEquals("input_$i", result.learning)
                assertEquals(DEFAULT_CAT, result.category)
            }
        }

    // ===== RECENTS DEFAULT x REPO SIZE =====

    @TestFactory
    fun `recents default across 50 repo sizes`(): List<DynamicTest> =
        (0..49).map { i ->
            val size = i * 5
            dynamicTest("recents default repo=$size") {
                val items = if (size == 0) emptyList() else makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemRecentShortDefault()
                assertEquals(minOf(size, SHORT_CNT), result.size)
            }
        }

    // ===== RANDOM SINGLE ENTITY x REPO SIZE =====

    @TestFactory
    fun `random single entity across repo sizes`(): List<DynamicTest> =
        (1..100).map { size ->
            dynamicTest("random single entity repo=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItem()
                assertNotNull(result)
                assertTrue(result.id in 1..size.toLong())
            }
        }

    // ===== RANDOMS SINGLE STRING x REPO SIZE =====

    @TestFactory
    fun `randoms single string across repo sizes`(): List<DynamicTest> =
        (1..100).map { size ->
            dynamicTest("randoms single string repo=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemCountShort()
                assertTrue(result.startsWith("learning_"))
                val num = result.removePrefix("learning_").toInt()
                assertTrue(num in 1..size)
            }
        }

    // ===== RECENTS-SHORT: VERIFY LAST ELEMENT =====

    @TestFactory
    fun `recents-short last element is correct for sizes 1 to 200`(): List<DynamicTest> =
        (1..200).map { size ->
            dynamicTest("recents-short last element size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val count = minOf(size, 100)
                val result = controller.getLearningItemRecentShort(count)
                if (result.isNotEmpty()) {
                    val lastNum = result.last().removePrefix("learning_").toInt()
                    assertEquals(size - count + 1, lastNum)
                }
            }
        }

    // ===== RECENT: VERIFY LAST ELEMENT =====

    @TestFactory
    fun `recent last entity is correct for sizes 1 to 200`(): List<DynamicTest> =
        (1..200).map { size ->
            dynamicTest("recent last entity size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val count = minOf(size, 100)
                val result = controller.getLearningItemRecent(count)
                if (result.isNotEmpty()) {
                    val lastId = result.last().id
                    assertEquals((size - count + 1).toLong(), lastId)
                }
            }
        }

    // ===== FILTER RETURNS MAPPED STRINGS =====

    @TestFactory
    fun `filter returns mapped learning strings not entities for 100 sizes`(): List<DynamicTest> =
        (1..100).map { size ->
            dynamicTest("filter returns strings not entities size=$size") {
                val items = (1..size).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAllByLearningContaining("item")).thenReturn(items)
                val result = controller.filterLearn("item")
                assertEquals(size, result.size)
                result.forEachIndexed { idx, s ->
                    assertEquals("item_${idx + 1}", s)
                }
            }
        }

    // ===== ADD CALLS SAVE FOR EACH INPUT =====

    @TestFactory
    fun `add returns saved entity for 200 inputs`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("add returns entity #$i") {
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val result = controller.add("verify_$i")
                assertEquals("verify_$i", result.learning)
                assertNotNull(result.dateAdded)
            }
        }

    // ===== RECENTS USES REPO =====

    @TestFactory
    fun `recents returns data from repo for 100 sizes`(): List<DynamicTest> =
        (0..99).map { size ->
            dynamicTest("recents from repo size=$size") {
                val items = if (size == 0) emptyList() else makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemRecentShortDefault()
                assertEquals(minOf(size, SHORT_CNT), result.size)
            }
        }

    // ===== CATEGORY DISTRIBUTION IN RESULTS =====

    @TestFactory
    fun `recent preserves category distribution across sizes`(): List<DynamicTest> =
        listOf(10, 25, 50, 100, 150, 200).map { size ->
            dynamicTest("category distribution size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(size)
                val categories = result.map { it.category }.toSet()
                assertTrue(categories.size > 1, "expected multiple categories")
            }
        }

    // ===== DATE ORDERING IN RECENT =====

    @TestFactory
    fun `recent dates are in descending order for sizes 2 to 100`(): List<DynamicTest> =
        (2..100).map { size ->
            dynamicTest("recent date order size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(size)
                for (i in 0 until result.size - 1) {
                    assertTrue(result[i].dateAdded.time >= result[i + 1].dateAdded.time)
                }
            }
        }
}
