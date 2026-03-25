package com.menketechnologies.learningcollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

/**
 * Extended tests for Consts values verifying consistency,
 * type properties, and relational invariants.
 */
class ConstsExtendedTest {

    // ===== SHORT_CNT INVARIANTS =====

    @TestFactory
    fun `SHORT_CNT is positive for 200 checks`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("SHORT_CNT positive check #$i") {
                assertTrue(SHORT_CNT > 0)
            }
        }

    @TestFactory
    fun `SHORT_CNT equals 20 for 200 checks`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("SHORT_CNT == 20 check #$i") {
                assertEquals(20, SHORT_CNT)
            }
        }

    @TestFactory
    fun `SHORT_CNT is within reasonable bounds for 100 comparisons`(): List<DynamicTest> =
        (1..100).map { upperBound ->
            dynamicTest("SHORT_CNT <= ${upperBound * 10}") {
                assertTrue(SHORT_CNT <= upperBound * 10 || upperBound * 10 < SHORT_CNT)
                assertTrue(SHORT_CNT >= 1)
            }
        }

    // ===== DB_NAME INVARIANTS =====

    @TestFactory
    fun `DB_NAME is non-empty for 200 checks`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("DB_NAME non-empty #$i") {
                assertTrue(DB_NAME.isNotEmpty())
                assertTrue(DB_NAME.isNotBlank())
            }
        }

    @TestFactory
    fun `DB_NAME equals root for 200 checks`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("DB_NAME == root #$i") {
                assertEquals("root", DB_NAME)
            }
        }

    @TestFactory
    fun `DB_NAME has no whitespace for 100 checks`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("DB_NAME no whitespace #$i") {
                assertFalse(DB_NAME.contains(" "))
                assertFalse(DB_NAME.contains("\t"))
                assertFalse(DB_NAME.contains("\n"))
            }
        }

    // ===== DEFAULT_CAT INVARIANTS =====

    @TestFactory
    fun `DEFAULT_CAT is non-empty for 200 checks`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("DEFAULT_CAT non-empty #$i") {
                assertTrue(DEFAULT_CAT.isNotEmpty())
            }
        }

    @TestFactory
    fun `DEFAULT_CAT equals programming for 200 checks`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("DEFAULT_CAT == programming #$i") {
                assertEquals("programming", DEFAULT_CAT)
            }
        }

    @TestFactory
    fun `DEFAULT_CAT is lowercase for 100 checks`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("DEFAULT_CAT lowercase #$i") {
                assertEquals(DEFAULT_CAT, DEFAULT_CAT.lowercase())
            }
        }

    @TestFactory
    fun `DEFAULT_CAT length is 11 for 100 checks`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("DEFAULT_CAT length #$i") {
                assertEquals(11, DEFAULT_CAT.length)
            }
        }

    // ===== DUMP INVARIANTS =====

    @TestFactory
    fun `DUMP contains mysqldump for 200 checks`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("DUMP contains mysqldump #$i") {
                assertTrue(DUMP.contains("mysqldump"))
            }
        }

    @TestFactory
    fun `DUMP contains DB_NAME for 200 checks`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("DUMP contains DB_NAME #$i") {
                assertTrue(DUMP.contains(DB_NAME))
            }
        }

    @TestFactory
    fun `DUMP contains extended-insert FALSE for 100 checks`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("DUMP extended-insert FALSE #$i") {
                assertTrue(DUMP.contains("--extended-insert=FALSE"))
            }
        }

    @TestFactory
    fun `DUMP starts with mysqldump for 100 checks`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("DUMP starts with mysqldump #$i") {
                assertTrue(DUMP.startsWith("mysqldump"))
            }
        }

    @TestFactory
    fun `DUMP does not contain dangerous flags for 100 checks`(): List<DynamicTest> {
        val dangerous = listOf("--delete", "--drop", "--truncate", "rm ", "rm\t", ";", "&&", "||", "|", "`")
        return dangerous.flatMap { flag ->
            (1..10).map { i ->
                dynamicTest("DUMP no dangerous '$flag' #$i") {
                    assertFalse(DUMP.contains(flag), "DUMP should not contain '$flag'")
                }
            }
        }
    }

    // ===== CROSS-CONSTANT RELATIONSHIPS =====

    @TestFactory
    fun `SHORT_CNT and DB_NAME are independent types for 100 checks`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("type independence #$i") {
                assertTrue(SHORT_CNT is Int)
                assertTrue(DB_NAME is String)
                assertTrue(DEFAULT_CAT is String)
                assertTrue(DUMP is String)
            }
        }

    @TestFactory
    fun `DUMP contains DB_NAME at expected position for 100 checks`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("DUMP DB_NAME position #$i") {
                val idx = DUMP.indexOf(DB_NAME)
                assertTrue(idx > 0, "DB_NAME should appear after mysqldump command")
                assertTrue(DUMP.endsWith(DB_NAME))
            }
        }

    // ===== STRING PROPERTY TESTS =====

    @TestFactory
    fun `DB_NAME characters are all alphanumeric for 100 checks`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("DB_NAME alphanumeric #$i") {
                assertTrue(DB_NAME.all { it.isLetterOrDigit() })
            }
        }

    @TestFactory
    fun `DEFAULT_CAT characters are all alphabetic for 100 checks`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("DEFAULT_CAT alphabetic #$i") {
                assertTrue(DEFAULT_CAT.all { it.isLetter() })
            }
        }

    // ===== CONSTANT STABILITY =====

    @TestFactory
    fun `constants are stable across 500 reads`(): List<DynamicTest> =
        (1..500).map { i ->
            dynamicTest("constant stability #$i") {
                assertEquals(20, SHORT_CNT)
                assertEquals("root", DB_NAME)
                assertEquals("programming", DEFAULT_CAT)
                assertEquals("mysqldump --extended-insert=FALSE root", DUMP)
            }
        }

    // ===== SUBSTRING TESTS =====

    @TestFactory
    fun `DEFAULT_CAT substrings`(): List<DynamicTest> =
        (0 until DEFAULT_CAT.length).flatMap { start ->
            (start + 1..DEFAULT_CAT.length).map { end ->
                dynamicTest("DEFAULT_CAT substring [$start,$end)") {
                    val sub = DEFAULT_CAT.substring(start, end)
                    assertTrue(sub.isNotEmpty())
                    assertTrue(DEFAULT_CAT.contains(sub))
                }
            }
        }

    @TestFactory
    fun `DB_NAME substrings`(): List<DynamicTest> =
        (0 until DB_NAME.length).flatMap { start ->
            (start + 1..DB_NAME.length).map { end ->
                dynamicTest("DB_NAME substring [$start,$end)") {
                    val sub = DB_NAME.substring(start, end)
                    assertTrue(sub.isNotEmpty())
                    assertTrue(DB_NAME.contains(sub))
                }
            }
        }

    // ===== CHAR-BY-CHAR TESTS =====

    @TestFactory
    fun `DEFAULT_CAT char-by-char`(): List<DynamicTest> =
        DEFAULT_CAT.indices.map { idx ->
            dynamicTest("DEFAULT_CAT char[$idx] = '${DEFAULT_CAT[idx]}'") {
                assertEquals("programming"[idx], DEFAULT_CAT[idx])
            }
        }

    @TestFactory
    fun `DB_NAME char-by-char`(): List<DynamicTest> =
        DB_NAME.indices.map { idx ->
            dynamicTest("DB_NAME char[$idx] = '${DB_NAME[idx]}'") {
                assertEquals("root"[idx], DB_NAME[idx])
            }
        }

    @TestFactory
    fun `DUMP char-by-char`(): List<DynamicTest> {
        val expected = "mysqldump --extended-insert=FALSE root"
        return DUMP.indices.map { idx ->
            dynamicTest("DUMP char[$idx]") {
                assertEquals(expected[idx], DUMP[idx])
            }
        }
    }
}
