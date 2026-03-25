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
 * Exhaustive serialization tests for LearningCollection.
 * Tests roundtrip serialization/deserialization with many variations.
 */
class LearningCollectionSerializationTest {

    private fun serializeAndDeserialize(lc: LearningCollection): LearningCollection {
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { it.writeObject(lc) }
        return ObjectInputStream(ByteArrayInputStream(baos.toByteArray())).use { it.readObject() } as LearningCollection
    }

    // ===== LEARNING FIELD SERIALIZATION 1..500 =====

    @TestFactory
    fun `serialize learning with alpha strings length 1 to 500`(): List<DynamicTest> =
        (1..500).map { len ->
            dynamicTest("serialize alpha learning len=$len") {
                val s = (1..len).map { ('a' + (it % 26)).toChar() }.joinToString("")
                val lc = LearningCollection(s, "cat", Date(999))
                lc.id = len.toLong()
                val restored = serializeAndDeserialize(lc)
                assertEquals(s, restored.learning)
                assertEquals(len, restored.learning.length)
            }
        }

    // ===== CATEGORY FIELD SERIALIZATION 1..500 =====

    @TestFactory
    fun `serialize category with alpha strings length 1 to 500`(): List<DynamicTest> =
        (1..500).map { len ->
            dynamicTest("serialize alpha category len=$len") {
                val s = (1..len).map { ('A' + (it % 26)).toChar() }.joinToString("")
                val lc = LearningCollection("test", s, Date(999))
                val restored = serializeAndDeserialize(lc)
                assertEquals(s, restored.category)
            }
        }

    // ===== ID SERIALIZATION SWEEP =====

    @TestFactory
    fun `serialize id values -1000 to 1000`(): List<DynamicTest> =
        (-1000L..1000L).map { id ->
            dynamicTest("serialize id=$id") {
                val lc = LearningCollection("test", "cat", Date(0))
                lc.id = id
                val restored = serializeAndDeserialize(lc)
                assertEquals(id, restored.id)
            }
        }

    // ===== DATE SERIALIZATION SWEEP =====

    @TestFactory
    fun `serialize dates epoch ms -1000 to 1000`(): List<DynamicTest> =
        (-1000L..1000L).map { ms ->
            dynamicTest("serialize date ms=$ms") {
                val lc = LearningCollection("test", "cat", Date(ms))
                val restored = serializeAndDeserialize(lc)
                assertEquals(ms, restored.dateAdded.time)
            }
        }

    // ===== UNICODE LEARNING SERIALIZATION =====

    @TestFactory
    fun `serialize learning with unicode 0x0020 to 0x0400`(): List<DynamicTest> =
        (0x0020..0x0400).map { cp ->
            dynamicTest("serialize unicode learning U+${cp.toString(16).uppercase().padStart(4, '0')}") {
                val s = cp.toChar().toString()
                val lc = LearningCollection(s, "cat", Date(0))
                val restored = serializeAndDeserialize(lc)
                assertEquals(s, restored.learning)
            }
        }

    // ===== UNICODE CATEGORY SERIALIZATION =====

    @TestFactory
    fun `serialize category with unicode 0x0020 to 0x0400`(): List<DynamicTest> =
        (0x0020..0x0400).map { cp ->
            dynamicTest("serialize unicode category U+${cp.toString(16).uppercase().padStart(4, '0')}") {
                val s = cp.toChar().toString()
                val lc = LearningCollection("test", s, Date(0))
                val restored = serializeAndDeserialize(lc)
                assertEquals(s, restored.category)
            }
        }

    // ===== CROSS-PRODUCT: LEARNING LENGTH x CATEGORY LENGTH =====

    @TestFactory
    fun `serialize cross-product learning x category lengths`(): List<DynamicTest> {
        val lengths = listOf(1, 5, 10, 25, 50, 100, 150, 200, 250, 300)
        return lengths.flatMap { lLen ->
            lengths.map { cLen ->
                dynamicTest("serialize learning=$lLen x category=$cLen") {
                    val learning = "L".repeat(lLen)
                    val category = "C".repeat(cLen)
                    val lc = LearningCollection(learning, category, Date(42))
                    lc.id = (lLen * 1000 + cLen).toLong()
                    val restored = serializeAndDeserialize(lc)
                    assertEquals(learning, restored.learning)
                    assertEquals(category, restored.category)
                    assertEquals(42L, restored.dateAdded.time)
                    assertEquals(lc.id, restored.id)
                }
            }
        }
    }

    // ===== CROSS-PRODUCT: ID x DATE =====

    @TestFactory
    fun `serialize cross-product id x date`(): List<DynamicTest> {
        val ids = listOf(0L, 1L, -1L, 100L, -100L, 1000L, -1000L, Long.MAX_VALUE, Long.MIN_VALUE)
        val dates = listOf(0L, 1L, -1L, 1000L, -1000L, 86400000L, 1000000000000L)
        return ids.flatMap { id ->
            dates.map { ms ->
                dynamicTest("serialize id=$id date=$ms") {
                    val lc = LearningCollection("test", "cat", Date(ms))
                    lc.id = id
                    val restored = serializeAndDeserialize(lc)
                    assertEquals(id, restored.id)
                    assertEquals(ms, restored.dateAdded.time)
                }
            }
        }
    }

    // ===== REPEATED SERIALIZATION OF SAME OBJECT =====

    @TestFactory
    fun `serialize same object 200 times`(): List<DynamicTest> {
        val lc = LearningCollection("persistent", "stable", Date(12345))
        lc.id = 99
        return (1..200).map { i ->
            dynamicTest("re-serialize iteration $i") {
                val restored = serializeAndDeserialize(lc)
                assertEquals("persistent", restored.learning)
                assertEquals("stable", restored.category)
                assertEquals(12345L, restored.dateAdded.time)
                assertEquals(99L, restored.id)
            }
        }
    }

    // ===== SERIALIZE AFTER MUTATION =====

    @TestFactory
    fun `serialize after mutating learning 200 times`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("serialize after learning mutation #$i") {
                val lc = LearningCollection("original", "cat", Date(0))
                lc.learning = "mutated_$i"
                val restored = serializeAndDeserialize(lc)
                assertEquals("mutated_$i", restored.learning)
            }
        }

    @TestFactory
    fun `serialize after mutating category 200 times`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("serialize after category mutation #$i") {
                val lc = LearningCollection("test", "original", Date(0))
                lc.category = "mutated_cat_$i"
                val restored = serializeAndDeserialize(lc)
                assertEquals("mutated_cat_$i", restored.category)
            }
        }

    @TestFactory
    fun `serialize after mutating id 200 times`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("serialize after id mutation #$i") {
                val lc = LearningCollection("test", "cat", Date(0))
                lc.id = i.toLong() * 7
                val restored = serializeAndDeserialize(lc)
                assertEquals(i.toLong() * 7, restored.id)
            }
        }

    @TestFactory
    fun `serialize after mutating date 200 times`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("serialize after date mutation #$i") {
                val lc = LearningCollection("test", "cat", Date(0))
                lc.dateAdded = Date(i.toLong() * 86400000)
                val restored = serializeAndDeserialize(lc)
                assertEquals(i.toLong() * 86400000, restored.dateAdded.time)
            }
        }

    // ===== SERIALIZATION WITH SPECIAL STRINGS =====

    @TestFactory
    fun `serialize with special string patterns`(): List<DynamicTest> {
        val specials = listOf(
            "", " ", "\t", "\n", "\r\n",
            "null", "NULL", "None", "nil", "undefined",
            "true", "false", "0", "-1", "NaN",
            "<html>", "<script>", "DROP TABLE",
            "'quote'", "\"double\"", "back\\slash",
            "\u0000", "\u0001", "\uFFFF",
            "emoji😀", "中文", "日本語", "한국어",
            "a".repeat(100), "b".repeat(500), "c".repeat(1000),
        )
        return specials.flatMap { s ->
            listOf(
                dynamicTest("serialize learning='${s.take(20)}'") {
                    val lc = LearningCollection(s, "cat", Date(0))
                    val restored = serializeAndDeserialize(lc)
                    assertEquals(s, restored.learning)
                },
                dynamicTest("serialize category='${s.take(20)}'") {
                    val lc = LearningCollection("test", s, Date(0))
                    val restored = serializeAndDeserialize(lc)
                    assertEquals(s, restored.category)
                }
            )
        }
    }

    // ===== SERIALIZED BYTE SIZE IS POSITIVE =====

    @TestFactory
    fun `serialized bytes are non-empty for learning lengths 1 to 300`(): List<DynamicTest> =
        (1..300).map { len ->
            dynamicTest("serialized bytes non-empty len=$len") {
                val lc = LearningCollection("x".repeat(len), "cat", Date(0))
                val baos = ByteArrayOutputStream()
                ObjectOutputStream(baos).use { it.writeObject(lc) }
                assertTrue(baos.toByteArray().isNotEmpty())
                assertTrue(baos.toByteArray().size > len)
            }
        }

    // ===== DESERIALIZED IS A DIFFERENT OBJECT =====

    @TestFactory
    fun `deserialized is not same reference for 200 tests`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("deserialized is different reference #$i") {
                val lc = LearningCollection("test_$i", "cat_$i", Date(i.toLong()))
                lc.id = i.toLong()
                val restored = serializeAndDeserialize(lc)
                assertNotSame(lc, restored)
                assertEquals(lc.learning, restored.learning)
                assertEquals(lc.category, restored.category)
                assertEquals(lc.id, restored.id)
                assertEquals(lc.dateAdded, restored.dateAdded)
            }
        }

    // ===== SERIALIZE BATCH =====

    @TestFactory
    fun `serialize batch of instances 1 to 300`(): List<DynamicTest> =
        (1..300).map { batchSize ->
            dynamicTest("serialize batch of $batchSize") {
                val instances = (1..batchSize).map {
                    val lc = LearningCollection("learn_$it", "cat_$it", Date(it.toLong()))
                    lc.id = it.toLong()
                    lc
                }
                val baos = ByteArrayOutputStream()
                ObjectOutputStream(baos).use { oos ->
                    instances.forEach { oos.writeObject(it) }
                }
                val bais = ByteArrayInputStream(baos.toByteArray())
                ObjectInputStream(bais).use { ois ->
                    instances.forEachIndexed { idx, original ->
                        val restored = ois.readObject() as LearningCollection
                        assertEquals(original.learning, restored.learning)
                        assertEquals(original.category, restored.category)
                        assertEquals(original.id, restored.id)
                        assertEquals(original.dateAdded, restored.dateAdded)
                    }
                }
            }
        }
}
