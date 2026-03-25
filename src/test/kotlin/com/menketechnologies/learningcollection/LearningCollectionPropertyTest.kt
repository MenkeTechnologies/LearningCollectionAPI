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
 * Property-style tests that verify invariants of LearningCollection
 * across large sets of generated inputs.
 */
class LearningCollectionPropertyTest {

    // ===== LEARNING FIELD: what you set is what you get =====

    @TestFactory
    fun `learning roundtrip - alphabetic strings of length 1 to 200`(): List<DynamicTest> =
        (1..200).map { len ->
            dynamicTest("learning roundtrip alpha len=$len") {
                val s = (1..len).map { ('a' + (it % 26)).toChar() }.joinToString("")
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
                assertEquals(len, lc.learning.length)
            }
        }

    @TestFactory
    fun `learning roundtrip - numeric strings`(): List<DynamicTest> =
        (-500..500).map { n ->
            dynamicTest("learning roundtrip numeric $n") {
                val s = n.toString()
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
            }
        }

    @TestFactory
    fun `learning roundtrip - unicode codepoints`(): List<DynamicTest> =
        (0x0020..0x0200).map { cp ->
            dynamicTest("learning with codepoint U+${cp.toString(16).uppercase().padStart(4, '0')}") {
                val s = cp.toChar().toString()
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
            }
        }

    // ===== CATEGORY FIELD: what you set is what you get =====

    @TestFactory
    fun `category roundtrip - alphabetic strings of length 1 to 200`(): List<DynamicTest> =
        (1..200).map { len ->
            dynamicTest("category roundtrip alpha len=$len") {
                val s = (1..len).map { ('A' + (it % 26)).toChar() }.joinToString("")
                val lc = LearningCollection("test", s, Date())
                assertEquals(s, lc.category)
                assertEquals(len, lc.category.length)
            }
        }

    @TestFactory
    fun `category roundtrip - unicode codepoints`(): List<DynamicTest> =
        (0x0020..0x0200).map { cp ->
            dynamicTest("category with codepoint U+${cp.toString(16).uppercase().padStart(4, '0')}") {
                val s = cp.toChar().toString()
                val lc = LearningCollection("test", s, Date())
                assertEquals(s, lc.category)
            }
        }

    // ===== ID FIELD: set then get =====

    @TestFactory
    fun `id roundtrip - range -500 to 500`(): List<DynamicTest> =
        (-500..500).map { id ->
            dynamicTest("id roundtrip $id") {
                val lc = LearningCollection()
                lc.id = id.toLong()
                assertEquals(id.toLong(), lc.id)
            }
        }

    @TestFactory
    fun `id roundtrip - powers of 10`(): List<DynamicTest> =
        (0..15).map { exp ->
            val id = Math.pow(10.0, exp.toDouble()).toLong()
            dynamicTest("id roundtrip 10^$exp = $id") {
                val lc = LearningCollection()
                lc.id = id
                assertEquals(id, lc.id)
            }
        }

    // ===== DATE FIELD: millisecond precision =====

    @TestFactory
    fun `date roundtrip - millisecond precision`(): List<DynamicTest> =
        (0L..200L).map { ms ->
            dynamicTest("date roundtrip ms=$ms") {
                val date = Date(ms)
                val lc = LearningCollection("test", "cat", date)
                assertEquals(ms, lc.dateAdded.time)
            }
        }

    @TestFactory
    fun `date roundtrip - daily intervals`(): List<DynamicTest> =
        (0..365).map { day ->
            val ms = day.toLong() * 86_400_000L
            dynamicTest("date roundtrip day=$day") {
                val date = Date(ms)
                val lc = LearningCollection("test", "cat", date)
                assertEquals(ms, lc.dateAdded.time)
            }
        }

    @TestFactory
    fun `date roundtrip - hourly intervals`(): List<DynamicTest> =
        (0..200).map { hour ->
            val ms = hour.toLong() * 3_600_000L
            dynamicTest("date roundtrip hour=$hour") {
                val date = Date(ms)
                val lc = LearningCollection("test", "cat", date)
                assertEquals(ms, lc.dateAdded.time)
            }
        }

    // ===== CONSTRUCTOR + SETTER EQUIVALENCE =====

    @TestFactory
    fun `constructor and setter produce equivalent state`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("equivalence test $i") {
                val learning = "learning_$i"
                val category = "cat_$i"
                val date = Date(i.toLong() * 1000)

                val viaConstructor = LearningCollection(learning, category, date)
                viaConstructor.id = i.toLong()

                val viaSetter = LearningCollection()
                viaSetter.id = i.toLong()
                viaSetter.learning = learning
                viaSetter.category = category
                viaSetter.dateAdded = date

                assertEquals(viaConstructor.id, viaSetter.id)
                assertEquals(viaConstructor.learning, viaSetter.learning)
                assertEquals(viaConstructor.category, viaSetter.category)
                assertEquals(viaConstructor.dateAdded, viaSetter.dateAdded)
            }
        }

    // ===== SERIALIZATION ROUNDTRIP =====

    @TestFactory
    fun `serialization roundtrip with ids 1 to 200`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("serialization roundtrip id=$i") {
                val original = LearningCollection("learning_$i", "cat_$i", Date(i.toLong()))
                original.id = i.toLong()

                val baos = ByteArrayOutputStream()
                ObjectOutputStream(baos).use { it.writeObject(original) }
                val restored = ObjectInputStream(ByteArrayInputStream(baos.toByteArray())).use {
                    it.readObject()
                } as LearningCollection

                assertEquals(original.id, restored.id)
                assertEquals(original.learning, restored.learning)
                assertEquals(original.category, restored.category)
                assertEquals(original.dateAdded, restored.dateAdded)
            }
        }

    @TestFactory
    fun `serialization roundtrip with various string lengths`(): List<DynamicTest> =
        (0..100).map { len ->
            dynamicTest("serialization roundtrip strlen=$len") {
                val str = "x".repeat(len)
                val original = LearningCollection(str, str, Date(len.toLong()))
                original.id = len.toLong()

                val baos = ByteArrayOutputStream()
                ObjectOutputStream(baos).use { it.writeObject(original) }
                val restored = ObjectInputStream(ByteArrayInputStream(baos.toByteArray())).use {
                    it.readObject()
                } as LearningCollection

                assertEquals(str, restored.learning)
                assertEquals(str, restored.category)
            }
        }

    // ===== MULTIPLE REASSIGNMENTS =====

    @TestFactory
    fun `learning field survives N reassignments`(): List<DynamicTest> =
        (1..100).map { n ->
            dynamicTest("$n reassignments of learning") {
                val lc = LearningCollection()
                var last = ""
                for (i in 1..n) {
                    last = "value_$i"
                    lc.learning = last
                }
                assertEquals(last, lc.learning)
            }
        }

    @TestFactory
    fun `category field survives N reassignments`(): List<DynamicTest> =
        (1..100).map { n ->
            dynamicTest("$n reassignments of category") {
                val lc = LearningCollection()
                var last = ""
                for (i in 1..n) {
                    last = "cat_$i"
                    lc.category = last
                }
                assertEquals(last, lc.category)
            }
        }

    @TestFactory
    fun `id field survives N reassignments`(): List<DynamicTest> =
        (1..100).map { n ->
            dynamicTest("$n reassignments of id") {
                val lc = LearningCollection()
                for (i in 1..n) {
                    lc.id = i.toLong()
                }
                assertEquals(n.toLong(), lc.id)
            }
        }

    // ===== INSTANCE INDEPENDENCE =====

    @TestFactory
    fun `batch created instances are independent`(): List<DynamicTest> =
        (1..100).map { batchSize ->
            dynamicTest("$batchSize independent instances") {
                val instances = (1..batchSize).map {
                    val lc = LearningCollection("learning_$it", "cat_$it", Date(it.toLong()))
                    lc.id = it.toLong()
                    lc
                }
                // modifying one should not affect others
                instances[0].learning = "MODIFIED"
                for (i in 1 until instances.size) {
                    assertNotEquals("MODIFIED", instances[i].learning)
                    assertEquals("learning_${i + 1}", instances[i].learning)
                }
            }
        }

    // ===== STRING CONTENT PATTERNS =====

    @TestFactory
    fun `learning with repeating patterns`(): List<DynamicTest> {
        val patterns = listOf("ab", "abc", "abcd", "hello ", "test-", "12345", "!@#")
        return patterns.flatMap { pattern ->
            (1..30).map { repeats ->
                dynamicTest("pattern '$pattern' x $repeats") {
                    val s = pattern.repeat(repeats)
                    val lc = LearningCollection(s, "cat", Date())
                    assertEquals(s, lc.learning)
                    assertEquals(pattern.length * repeats, lc.learning.length)
                }
            }
        }
    }

    // ===== CROSS-PRODUCT: learning length × category length =====

    @TestFactory
    fun `various learning length x category length combinations`(): List<DynamicTest> {
        val lengths = listOf(0, 1, 2, 5, 10, 20, 50, 100)
        return lengths.flatMap { lLen ->
            lengths.map { cLen ->
                dynamicTest("learning len=$lLen, category len=$cLen") {
                    val learning = "L".repeat(lLen)
                    val category = "C".repeat(cLen)
                    val lc = LearningCollection(learning, category, Date())
                    assertEquals(lLen, lc.learning.length)
                    assertEquals(cLen, lc.category.length)
                }
            }
        }
    }
}
