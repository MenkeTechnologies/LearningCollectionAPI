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
 * Boundary and edge-case tests for LearningController endpoints.
 * Tests with fine-grained sweeps across parameter ranges.
 */
@ExtendWith(MockitoExtension::class)
class LearningControllerBoundaryTest {

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

    // ===== ADD WITH STRINGS OF EVERY LENGTH 0..500 =====

    @TestFactory
    fun `add with string lengths 0 to 500`(): List<DynamicTest> =
        (0..500).map { len ->
            dynamicTest("add string len=$len") {
                val s = "x".repeat(len)
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val result = controller.add(s)
                assertEquals(s, result.learning)
                assertEquals(DEFAULT_CAT, result.category)
                assertEquals(len, result.learning.length)
            }
        }

    // ===== ADD WITH UNICODE STRINGS =====

    @TestFactory
    fun `add with unicode codepoints 0x0020 to 0x02FF`(): List<DynamicTest> =
        (0x0020..0x02FF).map { cp ->
            dynamicTest("add codepoint U+${cp.toString(16).uppercase().padStart(4, '0')}") {
                val s = cp.toChar().toString()
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val result = controller.add(s)
                assertEquals(s, result.learning)
            }
        }

    // ===== FILTER WITH STRINGS OF EVERY LENGTH 0..200 =====

    @TestFactory
    fun `filter with query lengths 0 to 200`(): List<DynamicTest> =
        (0..200).map { len ->
            dynamicTest("filter query len=$len") {
                val query = "q".repeat(len)
                whenever(lcRepo.findAllByLearningContaining(query)).thenReturn(emptyList())
                val result = controller.filterLearn(query)
                assertTrue(result.isEmpty())
            }
        }

    // ===== FILTER MATCHING VARIOUS ITEM COUNTS =====

    @TestFactory
    fun `filter with match counts 0 to 300`(): List<DynamicTest> =
        (0..300).map { matchCount ->
            dynamicTest("filter match count=$matchCount") {
                val items = (1..matchCount).map { makeLc(it.toLong(), "matched_$it") }
                whenever(lcRepo.findAllByLearningContaining("matched")).thenReturn(items)
                val result = controller.filterLearn("matched")
                assertEquals(matchCount, result.size)
                result.forEachIndexed { idx, s -> assertEquals("matched_${idx + 1}", s) }
            }
        }

    // ===== RECENTS DEFAULT WITH REPO SIZES 0..300 =====

    @TestFactory
    fun `recents default with repo sizes 0 to 300`(): List<DynamicTest> =
        (0..300).map { size ->
            dynamicTest("recents default repo size=$size") {
                val items = if (size == 0) emptyList() else makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemRecentShortDefault()
                assertEquals(minOf(size, SHORT_CNT), result.size)
                if (size > 0) {
                    assertEquals("learning_$size", result[0])
                }
            }
        }

    // ===== RECENTS/{count} FULL SWEEP 0..300 with fixed repo =====

    @TestFactory
    fun `recents-count sweep 0 to 300 with 500 items`(): List<DynamicTest> {
        val items = makeItems(500)
        return (0..300).map { count ->
            dynamicTest("recents count=$count from 500 items") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecentShort(count)
                assertEquals(minOf(count, 500), result.size)
                if (count > 0) {
                    assertEquals("learning_500", result[0])
                }
            }
        }
    }

    // ===== RECENT/{count} FULL SWEEP 0..300 with fixed repo =====

    @TestFactory
    fun `recent-count sweep 0 to 300 with 500 items`(): List<DynamicTest> {
        val items = makeItems(500)
        return (0..300).map { count ->
            dynamicTest("recent count=$count from 500 items") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(count)
                assertEquals(minOf(count, 500), result.size)
                if (count > 0) {
                    assertEquals("learning_500", result[0].learning)
                }
            }
        }
    }

    // ===== RANDOMS/{count} SWEEP 0..200 =====

    @TestFactory
    fun `randoms-count sweep 0 to 200 with 500 items`(): List<DynamicTest> {
        val items = makeItems(500)
        return (0..200).map { count ->
            dynamicTest("randoms count=$count from 500 items") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCountShort(count)
                assertEquals(minOf(count, 500), result.size)
                result.forEach { s -> assertTrue(s.startsWith("learning_")) }
            }
        }
    }

    // ===== RANDOM/{count} SWEEP 0..200 =====

    @TestFactory
    fun `random-count sweep 0 to 200 with 500 items`(): List<DynamicTest> {
        val items = makeItems(500)
        return (0..200).map { count ->
            dynamicTest("random count=$count from 500 items") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCount(count)
                assertEquals(minOf(count, 500), result.size)
                result.forEach { lc ->
                    assertTrue(lc.learning.startsWith("learning_"))
                    assertTrue(lc.category.startsWith("cat_"))
                }
            }
        }
    }

    // ===== RECENTS ORDER VERIFICATION FOR VARIOUS REPO SIZES =====

    @TestFactory
    fun `recents order is reversed for repo sizes 2 to 200`(): List<DynamicTest> =
        (2..200).map { size ->
            dynamicTest("recents order reversed size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecentShort(size)
                for (i in 0 until result.size - 1) {
                    val num1 = result[i].removePrefix("learning_").toInt()
                    val num2 = result[i + 1].removePrefix("learning_").toInt()
                    assertTrue(num1 > num2, "expected descending order at index $i")
                }
            }
        }

    // ===== RECENT ENTITY ORDER VERIFICATION =====

    @TestFactory
    fun `recent entities order is reversed for repo sizes 2 to 200`(): List<DynamicTest> =
        (2..200).map { size ->
            dynamicTest("recent entity order reversed size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(size)
                for (i in 0 until result.size - 1) {
                    assertTrue(result[i].id > result[i + 1].id, "expected descending id at index $i")
                }
            }
        }

    // ===== ADD PRESERVES SPECIAL CHARACTERS =====

    @TestFactory
    fun `add preserves special char patterns`(): List<DynamicTest> {
        val patterns = listOf(
            "single'quote", "double\"quote", "back\\slash", "forward/slash",
            "tab\there", "newline\nhere", "cr\rhere", "null\u0000here",
            "angle<bracket>", "curly{brace}", "square[bracket]", "pipe|char",
            "at@sign", "hash#tag", "dollar\$sign", "percent%age",
            "caret^up", "ampersand&and", "asterisk*star", "plus+sign",
            "equals=sign", "tilde~wave", "backtick`tick", "exclaim!mark",
            "question?mark", "colon:here", "semicolon;here", "comma,here",
            "period.here", "dash-here", "underscore_here",
        )
        return patterns.flatMap { pattern ->
            (1..10).map { repeat ->
                dynamicTest("add special pattern '${pattern.take(15)}' repeated $repeat times") {
                    val s = pattern.repeat(repeat)
                    whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                    val result = controller.add(s)
                    assertEquals(s, result.learning)
                }
            }
        }
    }

    // ===== FILTER WITH SPECIAL CHARACTERS =====

    @TestFactory
    fun `filter with various query patterns`(): List<DynamicTest> {
        val queries = (1..200).map { "query_$it" }
        return queries.map { q ->
            dynamicTest("filter query='$q'") {
                whenever(lcRepo.findAllByLearningContaining(q)).thenReturn(emptyList())
                val result = controller.filterLearn(q)
                assertTrue(result.isEmpty())
                verify(lcRepo).findAllByLearningContaining(q)
            }
        }
    }

    // ===== RECENTS WITH EXACTLY SHORT_CNT ITEMS =====

    @TestFactory
    fun `recents with exactly SHORT_CNT items - boundary`(): List<DynamicTest> =
        listOf(SHORT_CNT - 1, SHORT_CNT, SHORT_CNT + 1).flatMap { size ->
            (1..50).map { trial ->
                dynamicTest("recents boundary size=$size trial=$trial") {
                    val items = makeItems(size)
                    whenever(lcRepo.findAll()).thenReturn(items)
                    val result = controller.learningItemRecentShortDefault()
                    assertEquals(minOf(size, SHORT_CNT), result.size)
                }
            }
        }

    // ===== RANDOM SINGLE ITEM - REPEATED CALLS =====

    @TestFactory
    fun `random single entity repeated 300 times`(): List<DynamicTest> {
        val items = makeItems(50)
        return (1..300).map { i ->
            dynamicTest("random single call #$i") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItem()
                assertNotNull(result)
                assertTrue(result.learning.startsWith("learning_"))
            }
        }
    }

    // ===== RANDOMS SINGLE STRING - REPEATED CALLS =====

    @TestFactory
    fun `randoms single string repeated 300 times`(): List<DynamicTest> {
        val items = makeItems(50)
        return (1..300).map { i ->
            dynamicTest("randoms single call #$i") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemCountShort()
                assertNotNull(result)
                assertTrue(result.startsWith("learning_"))
            }
        }
    }

    // ===== ENTITY FIELD PRESERVATION THROUGH CONTROLLER =====

    @TestFactory
    fun `add preserves category as DEFAULT_CAT for 200 inputs`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("add preserves category #$i") {
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val result = controller.add("input_$i")
                assertEquals(DEFAULT_CAT, result.category)
            }
        }

    @TestFactory
    fun `add sets date close to now for 200 inputs`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("add date near now #$i") {
                val before = Date()
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val result = controller.add("input_$i")
                val after = Date()
                assertTrue(result.dateAdded.time >= before.time)
                assertTrue(result.dateAdded.time <= after.time)
            }
        }

    // ===== RECENTS STRING CONTENT VERIFICATION =====

    @TestFactory
    fun `recents returns correct string content for sizes 1 to 100`(): List<DynamicTest> =
        (1..100).map { size ->
            dynamicTest("recents content check size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemRecentShortDefault()
                result.forEach { s ->
                    assertTrue(s.startsWith("learning_"))
                    val num = s.removePrefix("learning_").toInt()
                    assertTrue(num in 1..size)
                }
            }
        }

    // ===== RECENT ENTITY DATA INTEGRITY =====

    @TestFactory
    fun `recent entities preserve all fields for sizes 1 to 100`(): List<DynamicTest> =
        (1..100).map { size ->
            dynamicTest("recent entity integrity size=$size") {
                val items = makeItems(size)
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(size)
                assertEquals(size, result.size)
                result.forEach { lc ->
                    assertTrue(lc.learning.startsWith("learning_"))
                    assertTrue(lc.category.startsWith("cat_"))
                    assertTrue(lc.id > 0)
                    assertNotNull(lc.dateAdded)
                }
            }
        }

    // ===== FILTER RETURNS STRINGS NOT ENTITIES =====

    @TestFactory
    fun `filter returns strings for match counts 1 to 200`(): List<DynamicTest> =
        (1..200).map { matchCount ->
            dynamicTest("filter returns strings count=$matchCount") {
                val items = (1..matchCount).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAllByLearningContaining("item")).thenReturn(items)
                val result = controller.filterLearn("item")
                assertEquals(matchCount, result.size)
                result.forEach { s ->
                    assertTrue(s is String)
                    assertTrue(s.startsWith("item_"))
                }
            }
        }
}
