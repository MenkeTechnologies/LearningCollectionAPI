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
 * Invariant tests verifying properties that must always hold true
 * for LearningCollection across thousands of input combinations.
 */
class LearningCollectionInvariantTest {

    // ===== INVARIANT: set-then-get identity for learning =====

    @TestFactory
    fun `learning set-get identity for 500 random-like strings`(): List<DynamicTest> =
        (1..500).map { i ->
            val s = "val_${i}_${i * 37 % 1000}_end"
            dynamicTest("learning identity #$i") {
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
            }
        }

    @TestFactory
    fun `learning set-get identity via setter for 500 strings`(): List<DynamicTest> =
        (1..500).map { i ->
            val s = "setter_${i}_${i * 41 % 997}"
            dynamicTest("learning setter identity #$i") {
                val lc = LearningCollection()
                lc.learning = s
                assertEquals(s, lc.learning)
            }
        }

    // ===== INVARIANT: set-then-get identity for category =====

    @TestFactory
    fun `category set-get identity for 500 strings`(): List<DynamicTest> =
        (1..500).map { i ->
            val s = "cat_${i}_${i * 43 % 991}"
            dynamicTest("category identity #$i") {
                val lc = LearningCollection("test", s, Date())
                assertEquals(s, lc.category)
            }
        }

    @TestFactory
    fun `category set-get identity via setter for 500 strings`(): List<DynamicTest> =
        (1..500).map { i ->
            val s = "setcat_${i}_${i * 47 % 983}"
            dynamicTest("category setter identity #$i") {
                val lc = LearningCollection()
                lc.category = s
                assertEquals(s, lc.category)
            }
        }

    // ===== INVARIANT: set-then-get identity for id =====

    @TestFactory
    fun `id set-get identity for values -2000 to 2000`(): List<DynamicTest> =
        (-2000L..2000L).map { id ->
            dynamicTest("id identity $id") {
                val lc = LearningCollection("test", "cat", Date())
                lc.id = id
                assertEquals(id, lc.id)
            }
        }

    // ===== INVARIANT: set-then-get identity for dateAdded =====

    @TestFactory
    fun `dateAdded set-get identity for 2000 millis`(): List<DynamicTest> =
        (-1000L..1000L).map { ms ->
            dynamicTest("dateAdded identity ms=$ms") {
                val d = Date(ms)
                val lc = LearningCollection("test", "cat", d)
                assertEquals(ms, lc.dateAdded.time)
            }
        }

    // ===== INVARIANT: constructor initializes all fields =====

    @TestFactory
    fun `constructor sets all 3 fields for 500 combos`(): List<DynamicTest> =
        (1..500).map { i ->
            dynamicTest("ctor all fields #$i") {
                val l = "learn_$i"
                val c = "cat_$i"
                val d = Date(i.toLong() * 1000)
                val lc = LearningCollection(l, c, d)
                assertEquals(l, lc.learning)
                assertEquals(c, lc.category)
                assertEquals(d, lc.dateAdded)
            }
        }

    // ===== INVARIANT: default id is 0 =====

    @TestFactory
    fun `default id is 0 for 300 instances`(): List<DynamicTest> =
        (1..300).map { i ->
            dynamicTest("default id is 0 #$i") {
                val lc = LearningCollection("learn_$i", "cat", Date())
                assertEquals(0L, lc.id)
            }
        }

    // ===== INVARIANT: instances are independent =====

    @TestFactory
    fun `modifying one instance does not affect another for 300 pairs`(): List<DynamicTest> =
        (1..300).map { i ->
            dynamicTest("instance independence pair #$i") {
                val lc1 = LearningCollection("original", "cat", Date(0))
                val lc2 = LearningCollection("original", "cat", Date(0))
                lc1.learning = "modified_$i"
                lc1.id = i.toLong()
                assertEquals("original", lc2.learning)
                assertEquals(0L, lc2.id)
            }
        }

    // ===== INVARIANT: serialization preserves all fields =====

    @TestFactory
    fun `serialization preserves all fields for 300 instances`(): List<DynamicTest> =
        (1..300).map { i ->
            dynamicTest("serialization preserves all #$i") {
                val lc = LearningCollection("learn_$i", "cat_$i", Date(i.toLong() * 86400000))
                lc.id = i.toLong()
                val baos = ByteArrayOutputStream()
                ObjectOutputStream(baos).use { it.writeObject(lc) }
                val restored = ObjectInputStream(ByteArrayInputStream(baos.toByteArray())).use { it.readObject() } as LearningCollection
                assertEquals(lc.learning, restored.learning)
                assertEquals(lc.category, restored.category)
                assertEquals(lc.id, restored.id)
                assertEquals(lc.dateAdded.time, restored.dateAdded.time)
            }
        }

    // ===== INVARIANT: string length is preserved =====

    @TestFactory
    fun `learning length preserved for 500 lengths`(): List<DynamicTest> =
        (0..499).map { len ->
            dynamicTest("learning length preserved len=$len") {
                val s = "x".repeat(len)
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(len, lc.learning.length)
            }
        }

    @TestFactory
    fun `category length preserved for 500 lengths`(): List<DynamicTest> =
        (0..499).map { len ->
            dynamicTest("category length preserved len=$len") {
                val s = "y".repeat(len)
                val lc = LearningCollection("test", s, Date())
                assertEquals(len, lc.category.length)
            }
        }

    // ===== INVARIANT: Serializable interface =====

    @TestFactory
    fun `instanceof Serializable for 200 instances`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("is Serializable #$i") {
                val lc = LearningCollection("learn_$i", "cat", Date())
                assertTrue(lc is java.io.Serializable)
            }
        }

    // ===== INVARIANT: multiple reassignments converge to last value =====

    @TestFactory
    fun `multiple learning reassignments keep last value for 300 tests`(): List<DynamicTest> =
        (1..300).map { i ->
            dynamicTest("learning reassignment convergence #$i") {
                val lc = LearningCollection("first", "cat", Date())
                val values = (1..i).map { "val_$it" }
                values.forEach { lc.learning = it }
                assertEquals(values.last(), lc.learning)
            }
        }

    @TestFactory
    fun `multiple category reassignments keep last value for 300 tests`(): List<DynamicTest> =
        (1..300).map { i ->
            dynamicTest("category reassignment convergence #$i") {
                val lc = LearningCollection("test", "first", Date())
                val values = (1..i).map { "cat_$it" }
                values.forEach { lc.category = it }
                assertEquals(values.last(), lc.category)
            }
        }

    @TestFactory
    fun `multiple id reassignments keep last value for 300 tests`(): List<DynamicTest> =
        (1..300).map { i ->
            dynamicTest("id reassignment convergence #$i") {
                val lc = LearningCollection("test", "cat", Date())
                val values = (1..i).map { it.toLong() * 3 }
                values.forEach { lc.id = it }
                assertEquals(values.last(), lc.id)
            }
        }

    // ===== INVARIANT: date equality =====

    @TestFactory
    fun `dates with same millis are equal for 500 values`(): List<DynamicTest> =
        (0L..499L).map { ms ->
            dynamicTest("date equality ms=$ms") {
                val lc1 = LearningCollection("test", "cat", Date(ms))
                val lc2 = LearningCollection("test", "cat", Date(ms))
                assertEquals(lc1.dateAdded, lc2.dateAdded)
                assertEquals(lc1.dateAdded.time, lc2.dateAdded.time)
            }
        }

    // ===== INVARIANT: different dates are not equal =====

    @TestFactory
    fun `dates with different millis are not equal for 500 pairs`(): List<DynamicTest> =
        (0L..499L).map { ms ->
            dynamicTest("date inequality ms=$ms vs ${ms + 1}") {
                val lc1 = LearningCollection("test", "cat", Date(ms))
                val lc2 = LearningCollection("test", "cat", Date(ms + 1))
                assertNotEquals(lc1.dateAdded.time, lc2.dateAdded.time)
            }
        }

    // ===== INVARIANT: learning content is exactly what was provided =====

    @TestFactory
    fun `learning content exact match for prefix-suffix patterns 1 to 200`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("exact match pattern #$i") {
                val prefix = "pre_$i"
                val suffix = "_suf_$i"
                val middle = "_mid_${i * 7 % 100}_"
                val s = prefix + middle + suffix
                val lc = LearningCollection(s, "cat", Date())
                assertTrue(lc.learning.startsWith(prefix))
                assertTrue(lc.learning.endsWith(suffix))
                assertTrue(lc.learning.contains(middle))
                assertEquals(s, lc.learning)
            }
        }

    // ===== INVARIANT: no-arg constructor produces valid object =====

    @TestFactory
    fun `no-arg constructor produces object with id 0 for 200 tests`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("no-arg constructor id=0 #$i") {
                val lc = LearningCollection()
                assertEquals(0L, lc.id)
            }
        }

    // ===== INVARIANT: hash-like patterns in learning =====

    @TestFactory
    fun `learning with hash-like patterns`(): List<DynamicTest> =
        (1..200).map { i ->
            val hash = i.hashCode().toString(16)
            dynamicTest("hash pattern '$hash'") {
                val lc = LearningCollection(hash, "cat", Date())
                assertEquals(hash, lc.learning)
            }
        }

    // ===== INVARIANT: mixed whitespace preservation =====

    @TestFactory
    fun `mixed whitespace preservation for 200 patterns`(): List<DynamicTest> =
        (1..200).map { i ->
            val spaces = " ".repeat(i % 10)
            val tabs = "\t".repeat(i % 5)
            val newlines = "\n".repeat(i % 3)
            val s = spaces + "text_$i" + tabs + newlines
            dynamicTest("mixed whitespace #$i") {
                val lc = LearningCollection(s, "cat", Date())
                assertEquals(s, lc.learning)
                assertEquals(s.length, lc.learning.length)
            }
        }
}
