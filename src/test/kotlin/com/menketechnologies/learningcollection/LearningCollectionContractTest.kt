package com.menketechnologies.learningcollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Date

/**
 * Additional contract pins for the LearningCollection entity that
 * the existing test suites don't cover:
 *
 *   1. Constructor field assignment is positional (learning, category,
 *      dateAdded) — drift here silently shifts every persisted row.
 *   2. id starts at 0 before persistence (Hibernate's @GeneratedValue
 *      contract). A non-zero default would cause every save to attempt
 *      an UPDATE instead of an INSERT.
 *   3. Class implements Serializable (JPA L2 cache / clustering relies
 *      on it; removing the marker silently breaks distributed deploys).
 *   4. The constructor preserves the exact Date instance (no defensive
 *      copy, no clock-rewind to "now") so callers can re-read a value
 *      they assigned moments ago.
 */
class LearningCollectionContractTest {

    @Test
    fun `constructor populates learning category dateAdded positionally`() {
        val d = Date(1_700_000_000_000L)
        val lc = LearningCollection("rust ownership", "programming", d)
        assertEquals("rust ownership", lc.learning)
        assertEquals("programming", lc.category)
        assertEquals(d, lc.dateAdded)
    }

    @Test
    fun `id defaults to 0 before persistence`() {
        val lc = LearningCollection("x", "y", Date())
        assertEquals(0L, lc.id, "id must be 0 so @GeneratedValue triggers INSERT")
    }

    @Test
    fun `class implements Serializable for JPA L2 + clustering compat`() {
        val lc = LearningCollection("x", "y", Date())
        assertTrue(lc is java.io.Serializable, "must implement Serializable")
    }

    @Test
    fun `constructor preserves Date identity not a snapshot`() {
        // Hibernate doesn't defensive-copy Dates on save; tests that
        // rely on `==` comparison would break if a copy snuck in.
        val d = Date(1_700_000_000_000L)
        val lc = LearningCollection("learn", "cat", d)
        assertSame(d, lc.dateAdded, "Date must be preserved, not copied")
    }

    @Test
    fun `multiple instances do not share lateinit fields`() {
        val a = LearningCollection("a", "cat1", Date(1))
        val b = LearningCollection("b", "cat2", Date(2))
        assertNotEquals(a.learning, b.learning)
        assertNotEquals(a.category, b.category)
        assertNotEquals(a.dateAdded.time, b.dateAdded.time)
    }

    @Test
    fun `empty strings are allowed by constructor (validation lives elsewhere)`() {
        // The constructor itself doesn't reject empty inputs; the
        // CLI / repo layer is responsible. Pin that the constructor
        // accepts them so callers can pass user input raw.
        val lc = LearningCollection("", "", Date())
        assertEquals("", lc.learning)
        assertEquals("", lc.category)
    }
}
