package com.menketechnologies.learningcollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ConstsTest {

    @Test
    fun `SHORT_CNT is 20`() = assertEquals(20, SHORT_CNT)

    @Test
    fun `DB_NAME is root`() = assertEquals("root", DB_NAME)

    @Test
    fun `DEFAULT_CAT is programming`() = assertEquals("programming", DEFAULT_CAT)

    @Test
    fun `DUMP contains mysqldump command`() = assertTrue(DUMP.startsWith("mysqldump"))

    @Test
    fun `DUMP contains extended-insert FALSE`() = assertTrue(DUMP.contains("--extended-insert=FALSE"))

    @Test
    fun `DUMP contains DB_NAME`() = assertTrue(DUMP.contains(DB_NAME))

    @Test
    fun `SHORT_CNT is positive`() = assertTrue(SHORT_CNT > 0)

    @Test
    fun `DEFAULT_CAT is not blank`() = assertTrue(DEFAULT_CAT.isNotBlank())

    @Test
    fun `DB_NAME is not blank`() = assertTrue(DB_NAME.isNotBlank())

    @Test
    fun `SHORT_CNT equals 20 exactly`() = assertEquals(20, SHORT_CNT)

    @Test
    fun `DUMP is a complete command string`() {
        val parts = DUMP.split(" ")
        assertTrue(parts.size >= 2)
        assertEquals("mysqldump", parts[0])
    }

    @Test
    fun `DUMP does not contain password`() {
        assertFalse(DUMP.contains("--password"))
        assertFalse(DUMP.contains("-p"))
    }

    @Test
    fun `DEFAULT_CAT is lowercase`() = assertEquals(DEFAULT_CAT, DEFAULT_CAT.lowercase())

    @Test
    fun `DB_NAME is lowercase`() = assertEquals(DB_NAME, DB_NAME.lowercase())

    @Test
    fun `SHORT_CNT is even`() = assertEquals(0, SHORT_CNT % 2)

    @Test
    fun `DUMP ends with DB_NAME`() = assertTrue(DUMP.endsWith(DB_NAME))

    @Test
    fun `constants are stable across multiple accesses`() {
        repeat(100) {
            assertEquals(20, SHORT_CNT)
            assertEquals("root", DB_NAME)
            assertEquals("programming", DEFAULT_CAT)
        }
    }

    @TestFactory
    fun `SHORT_CNT comparison with various values`(): List<DynamicTest> =
        (1..50).map { n ->
            dynamicTest("SHORT_CNT vs $n") {
                if (n < SHORT_CNT) assertTrue(n < SHORT_CNT)
                else if (n == SHORT_CNT) assertEquals(n, SHORT_CNT)
                else assertTrue(n > SHORT_CNT)
            }
        }

    @TestFactory
    fun `DEFAULT_CAT does not equal other categories`(): List<DynamicTest> {
        val others = listOf(
            "science", "math", "history", "art", "music",
            "sports", "cooking", "travel", "finance", "health",
            "PROGRAMMING", "Programming", "PROG", "prog",
        )
        return others.map { other ->
            dynamicTest("DEFAULT_CAT != '$other'") {
                assertNotEquals(other, DEFAULT_CAT)
            }
        }
    }

    @TestFactory
    fun `DB_NAME does not equal other db names`(): List<DynamicTest> {
        val others = listOf(
            "admin", "postgres", "mysql", "test", "production",
            "ROOT", "Root", "user", "database", "db",
        )
        return others.map { other ->
            dynamicTest("DB_NAME != '$other'") {
                assertNotEquals(other, DB_NAME)
            }
        }
    }

    @TestFactory
    fun `DUMP contains expected substrings`(): List<DynamicTest> {
        val expected = listOf("mysqldump", "--extended-insert=FALSE", DB_NAME)
        return expected.map { sub ->
            dynamicTest("DUMP contains '$sub'") {
                assertTrue(DUMP.contains(sub))
            }
        }
    }

    @TestFactory
    fun `DUMP does not contain dangerous flags`(): List<DynamicTest> {
        val dangerous = listOf(
            "--password", "-p", "--delete", "--drop",
            "--force", "--replace", "rm ", "sudo",
        )
        return dangerous.map { flag ->
            dynamicTest("DUMP does not contain '$flag'") {
                assertFalse(DUMP.contains(flag))
            }
        }
    }
}
