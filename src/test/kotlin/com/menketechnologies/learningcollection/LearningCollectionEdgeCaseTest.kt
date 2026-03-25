package com.menketechnologies.learningcollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

/**
 * Edge-case and boundary tests for LearningCollection entity.
 * Covers extreme values, unusual inputs, and corner cases.
 */
class LearningCollectionEdgeCaseTest {

    // ===== REPEATED CHARACTER STRINGS 1..500 =====

    @TestFactory
    fun `learning with repeated single char a length 1 to 500`(): List<DynamicTest> =
        (1..500).map { len ->
            dynamicTest("repeated 'a' length $len") {
                val s = "a".repeat(len)
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
                assertEquals(len, lc.learning.length)
            }
        }

    @TestFactory
    fun `learning with repeated space length 1 to 300`(): List<DynamicTest> =
        (1..300).map { len ->
            dynamicTest("repeated space length $len") {
                val s = " ".repeat(len)
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
                assertEquals(len, lc.learning.length)
            }
        }

    @TestFactory
    fun `learning with repeated tab length 1 to 200`(): List<DynamicTest> =
        (1..200).map { len ->
            dynamicTest("repeated tab length $len") {
                val s = "\t".repeat(len)
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
            }
        }

    @TestFactory
    fun `learning with repeated newline length 1 to 200`(): List<DynamicTest> =
        (1..200).map { len ->
            dynamicTest("repeated newline length $len") {
                val s = "\n".repeat(len)
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
            }
        }

    // ===== CATEGORY REPEATED CHARS 1..500 =====

    @TestFactory
    fun `category with repeated z length 1 to 500`(): List<DynamicTest> =
        (1..500).map { len ->
            dynamicTest("category repeated 'z' length $len") {
                val s = "z".repeat(len)
                val lc = LearningCollection("test", s, Date())
                assertEquals(s, lc.category)
                assertEquals(len, lc.category.length)
            }
        }

    // ===== ID EXTREMES =====

    @TestFactory
    fun `id values across full long range`(): List<DynamicTest> {
        val ids = (-1000L..1000L).toList() +
            listOf(Long.MIN_VALUE, Long.MAX_VALUE, Long.MIN_VALUE + 1, Long.MAX_VALUE - 1) +
            (1..30).map { 1L shl it } +
            (1..30).map { -(1L shl it) }
        return ids.distinct().map { id ->
            dynamicTest("id = $id") {
                val lc = LearningCollection("test", "cat", Date())
                lc.id = id
                assertEquals(id, lc.id)
            }
        }
    }

    // ===== DATE EXTREMES =====

    @TestFactory
    fun `dates across epoch millis sweep -1000 to 1000`(): List<DynamicTest> =
        (-1000L..1000L).map { ms ->
            dynamicTest("date at epoch ms=$ms") {
                val d = Date(ms)
                val lc = LearningCollection("test", "cat", d)
                assertEquals(ms, lc.dateAdded.time)
            }
        }

    @TestFactory
    fun `dates at powers of 10 millis`(): List<DynamicTest> =
        (0..15).map { exp ->
            val ms = Math.pow(10.0, exp.toDouble()).toLong()
            dynamicTest("date at 10^$exp = $ms ms") {
                val lc = LearningCollection("test", "cat", Date(ms))
                assertEquals(ms, lc.dateAdded.time)
            }
        }

    @TestFactory
    fun `dates at negative powers of 10`(): List<DynamicTest> =
        (0..15).map { exp ->
            val ms = -Math.pow(10.0, exp.toDouble()).toLong()
            dynamicTest("date at -10^$exp = $ms ms") {
                val lc = LearningCollection("test", "cat", Date(ms))
                assertEquals(ms, lc.dateAdded.time)
            }
        }

    // ===== UNICODE CODEPOINT SWEEP =====

    @TestFactory
    fun `learning with unicode codepoints 0x0020 to 0x0500`(): List<DynamicTest> =
        (0x0020..0x0500).map { cp ->
            dynamicTest("learning codepoint U+${cp.toString(16).uppercase().padStart(4, '0')}") {
                val s = cp.toChar().toString()
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
            }
        }

    @TestFactory
    fun `category with unicode codepoints 0x0020 to 0x0500`(): List<DynamicTest> =
        (0x0020..0x0500).map { cp ->
            dynamicTest("category codepoint U+${cp.toString(16).uppercase().padStart(4, '0')}") {
                val s = cp.toChar().toString()
                val lc = LearningCollection("test", s, Date())
                assertEquals(s, lc.category)
            }
        }

    // ===== CJK EXTENDED CODEPOINTS =====

    @TestFactory
    fun `learning with CJK codepoints 0x4E00 to 0x4FFF`(): List<DynamicTest> =
        (0x4E00..0x4FFF).map { cp ->
            dynamicTest("CJK learning U+${cp.toString(16).uppercase()}") {
                val s = cp.toChar().toString()
                val lc = LearningCollection(s, "cjk", Date())
                assertEquals(s, lc.learning)
            }
        }

    // ===== MIXED CONTENT PATTERNS =====

    @TestFactory
    fun `learning with alternating char patterns length 1 to 200`(): List<DynamicTest> =
        (1..200).map { len ->
            dynamicTest("alternating ab pattern len=$len") {
                val s = (1..len).map { if (it % 2 == 0) 'a' else 'b' }.joinToString("")
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
                assertEquals(len, lc.learning.length)
            }
        }

    @TestFactory
    fun `learning with digit patterns 0 to 300`(): List<DynamicTest> =
        (0..300).map { n ->
            dynamicTest("digit string '$n'") {
                val s = n.toString()
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
            }
        }

    // ===== FIELD REASSIGNMENT SWEEPS =====

    @TestFactory
    fun `reassign learning 200 times per instance`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("reassign learning iteration $i") {
                val lc = LearningCollection("initial", "cat", Date())
                for (j in 1..i) {
                    lc.learning = "value_$j"
                }
                assertEquals("value_$i", lc.learning)
            }
        }

    @TestFactory
    fun `reassign category 200 times per instance`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("reassign category iteration $i") {
                val lc = LearningCollection("test", "initial", Date())
                for (j in 1..i) {
                    lc.category = "cat_$j"
                }
                assertEquals("cat_$i", lc.category)
            }
        }

    @TestFactory
    fun `reassign id 200 times per instance`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("reassign id iteration $i") {
                val lc = LearningCollection("test", "cat", Date())
                for (j in 1..i) {
                    lc.id = j.toLong()
                }
                assertEquals(i.toLong(), lc.id)
            }
        }

    @TestFactory
    fun `reassign dateAdded 200 times per instance`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("reassign dateAdded iteration $i") {
                val lc = LearningCollection("test", "cat", Date(0))
                for (j in 1..i) {
                    lc.dateAdded = Date(j.toLong() * 1000)
                }
                assertEquals(i.toLong() * 1000, lc.dateAdded.time)
            }
        }

    // ===== SERIALIZATION WITH VARIOUS LEARNING LENGTHS =====

    @TestFactory
    fun `serialization roundtrip with learning length 1 to 300`(): List<DynamicTest> =
        (1..300).map { len ->
            dynamicTest("serialization roundtrip learning len=$len") {
                val s = "x".repeat(len)
                val lc = LearningCollection(s, "cat", Date(12345))
                lc.id = len.toLong()
                val baos = ByteArrayOutputStream()
                ObjectOutputStream(baos).use { it.writeObject(lc) }
                val bytes = baos.toByteArray()
                val restored = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() } as LearningCollection
                assertEquals(s, restored.learning)
                assertEquals("cat", restored.category)
                assertEquals(12345L, restored.dateAdded.time)
                assertEquals(len.toLong(), restored.id)
            }
        }

    @TestFactory
    fun `serialization roundtrip with category length 1 to 300`(): List<DynamicTest> =
        (1..300).map { len ->
            dynamicTest("serialization roundtrip category len=$len") {
                val s = "y".repeat(len)
                val lc = LearningCollection("test", s, Date(54321))
                lc.id = 42
                val baos = ByteArrayOutputStream()
                ObjectOutputStream(baos).use { it.writeObject(lc) }
                val restored = ObjectInputStream(ByteArrayInputStream(baos.toByteArray())).use { it.readObject() } as LearningCollection
                assertEquals("test", restored.learning)
                assertEquals(s, restored.category)
                assertEquals(54321L, restored.dateAdded.time)
            }
        }

    // ===== INSTANCE INDEPENDENCE =====

    @TestFactory
    fun `instances are independent - 200 pairs`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("independence pair $i") {
                val lc1 = LearningCollection("learn_$i", "cat_$i", Date(i.toLong()))
                val lc2 = LearningCollection("learn_${i + 1000}", "cat_${i + 1000}", Date(i.toLong() + 1000))
                lc1.id = i.toLong()
                lc2.id = i.toLong() + 1000
                assertNotEquals(lc1.learning, lc2.learning)
                assertNotEquals(lc1.category, lc2.category)
                assertNotEquals(lc1.id, lc2.id)
                assertNotEquals(lc1.dateAdded.time, lc2.dateAdded.time)
            }
        }

    // ===== CONSTRUCTOR VS SETTER EQUIVALENCE =====

    @TestFactory
    fun `constructor vs setter equivalence for 200 values`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("constructor vs setter equivalence $i") {
                val learning = "learning_$i"
                val category = "category_$i"
                val date = Date(i.toLong() * 86400000)
                val fromCtor = LearningCollection(learning, category, date)
                fromCtor.id = i.toLong()
                val fromSetter = LearningCollection()
                fromSetter.learning = learning
                fromSetter.category = category
                fromSetter.dateAdded = date
                fromSetter.id = i.toLong()
                assertEquals(fromCtor.learning, fromSetter.learning)
                assertEquals(fromCtor.category, fromSetter.category)
                assertEquals(fromCtor.dateAdded, fromSetter.dateAdded)
                assertEquals(fromCtor.id, fromSetter.id)
            }
        }

    // ===== WHITESPACE VARIANTS =====

    @TestFactory
    fun `learning preserves leading spaces count 1 to 200`(): List<DynamicTest> =
        (1..200).map { n ->
            dynamicTest("leading $n spaces") {
                val s = " ".repeat(n) + "text"
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
                assertTrue(lc.learning.startsWith(" ".repeat(n)))
            }
        }

    @TestFactory
    fun `learning preserves trailing spaces count 1 to 200`(): List<DynamicTest> =
        (1..200).map { n ->
            dynamicTest("trailing $n spaces") {
                val s = "text" + " ".repeat(n)
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
                assertTrue(lc.learning.endsWith(" ".repeat(n)))
            }
        }

    // ===== STRING CONCATENATION PATTERNS =====

    @TestFactory
    fun `learning from concatenated substrings 1 to 200`(): List<DynamicTest> =
        (1..200).map { parts ->
            dynamicTest("$parts concatenated parts") {
                val s = (1..parts).joinToString("-") { "p$it" }
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
                assertEquals(parts, lc.learning.split("-").size)
            }
        }

    // ===== BATCH CREATION =====

    @TestFactory
    fun `batch create 1 to 500 instances`(): List<DynamicTest> =
        listOf(1, 5, 10, 25, 50, 100, 200, 300, 400, 500).map { batchSize ->
            dynamicTest("batch create $batchSize instances") {
                val instances = (1..batchSize).map {
                    LearningCollection("learn_$it", "cat_$it", Date(it.toLong()))
                }
                assertEquals(batchSize, instances.size)
                instances.forEachIndexed { idx, lc ->
                    assertEquals("learn_${idx + 1}", lc.learning)
                    assertEquals("cat_${idx + 1}", lc.category)
                }
            }
        }

    // ===== PALINDROME STRINGS =====

    @TestFactory
    fun `learning with palindrome strings length 1 to 200`(): List<DynamicTest> =
        (1..200).map { len ->
            dynamicTest("palindrome length $len") {
                val half = (1..(len / 2)).map { ('a' + (it % 26)).toChar() }.joinToString("")
                val s = half + (if (len % 2 == 1) "x" else "") + half.reversed()
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
                assertEquals(s, lc.learning.reversed())
            }
        }

    // ===== EMOJI STRINGS =====

    @TestFactory
    fun `learning with repeated emoji 1 to 100`(): List<DynamicTest> =
        (1..100).map { count ->
            dynamicTest("repeated emoji count=$count") {
                val s = "\uD83D\uDE00".repeat(count)
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
            }
        }

    // ===== MIXED CASE PATTERNS =====

    @TestFactory
    fun `learning with alternating case length 1 to 200`(): List<DynamicTest> =
        (1..200).map { len ->
            dynamicTest("alternating case len=$len") {
                val s = (1..len).map { if (it % 2 == 0) 'A' else 'a' }.joinToString("")
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
            }
        }

    // ===== NUMERIC STRING PATTERNS =====

    @TestFactory
    fun `learning with large number strings`(): List<DynamicTest> =
        (1..200).map { digits ->
            dynamicTest("numeric string with $digits digits") {
                val s = (1..digits).map { (it % 10).toString() }.joinToString("")
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
                assertEquals(digits, lc.learning.length)
            }
        }

    // ===== HEX STRING PATTERNS =====

    @TestFactory
    fun `learning with hex strings of various lengths`(): List<DynamicTest> =
        (1..200).map { len ->
            dynamicTest("hex string len=$len") {
                val s = (1..len).map { "0123456789abcdef"[it % 16] }.joinToString("")
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
                assertEquals(len, lc.learning.length)
            }
        }

    // ===== DATE HOUR SWEEP =====

    @TestFactory
    fun `dates at each hour for 30 days`(): List<DynamicTest> =
        (0 until 720).map { hour ->
            dynamicTest("date at hour $hour") {
                val ms = hour.toLong() * 3600000
                val lc = LearningCollection("test", "cat", Date(ms))
                assertEquals(ms, lc.dateAdded.time)
            }
        }

    // ===== DEFAULT CONSTRUCTOR FIELD INIT =====

    @TestFactory
    fun `default constructor then set all fields sweep 1 to 200`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("default ctor set fields $i") {
                val lc = LearningCollection()
                lc.learning = "learn_$i"
                lc.category = "cat_$i"
                lc.dateAdded = Date(i.toLong())
                lc.id = i.toLong()
                assertEquals("learn_$i", lc.learning)
                assertEquals("cat_$i", lc.category)
                assertEquals(i.toLong(), lc.dateAdded.time)
                assertEquals(i.toLong(), lc.id)
            }
        }
}
