package com.menketechnologies.learningcollection

import jakarta.servlet.http.HttpServletResponse
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
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

@ExtendWith(MockitoExtension::class)
class LearningControllerTest {

    @Mock
    private lateinit var lcRepo: LCRepo

    @InjectMocks
    private lateinit var controller: LearningController

    private fun makeLc(id: Long, learning: String, category: String = DEFAULT_CAT, date: Date = Date()): LearningCollection {
        val lc = LearningCollection(learning, category, date)
        lc.id = id
        return lc
    }

    private val sampleItems = listOf(
        makeLc(1, "learn kotlin", date = Date(1000)),
        makeLc(2, "learn spring", date = Date(2000)),
        makeLc(3, "learn docker", date = Date(3000)),
        makeLc(4, "learn testing", date = Date(4000)),
        makeLc(5, "learn git", date = Date(5000)),
    )

    // ===== ADD ENDPOINT =====

    @Test
    fun `add saves learning with default category and current date`() {
        val saved = makeLc(10, "new learning")
        whenever(lcRepo.save(any<LearningCollection>())).thenReturn(saved)
        val result = controller.add("new learning")
        assertEquals(saved, result)
        verify(lcRepo).save(argThat<LearningCollection> {
            learning == "new learning" && category == DEFAULT_CAT
        })
    }

    @Test
    fun `add with empty string`() {
        val saved = makeLc(11, "")
        whenever(lcRepo.save(any<LearningCollection>())).thenReturn(saved)
        val result = controller.add("")
        assertEquals("", result.learning)
    }

    @Test
    fun `add with special characters`() {
        val special = "<script>alert('xss')</script>"
        val saved = makeLc(12, special)
        whenever(lcRepo.save(any<LearningCollection>())).thenReturn(saved)
        val result = controller.add(special)
        assertEquals(special, result.learning)
    }

    @Test
    fun `add uses DEFAULT_CAT constant`() {
        whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
        controller.add("something")
        verify(lcRepo).save(argThat<LearningCollection> { category == "programming" })
    }

    @Test
    fun `add sets date to approximately now`() {
        val before = Date()
        whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
        val result = controller.add("test")
        val after = Date()
        assertTrue(result.dateAdded.time >= before.time)
        assertTrue(result.dateAdded.time <= after.time)
    }

    @Test
    fun `add calls repo save exactly once`() {
        whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
        controller.add("test")
        verify(lcRepo, times(1)).save(any())
    }

    @Test
    fun `add returns the saved entity`() {
        val saved = makeLc(99, "returned")
        whenever(lcRepo.save(any<LearningCollection>())).thenReturn(saved)
        val result = controller.add("anything")
        assertSame(saved, result)
    }

    @TestFactory
    fun `add with various learning strings`(): List<DynamicTest> {
        val inputs = listOf(
            "kotlin", "spring boot", "docker", "kubernetes", "terraform",
            "react", "angular", "vue", "svelte", "next.js",
            "postgresql", "mongodb", "redis", "elasticsearch", "cassandra",
            "aws", "gcp", "azure", "heroku", "vercel",
            "git", "github", "gitlab", "bitbucket", "svn",
            "junit", "mockito", "testcontainers", "selenium", "cypress",
            "gradle", "maven", "npm", "yarn", "pnpm",
            "linux", "macos", "windows", "android", "ios",
            "rest", "graphql", "grpc", "websocket", "mqtt",
            "json", "xml", "yaml", "toml", "protobuf",
            "tcp", "udp", "http", "https", "ssh",
            "tls", "ssl", "oauth", "jwt", "saml",
            "microservices", "monolith", "serverless", "event-driven", "cqrs",
            "solid", "dry", "kiss", "yagni", "clean-code",
            "design-patterns", "singleton", "factory", "observer", "strategy",
            "linked-list", "binary-tree", "hash-map", "graph", "heap",
            "sorting", "searching", "recursion", "dynamic-programming", "greedy",
            "big-o", "time-complexity", "space-complexity", "amortized", "worst-case",
        )
        return inputs.mapIndexed { i, input ->
            dynamicTest("add learning: '$input'") {
                val saved = makeLc(i.toLong(), input)
                whenever(lcRepo.save(any<LearningCollection>())).thenReturn(saved)
                val result = controller.add(input)
                assertEquals(input, result.learning)
            }
        }
    }

    @TestFactory
    fun `add preserves exact input string`(): List<DynamicTest> {
        val inputs = listOf(
            " leading space", "trailing space ", " both sides ",
            "UPPERCASE", "lowercase", "MiXeD CaSe",
            "with-dashes", "with_underscores", "with.dots",
            "with/slashes", "with\\backslashes",
            "with\ttabs", "with\nnewlines",
            "emoji 😀🎉", "unicode 中文", "accented éàü",
            "numbers 123", "special !@#\$%", "html <b>bold</b>",
        )
        return inputs.mapIndexed { i, input ->
            dynamicTest("add preserves: '$input'") {
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                val result = controller.add(input)
                assertEquals(input, result.learning)
                assertEquals(DEFAULT_CAT, result.category)
            }
        }
    }

    // ===== FILTER ENDPOINT =====

    @Test
    fun `filterLearn returns matching learning strings`() {
        val matches = listOf(makeLc(1, "learn kotlin"), makeLc(2, "learn spring"))
        whenever(lcRepo.findAllByLearningContaining("learn")).thenReturn(matches)
        val result = controller.filterLearn("learn")
        assertEquals(listOf("learn kotlin", "learn spring"), result)
    }

    @Test
    fun `filterLearn returns empty list when no matches`() {
        whenever(lcRepo.findAllByLearningContaining("nonexistent")).thenReturn(emptyList())
        val result = controller.filterLearn("nonexistent")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterLearn maps to learning field only`() {
        val items = listOf(makeLc(1, "kotlin basics", "programming"))
        whenever(lcRepo.findAllByLearningContaining("kotlin")).thenReturn(items)
        val result = controller.filterLearn("kotlin")
        assertEquals(1, result.size)
        assertEquals("kotlin basics", result[0])
    }

    @Test
    fun `filterLearn with empty string`() {
        whenever(lcRepo.findAllByLearningContaining("")).thenReturn(sampleItems)
        val result = controller.filterLearn("")
        assertEquals(5, result.size)
    }

    @Test
    fun `filterLearn calls repo with exact query`() {
        whenever(lcRepo.findAllByLearningContaining(any())).thenReturn(emptyList())
        controller.filterLearn("exact query")
        verify(lcRepo).findAllByLearningContaining("exact query")
    }

    @TestFactory
    fun `filterLearn with various query strings`(): List<DynamicTest> {
        val queries = listOf(
            "kotlin", "spring", "docker", "test", "learn",
            "a", "the", "is", "of", "in",
            "API", "REST", "SQL", "JPA", "CORS",
            "hello world", "foo bar", "unit test",
        )
        return queries.map { query ->
            dynamicTest("filter with query: '$query'") {
                val items = listOf(makeLc(1, "something $query something"))
                whenever(lcRepo.findAllByLearningContaining(query)).thenReturn(items)
                val result = controller.filterLearn(query)
                assertEquals(1, result.size)
                assertTrue(result[0].contains(query))
            }
        }
    }

    @TestFactory
    fun `filterLearn returns correct count for various result sizes`(): List<DynamicTest> =
        (0..50).map { size ->
            dynamicTest("filter returns $size results") {
                val items = (1..size).map { makeLc(it.toLong(), "item $it") }
                whenever(lcRepo.findAllByLearningContaining("item")).thenReturn(items)
                val result = controller.filterLearn("item")
                assertEquals(size, result.size)
            }
        }

    @TestFactory
    fun `filterLearn extracts only learning field from results`(): List<DynamicTest> {
        val categories = listOf("cat1", "cat2", "cat3", "cat4", "cat5")
        return categories.mapIndexed { i, cat ->
            dynamicTest("filter extracts learning not category '$cat'") {
                val items = listOf(makeLc(i.toLong(), "learning_$i", cat))
                whenever(lcRepo.findAllByLearningContaining("learning")).thenReturn(items)
                val result = controller.filterLearn("learning")
                assertEquals("learning_$i", result[0])
                assertFalse(result.contains(cat))
            }
        }
    }

    // ===== RECENTS ENDPOINT (default) =====

    @Test
    fun `recents returns reversed list of learning strings`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.learningItemRecentShortDefault()
        assertEquals(listOf("learn git", "learn testing", "learn docker", "learn spring", "learn kotlin"), result)
    }

    @Test
    fun `recents returns at most SHORT_CNT items`() {
        val manyItems = (1..30L).map { makeLc(it, "item $it") }
        whenever(lcRepo.findAll()).thenReturn(manyItems)
        val result = controller.learningItemRecentShortDefault()
        assertEquals(SHORT_CNT, result.size)
    }

    @Test
    fun `recents returns all items when fewer than SHORT_CNT`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.learningItemRecentShortDefault()
        assertEquals(5, result.size)
    }

    @Test
    fun `recents returns empty when no items`() {
        whenever(lcRepo.findAll()).thenReturn(emptyList())
        val result = controller.learningItemRecentShortDefault()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `recents calls findAll`() {
        whenever(lcRepo.findAll()).thenReturn(emptyList())
        controller.learningItemRecentShortDefault()
        verify(lcRepo).findAll()
    }

    @Test
    fun `recents preserves order after reversal`() {
        val ordered = listOf(makeLc(1, "first"), makeLc(2, "second"), makeLc(3, "third"))
        whenever(lcRepo.findAll()).thenReturn(ordered)
        val result = controller.learningItemRecentShortDefault()
        assertEquals("third", result[0])
        assertEquals("second", result[1])
        assertEquals("first", result[2])
    }

    @TestFactory
    fun `recents with various repo sizes`(): List<DynamicTest> =
        (0..50).map { size ->
            dynamicTest("recents with $size items in repo") {
                val items = (1..size).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemRecentShortDefault()
                assertEquals(minOf(size, SHORT_CNT), result.size)
                if (size > 0) {
                    assertEquals("item_$size", result[0])
                }
            }
        }

    @TestFactory
    fun `recents returns strings not entities for various sizes`(): List<DynamicTest> =
        (1..30).map { size ->
            dynamicTest("recents returns strings for $size items") {
                val items = (1..size).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemRecentShortDefault()
                result.forEach { item -> assertTrue(item is String) }
            }
        }

    // ===== RECENTS/{COUNT} ENDPOINT =====

    @Test
    fun `recents with count returns specified number`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.getLearningItemRecentShort(3)
        assertEquals(3, result.size)
        assertEquals(listOf("learn git", "learn testing", "learn docker"), result)
    }

    @Test
    fun `recents with count 0 returns empty`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.getLearningItemRecentShort(0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `recents with count larger than total returns all`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.getLearningItemRecentShort(100)
        assertEquals(5, result.size)
    }

    @Test
    fun `recents with count 1 returns most recent`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.getLearningItemRecentShort(1)
        assertEquals(listOf("learn git"), result)
    }

    @TestFactory
    fun `recents-count with counts 0 to 100`(): List<DynamicTest> {
        val items = (1..50).map { makeLc(it.toLong(), "item_$it") }
        return (0..100).map { count ->
            dynamicTest("recents with count=$count") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecentShort(count)
                assertEquals(minOf(count, 50), result.size)
            }
        }
    }

    @TestFactory
    fun `recents-count reversal is correct for various counts`(): List<DynamicTest> {
        val items = (1..20).map { makeLc(it.toLong(), "item_$it") }
        return (1..20).map { count ->
            dynamicTest("recents reversal correctness for count=$count") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecentShort(count)
                assertEquals("item_20", result[0])
                if (count > 1) {
                    assertEquals("item_19", result[1])
                }
            }
        }
    }

    @TestFactory
    fun `recents-count returns strings for various counts`(): List<DynamicTest> =
        (1..30).map { count ->
            dynamicTest("recents/$count returns strings") {
                val items = (1..50).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecentShort(count)
                result.forEach { assertTrue(it is String) }
            }
        }

    // ===== RECENT/{COUNT} ENDPOINT =====

    @Test
    fun `recent with count returns full entities reversed`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.getLearningItemRecent(2)
        assertEquals(2, result.size)
        assertEquals(5L, result[0].id)
        assertEquals(4L, result[1].id)
    }

    @Test
    fun `recent with count 0 returns empty`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.getLearningItemRecent(0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `recent with count larger than total returns all reversed`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.getLearningItemRecent(100)
        assertEquals(5, result.size)
        assertEquals(5L, result[0].id)
        assertEquals(1L, result[4].id)
    }

    @Test
    fun `recent preserves entity data after reversal`() {
        val date = Date(12345L)
        val items = listOf(
            makeLc(1, "first", "cat1", date),
            makeLc(2, "second", "cat2", Date(99999L)),
        )
        whenever(lcRepo.findAll()).thenReturn(items)
        val result = controller.getLearningItemRecent(2)
        assertEquals("second", result[0].learning)
        assertEquals("cat2", result[0].category)
        assertEquals("first", result[1].learning)
        assertEquals("cat1", result[1].category)
        assertEquals(date, result[1].dateAdded)
    }

    @TestFactory
    fun `recent-count with counts 0 to 100`(): List<DynamicTest> {
        val items = (1..50).map { makeLc(it.toLong(), "item_$it", date = Date(it.toLong())) }
        return (0..100).map { count ->
            dynamicTest("recent with count=$count") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(count)
                assertEquals(minOf(count, 50), result.size)
            }
        }
    }

    @TestFactory
    fun `recent-count preserves all entity fields`(): List<DynamicTest> =
        (1..30).map { count ->
            dynamicTest("recent/$count preserves entity fields") {
                val items = (1..30).map {
                    makeLc(it.toLong(), "learning_$it", "cat_$it", Date(it.toLong() * 1000))
                }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(count)
                result.forEach { entity ->
                    assertTrue(entity.learning.startsWith("learning_"))
                    assertTrue(entity.category.startsWith("cat_"))
                    assertTrue(entity.id > 0)
                }
            }
        }

    @TestFactory
    fun `recent-count reversal order is correct`(): List<DynamicTest> {
        val items = (1..20).map { makeLc(it.toLong(), "item_$it") }
        return (1..20).map { count ->
            dynamicTest("recent reversal for count=$count") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemRecent(count)
                assertEquals(20L, result[0].id)
                assertEquals(minOf(count, 20), result.size)
            }
        }
    }

    // ===== RANDOMS ENDPOINT =====

    @Test
    fun `randoms returns a single learning string`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.learningItemCountShort()
        assertTrue(sampleItems.map { it.learning }.contains(result))
    }

    @Test
    fun `randoms with single item returns that item`() {
        val single = listOf(makeLc(1, "only item"))
        whenever(lcRepo.findAll()).thenReturn(single)
        val result = controller.learningItemCountShort()
        assertEquals("only item", result)
    }

    @TestFactory
    fun `randoms always returns item from repo`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("randoms iteration $i returns valid item") {
                whenever(lcRepo.findAll()).thenReturn(sampleItems)
                val result = controller.learningItemCountShort()
                assertTrue(sampleItems.map { it.learning }.contains(result))
            }
        }

    @TestFactory
    fun `randoms with various repo sizes`(): List<DynamicTest> =
        (1..50).map { size ->
            dynamicTest("randoms with $size items") {
                val items = (1..size).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItemCountShort()
                assertTrue(items.map { it.learning }.contains(result))
            }
        }

    // ===== RANDOMS/{COUNT} ENDPOINT =====

    @Test
    fun `randoms with count returns correct number`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.getLearningItemCountShort(3)
        assertEquals(3, result.size)
    }

    @Test
    fun `randoms with count 0 returns empty`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.getLearningItemCountShort(0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `randoms with count larger than total returns all`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.getLearningItemCountShort(100)
        assertEquals(5, result.size)
    }

    @TestFactory
    fun `randoms-count with counts 0 to 100`(): List<DynamicTest> {
        val items = (1..50).map { makeLc(it.toLong(), "item_$it") }
        return (0..100).map { count ->
            dynamicTest("randoms with count=$count") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCountShort(count)
                assertEquals(minOf(count, 50), result.size)
            }
        }
    }

    @TestFactory
    fun `randoms-count returns strings from repo`(): List<DynamicTest> =
        (1..50).map { count ->
            dynamicTest("randoms/$count returns valid strings") {
                val items = (1..50).map { makeLc(it.toLong(), "item_$it") }
                val allLearnings = items.map { it.learning }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCountShort(count)
                result.forEach { assertTrue(allLearnings.contains(it)) }
            }
        }

    @TestFactory
    fun `randoms-count returns strings not entities`(): List<DynamicTest> =
        (1..30).map { count ->
            dynamicTest("randoms/$count returns strings") {
                val items = (1..30).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCountShort(count)
                result.forEach { assertTrue(it is String) }
            }
        }

    // ===== RANDOM/{COUNT} ENDPOINT =====

    @Test
    fun `random with count returns full entities`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.getLearningItemCount(3)
        assertEquals(3, result.size)
    }

    @Test
    fun `random with count 0 returns empty`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.getLearningItemCount(0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `random with count returns items from repo`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val allIds = sampleItems.map { it.id }
        val result = controller.getLearningItemCount(5)
        result.forEach { assertTrue(allIds.contains(it.id)) }
    }

    @Test
    fun `random with count larger than total returns all`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.getLearningItemCount(100)
        assertEquals(5, result.size)
    }

    @TestFactory
    fun `random-count with counts 0 to 100`(): List<DynamicTest> {
        val items = (1..50).map { makeLc(it.toLong(), "item_$it") }
        return (0..100).map { count ->
            dynamicTest("random with count=$count") {
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCount(count)
                assertEquals(minOf(count, 50), result.size)
            }
        }
    }

    @TestFactory
    fun `random-count returns valid entities`(): List<DynamicTest> =
        (1..50).map { count ->
            dynamicTest("random/$count returns valid entities") {
                val items = (1..50).map { makeLc(it.toLong(), "item_$it", "cat_$it", Date(it.toLong())) }
                val allIds = items.map { it.id }.toSet()
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCount(count)
                result.forEach { assertTrue(allIds.contains(it.id)) }
            }
        }

    @TestFactory
    fun `random-count preserves entity data`(): List<DynamicTest> =
        (1..30).map { count ->
            dynamicTest("random/$count preserves data") {
                val items = (1..30).map {
                    makeLc(it.toLong(), "learning_$it", "cat_$it", Date(it.toLong() * 1000))
                }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.getLearningItemCount(count)
                result.forEach { entity ->
                    assertTrue(entity.learning.startsWith("learning_"))
                    assertTrue(entity.category.startsWith("cat_"))
                    assertTrue(entity.dateAdded.time > 0)
                }
            }
        }

    // ===== RANDOM ENDPOINT (single) =====

    @Test
    fun `random returns a single entity`() {
        whenever(lcRepo.findAll()).thenReturn(sampleItems)
        val result = controller.learningItem()
        assertTrue(sampleItems.map { it.id }.contains(result.id))
    }

    @Test
    fun `random with single item returns that item`() {
        val single = listOf(makeLc(42, "sole item"))
        whenever(lcRepo.findAll()).thenReturn(single)
        val result = controller.learningItem()
        assertEquals(42L, result.id)
        assertEquals("sole item", result.learning)
    }

    @TestFactory
    fun `random always returns item from repo`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("random iteration $i returns valid entity") {
                whenever(lcRepo.findAll()).thenReturn(sampleItems)
                val result = controller.learningItem()
                assertTrue(sampleItems.map { it.id }.contains(result.id))
            }
        }

    @TestFactory
    fun `random with various repo sizes`(): List<DynamicTest> =
        (1..50).map { size ->
            dynamicTest("random with $size items") {
                val items = (1..size).map { makeLc(it.toLong(), "item_$it") }
                whenever(lcRepo.findAll()).thenReturn(items)
                val result = controller.learningItem()
                assertTrue(items.map { it.id }.contains(result.id))
            }
        }

    @TestFactory
    fun `random preserves entity data for various items`(): List<DynamicTest> =
        (1..50).map { i ->
            dynamicTest("random preserves data for item $i") {
                val item = makeLc(i.toLong(), "learning_$i", "cat_$i", Date(i.toLong() * 1000))
                whenever(lcRepo.findAll()).thenReturn(listOf(item))
                val result = controller.learningItem()
                assertEquals(i.toLong(), result.id)
                assertEquals("learning_$i", result.learning)
                assertEquals("cat_$i", result.category)
                assertEquals(i.toLong() * 1000, result.dateAdded.time)
            }
        }

    // ===== DUMP ENDPOINT =====

    @Test
    fun `getDump accesses response writer`() {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        val response = mock<HttpServletResponse>()
        whenever(response.writer).thenReturn(printWriter)
        try {
            controller.getDump(response)
        } catch (_: Exception) {
        }
        verify(response, atLeastOnce()).writer
    }

    // ===== REPO INTERACTION VERIFICATION =====

    @TestFactory
    fun `add calls save for each invocation`(): List<DynamicTest> =
        (1..50).map { i ->
            dynamicTest("add call #$i invokes save") {
                whenever(lcRepo.save(any<LearningCollection>())).thenAnswer { it.arguments[0] }
                controller.add("item_$i")
                verify(lcRepo).save(argThat<LearningCollection> { learning == "item_$i" })
            }
        }

    @TestFactory
    fun `filter calls findAllByLearningContaining with exact query`(): List<DynamicTest> {
        val queries = (1..50).map { "query_$it" }
        return queries.map { query ->
            dynamicTest("filter passes '$query' to repo") {
                whenever(lcRepo.findAllByLearningContaining(query)).thenReturn(emptyList())
                controller.filterLearn(query)
                verify(lcRepo).findAllByLearningContaining(query)
            }
        }
    }
}
