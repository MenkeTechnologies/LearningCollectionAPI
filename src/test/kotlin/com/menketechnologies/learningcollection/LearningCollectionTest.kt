package com.menketechnologies.learningcollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import java.util.stream.Stream

class LearningCollectionTest {

    companion object {
        private val UNICODE_BLOCKS = listOf(
            "Basic Latin" to (0x0020..0x007E),
            "Latin-1 Supplement" to (0x00A0..0x00FF),
            "Latin Extended-A" to (0x0100..0x017F),
            "Latin Extended-B" to (0x0180..0x024F),
            "Greek and Coptic" to (0x0370..0x03FF),
            "Cyrillic" to (0x0400..0x04FF),
            "Armenian" to (0x0530..0x058F),
            "Hebrew" to (0x0590..0x05FF),
            "Arabic" to (0x0600..0x06FF),
            "Devanagari" to (0x0900..0x097F),
            "Thai" to (0x0E00..0x0E7F),
            "Georgian" to (0x10A0..0x10FF),
            "Hangul Jamo" to (0x1100..0x11FF),
            "CJK Unified Ideographs (sample)" to (0x4E00..0x4E50),
            "Hiragana" to (0x3040..0x309F),
            "Katakana" to (0x30A0..0x30FF),
        )

        private val SPECIAL_STRINGS = listOf(
            "", " ", "  ", "\t", "\n", "\r\n", "\t\n\r",
            "a", "ab", "abc",
            "<html>", "</html>", "<script>alert(1)</script>",
            "&amp;", "&lt;", "&gt;", "&quot;",
            "'", "\"", "\\", "/", "//", "/*", "*/",
            "SELECT * FROM users", "DROP TABLE users;--",
            "Robert'); DROP TABLE Students;--",
            "\u0000", "\u0001", "\uFFFF",
            "null", "NULL", "None", "nil", "undefined",
            "true", "false", "True", "False",
            "0", "-1", "1", "999999999", "-999999999",
            "1.0", "-1.0", "NaN", "Infinity", "-Infinity",
            "#{}", "\${}", "{{template}}",
            "%s", "%d", "%n", "%00",
            "C:\\Windows\\System32", "/etc/passwd",
            "http://example.com", "ftp://evil.com",
            "data:text/html,<h1>hi</h1>",
            "javascript:alert(1)",
            " leading", "trailing ", " both ",
            "emoji 😀🎉🚀💻🔥",
            "中文测试", "日本語テスト", "한국어테스트",
            "العربية", "עברית", "हिन्दी", "ไทย",
            "Ṫḧïṡ ïṡ ä ẗëṡẗ",
            "ⒸⒾⓇⒸⓁⒺⒹ",
            "𝕳𝖊𝖑𝖑𝖔",
            "a".repeat(100),
            "b".repeat(1000),
            "c".repeat(5000),
            "d".repeat(10000),
            "line1\nline2\nline3",
            "col1\tcol2\tcol3",
            "a\u0000b\u0000c",
            "\uD83D\uDE00", // surrogate pair emoji
        )

        private val CATEGORIES = listOf(
            "programming", "science", "math", "history", "art",
            "music", "literature", "philosophy", "economics", "biology",
            "chemistry", "physics", "engineering", "medicine", "law",
            "psychology", "sociology", "anthropology", "linguistics", "geography",
            "astronomy", "geology", "ecology", "genetics", "neuroscience",
            "robotics", "AI", "machine-learning", "deep-learning", "NLP",
            "computer-vision", "databases", "networking", "security", "devops",
            "frontend", "backend", "fullstack", "mobile", "embedded",
            "kotlin", "java", "python", "rust", "go",
            "typescript", "javascript", "c++", "c#", "swift",
        )

        @JvmStatic
        fun learningStringProvider(): Stream<String> = SPECIAL_STRINGS.stream()

        @JvmStatic
        fun categoryProvider(): Stream<String> = CATEGORIES.stream()

        @JvmStatic
        fun idProvider(): Stream<Long> = listOf(
            0L, 1L, 2L, -1L, Long.MAX_VALUE, Long.MIN_VALUE,
            100L, 1000L, 999999L, 42L, 7L, 13L, 256L, 512L, 1024L,
            Int.MAX_VALUE.toLong(), Int.MIN_VALUE.toLong(),
            Int.MAX_VALUE.toLong() + 1, Int.MIN_VALUE.toLong() - 1,
        ).stream()

        @JvmStatic
        fun dateProvider(): Stream<Date> = listOf(
            Date(0L),
            Date(1L),
            Date(-1L),
            Date(Long.MAX_VALUE),
            Date(1000000000000L),
            Date(946684800000L), // 2000-01-01
            Date(1609459200000L), // 2021-01-01
            Date(253402300799000L), // 9999-12-31
            Date(86400000L), // 1970-01-02
            Date(),
        ).stream()
    }

    // --- Basic constructor tests ---

    @Test
    fun `constructor sets all fields`() {
        val date = Date()
        val lc = LearningCollection("learn kotlin", "programming", date)
        assertEquals("learn kotlin", lc.learning)
        assertEquals("programming", lc.category)
        assertEquals(date, lc.dateAdded)
        assertEquals(0L, lc.id)
    }

    @Test
    fun `no-arg constructor creates instance`() {
        val lc = LearningCollection()
        assertEquals(0L, lc.id)
    }

    @Test
    fun `entity implements Serializable`() {
        val lc = LearningCollection("test", "cat", Date())
        assertTrue(lc is java.io.Serializable)
    }

    @Test
    fun `default id is zero`() {
        val lc = LearningCollection("test", "cat", Date())
        assertEquals(0L, lc.id)
    }

    // --- Parameterized learning string tests ---

    @ParameterizedTest(name = "constructor accepts learning string: {0}")
    @MethodSource("learningStringProvider")
    fun `constructor accepts various learning strings`(input: String) {
        val lc = LearningCollection(input, "cat", Date())
        assertEquals(input, lc.learning)
    }

    @ParameterizedTest(name = "learning field setter accepts: {0}")
    @MethodSource("learningStringProvider")
    fun `learning field setter accepts various strings`(input: String) {
        val lc = LearningCollection()
        lc.learning = input
        assertEquals(input, lc.learning)
    }

    // --- Parameterized category tests ---

    @ParameterizedTest(name = "constructor accepts category: {0}")
    @MethodSource("categoryProvider")
    fun `constructor accepts various categories`(cat: String) {
        val lc = LearningCollection("test", cat, Date())
        assertEquals(cat, lc.category)
    }

    @ParameterizedTest(name = "category field setter accepts: {0}")
    @MethodSource("categoryProvider")
    fun `category field setter accepts various strings`(cat: String) {
        val lc = LearningCollection()
        lc.category = cat
        assertEquals(cat, lc.category)
    }

    // --- Parameterized ID tests ---

    @ParameterizedTest(name = "id can be set to: {0}")
    @MethodSource("idProvider")
    fun `id can be set to various values`(id: Long) {
        val lc = LearningCollection()
        lc.id = id
        assertEquals(id, lc.id)
    }

    // --- Parameterized date tests ---

    @ParameterizedTest(name = "dateAdded can be set to various dates")
    @MethodSource("dateProvider")
    fun `dateAdded field accepts various dates`(date: Date) {
        val lc = LearningCollection("test", "cat", date)
        assertEquals(date, lc.dateAdded)
    }

    @ParameterizedTest(name = "dateAdded setter accepts various dates")
    @MethodSource("dateProvider")
    fun `dateAdded setter accepts various dates`(date: Date) {
        val lc = LearningCollection()
        lc.dateAdded = date
        assertEquals(date, lc.dateAdded)
    }

    // --- Dynamic tests for Unicode blocks ---

    @TestFactory
    fun `constructor accepts strings from various Unicode blocks`(): List<DynamicTest> =
        UNICODE_BLOCKS.map { (name, range) ->
            dynamicTest("constructor accepts $name characters") {
                val str = range.take(10).map { it.toChar() }.joinToString("")
                val lc = LearningCollection(str, "unicode-test", Date())
                assertEquals(str, lc.learning)
            }
        }

    @TestFactory
    fun `category accepts strings from various Unicode blocks`(): List<DynamicTest> =
        UNICODE_BLOCKS.map { (name, range) ->
            dynamicTest("category accepts $name characters") {
                val str = range.take(10).map { it.toChar() }.joinToString("")
                val lc = LearningCollection("test", str, Date())
                assertEquals(str, lc.category)
            }
        }

    // --- Dynamic tests for string lengths ---

    @TestFactory
    fun `constructor accepts strings of various lengths for learning`(): List<DynamicTest> =
        (0..200).map { len ->
            dynamicTest("learning string of length $len") {
                val str = "x".repeat(len)
                val lc = LearningCollection(str, "cat", Date())
                assertEquals(str, lc.learning)
                assertEquals(len, lc.learning.length)
            }
        }

    @TestFactory
    fun `constructor accepts strings of various lengths for category`(): List<DynamicTest> =
        (0..200).map { len ->
            dynamicTest("category string of length $len") {
                val str = "y".repeat(len)
                val lc = LearningCollection("test", str, Date())
                assertEquals(str, lc.category)
                assertEquals(len, lc.category.length)
            }
        }

    // --- Dynamic tests for ID ranges ---

    @TestFactory
    fun `id accepts values in positive range`(): List<DynamicTest> =
        (0L..200L).map { id ->
            dynamicTest("id = $id") {
                val lc = LearningCollection()
                lc.id = id
                assertEquals(id, lc.id)
            }
        }

    @TestFactory
    fun `id accepts negative values`(): List<DynamicTest> =
        (-200L..-1L).map { id ->
            dynamicTest("id = $id") {
                val lc = LearningCollection()
                lc.id = id
                assertEquals(id, lc.id)
            }
        }

    @TestFactory
    fun `id accepts large values`(): List<DynamicTest> =
        (1..100).map { i ->
            val id = i.toLong() * 1_000_000L
            dynamicTest("id = $id") {
                val lc = LearningCollection()
                lc.id = id
                assertEquals(id, lc.id)
            }
        }

    // --- Dynamic tests for date epochs ---

    @TestFactory
    fun `dateAdded accepts dates at various epochs`(): List<DynamicTest> =
        (-50..50).map { dayOffset ->
            val ms = dayOffset.toLong() * 86400000L
            dynamicTest("date at epoch offset $dayOffset days") {
                val date = Date(ms)
                val lc = LearningCollection("test", "cat", date)
                assertEquals(date, lc.dateAdded)
                assertEquals(ms, lc.dateAdded.time)
            }
        }

    @TestFactory
    fun `dateAdded accepts dates in year range`(): List<DynamicTest> =
        (1970..2070).map { year ->
            dynamicTest("date in year $year") {
                val cal = Calendar.getInstance()
                cal.set(year, 0, 1, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val date = cal.time
                val lc = LearningCollection("test", "cat", date)
                assertEquals(date, lc.dateAdded)
            }
        }

    // --- Mutability tests ---

    @TestFactory
    fun `learning field can be reassigned multiple times`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("reassign learning field iteration $i") {
                val lc = LearningCollection("initial", "cat", Date())
                val newVal = "value_$i"
                lc.learning = newVal
                assertEquals(newVal, lc.learning)
            }
        }

    @TestFactory
    fun `category field can be reassigned multiple times`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("reassign category field iteration $i") {
                val lc = LearningCollection("test", "initial", Date())
                val newVal = "cat_$i"
                lc.category = newVal
                assertEquals(newVal, lc.category)
            }
        }

    @TestFactory
    fun `id field can be reassigned multiple times`(): List<DynamicTest> =
        (1L..100L).map { i ->
            dynamicTest("reassign id field to $i") {
                val lc = LearningCollection()
                lc.id = i
                assertEquals(i, lc.id)
                lc.id = i * 2
                assertEquals(i * 2, lc.id)
            }
        }

    @TestFactory
    fun `dateAdded field can be reassigned multiple times`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("reassign dateAdded iteration $i") {
                val lc = LearningCollection("test", "cat", Date(0))
                val newDate = Date(i.toLong() * 1000)
                lc.dateAdded = newDate
                assertEquals(newDate, lc.dateAdded)
            }
        }

    // --- Instance independence tests ---

    @TestFactory
    fun `multiple instances are independent`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("independence test $i") {
                val lc1 = LearningCollection("learning_$i", "cat_$i", Date(i.toLong()))
                val lc2 = LearningCollection("other_$i", "other_$i", Date(i.toLong() + 1000))
                lc1.id = i.toLong()
                lc2.id = i.toLong() + 1000

                assertNotEquals(lc1.learning, lc2.learning)
                assertNotEquals(lc1.category, lc2.category)
                assertNotEquals(lc1.id, lc2.id)
                assertNotEquals(lc1.dateAdded, lc2.dateAdded)
            }
        }

    // --- Serialization tests ---

    @TestFactory
    fun `entity can be serialized and deserialized with various data`(): List<DynamicTest> =
        CATEGORIES.mapIndexed { i, cat ->
            dynamicTest("serialize with category $cat") {
                val original = LearningCollection("learning_$i", cat, Date(i.toLong() * 1000))
                original.id = i.toLong()

                val baos = ByteArrayOutputStream()
                ObjectOutputStream(baos).use { it.writeObject(original) }
                val bytes = baos.toByteArray()

                val restored = ObjectInputStream(ByteArrayInputStream(bytes)).use {
                    it.readObject()
                } as LearningCollection

                assertEquals(original.id, restored.id)
                assertEquals(original.learning, restored.learning)
                assertEquals(original.category, restored.category)
                assertEquals(original.dateAdded, restored.dateAdded)
            }
        }

    @TestFactory
    fun `entity serialization roundtrip with special strings`(): List<DynamicTest> =
        SPECIAL_STRINGS.filter { it.isNotEmpty() }.mapIndexed { i, str ->
            dynamicTest("serialize with special string index $i") {
                val original = LearningCollection(str, "cat", Date())
                original.id = i.toLong()

                val baos = ByteArrayOutputStream()
                ObjectOutputStream(baos).use { it.writeObject(original) }

                val restored = ObjectInputStream(ByteArrayInputStream(baos.toByteArray())).use {
                    it.readObject()
                } as LearningCollection

                assertEquals(original.learning, restored.learning)
            }
        }

    // --- Cross-product tests: learning × category combinations ---

    @TestFactory
    fun `constructor with learning-category combinations`(): List<DynamicTest> {
        val learnings = listOf(
            "kotlin basics", "spring boot", "docker compose", "unit testing",
            "REST API", "JPA entities", "CORS config", "gradle build",
            "git workflow", "CI/CD pipeline"
        )
        return learnings.flatMap { learning ->
            CATEGORIES.take(20).map { category ->
                dynamicTest("learning='$learning', category='$category'") {
                    val lc = LearningCollection(learning, category, Date())
                    assertEquals(learning, lc.learning)
                    assertEquals(category, lc.category)
                }
            }
        }
    }

    // --- Whitespace handling ---

    @TestFactory
    fun `learning field preserves whitespace variations`(): List<DynamicTest> {
        val whitespaceVariations = (1..50).flatMap { n ->
            listOf(
                " ".repeat(n) + "text",
                "text" + " ".repeat(n),
                " ".repeat(n) + "text" + " ".repeat(n),
                "\t".repeat(n) + "text",
                "text" + "\t".repeat(n),
            )
        }
        return whitespaceVariations.mapIndexed { i, str ->
            dynamicTest("whitespace variation $i") {
                val lc = LearningCollection(str, "cat", Date())
                assertEquals(str, lc.learning)
            }
        }
    }

    // --- Repetitive character tests ---

    @TestFactory
    fun `learning with repeated characters`(): List<DynamicTest> {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf(' ', '.', ',', '!', '?', '-', '_')
        return chars.flatMap { c ->
            listOf(1, 10, 100).map { len ->
                dynamicTest("char '$c' repeated $len times") {
                    val str = c.toString().repeat(len)
                    val lc = LearningCollection(str, "cat", Date())
                    assertEquals(str, lc.learning)
                    assertEquals(len, lc.learning.length)
                }
            }
        }
    }

    // --- Pattern strings ---

    @TestFactory
    fun `learning with pattern strings`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("pattern string $i") {
                val pattern = (1..i).joinToString("-") { "item$it" }
                val lc = LearningCollection(pattern, "cat", Date())
                assertEquals(pattern, lc.learning)
            }
        }

    // --- No-arg constructor + field assignment combinations ---

    @TestFactory
    fun `no-arg constructor then set all fields`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("set all fields via no-arg constructor iteration $i") {
                val lc = LearningCollection()
                lc.id = i.toLong()
                lc.learning = "learning_$i"
                lc.category = "category_$i"
                lc.dateAdded = Date(i.toLong() * 86400000)

                assertEquals(i.toLong(), lc.id)
                assertEquals("learning_$i", lc.learning)
                assertEquals("category_$i", lc.category)
                assertEquals(i.toLong() * 86400000, lc.dateAdded.time)
            }
        }

    // --- Date mutation tests ---

    @TestFactory
    fun `date object mutation does not affect entity if reassigned`(): List<DynamicTest> =
        (1..50).map { i ->
            dynamicTest("date reassignment isolation $i") {
                val date1 = Date(i.toLong() * 1000)
                val lc = LearningCollection("test", "cat", date1)
                val date2 = Date(i.toLong() * 2000)
                lc.dateAdded = date2
                assertEquals(date2, lc.dateAdded)
                assertNotEquals(date1.time, lc.dateAdded.time)
            }
        }

    // --- Equality is reference-based (no equals override) ---

    @TestFactory
    fun `two entities with same data are different object references`(): List<DynamicTest> =
        (1..50).map { i ->
            dynamicTest("reference equality $i") {
                val date = Date(i.toLong())
                val lc1 = LearningCollection("same", "same", date)
                val lc2 = LearningCollection("same", "same", date)
                assertNotSame(lc1, lc2)
            }
        }

    // --- Numeric string learnings ---

    @TestFactory
    fun `learning with numeric strings`(): List<DynamicTest> =
        (-100..100).map { n ->
            dynamicTest("numeric string learning $n") {
                val lc = LearningCollection(n.toString(), "cat", Date())
                assertEquals(n.toString(), lc.learning)
            }
        }

    // --- CSV-based parameterized tests ---

    @ParameterizedTest(name = "learning={0}, category={1}")
    @CsvSource(
        "hello, world",
        "foo, bar",
        "kotlin, jvm",
        "spring, boot",
        "unit, test",
        "REST, API",
        "docker, container",
        "gradle, build",
        "git, repo",
        "CI, CD",
        "JPA, entity",
        "CORS, config",
        "JSON, parse",
        "HTTP, request",
        "SQL, query",
        "ORM, mapping",
        "MVC, pattern",
        "DI, injection",
        "AOP, aspect",
        "JWT, token",
    )
    fun `constructor with CSV learning-category pairs`(learning: String, category: String) {
        val lc = LearningCollection(learning, category, Date())
        assertEquals(learning, lc.learning)
        assertEquals(category, lc.category)
    }

    // --- Value source parameterized ---

    @ParameterizedTest(name = "id value: {0}")
    @ValueSource(longs = [0, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765])
    fun `id accepts fibonacci values`(id: Long) {
        val lc = LearningCollection()
        lc.id = id
        assertEquals(id, lc.id)
    }

    @ParameterizedTest(name = "id power of 2: {0}")
    @ValueSource(longs = [1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536])
    fun `id accepts powers of two`(id: Long) {
        val lc = LearningCollection()
        lc.id = id
        assertEquals(id, lc.id)
    }

    // --- Large batch creation ---

    @TestFactory
    fun `create many entities in sequence`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("create entity #$i") {
                val lc = LearningCollection("learning number $i", "category ${i % 10}", Date(i.toLong()))
                lc.id = i.toLong()
                assertEquals("learning number $i", lc.learning)
                assertEquals("category ${i % 10}", lc.category)
                assertEquals(i.toLong(), lc.id)
                assertEquals(i.toLong(), lc.dateAdded.time)
            }
        }
}
