package com.menketechnologies.learningcollection

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
import java.util.*

/**
 * Error-path contract tests for LearningController.
 *
 * These pin the exact exception type each endpoint surfaces on its two
 * un-guarded failure modes, neither of which is exercised by the existing
 * happy-path / boundary suites:
 *
 *   1. Negative {count} path variables. The endpoints end in `take(count)`,
 *      and Kotlin's stdlib `Iterable.take` does `require(n >= 0)`, throwing
 *      IllegalArgumentException for any negative count. A path like
 *      `/recents/-1` is reachable (Spring binds a negative Int fine), so the
 *      controller throws rather than returning an empty list. Pinning the
 *      exception type means a future "clamp negative to 0" or "return empty"
 *      change is a deliberate, test-visible decision, not a silent regression.
 *
 *   2. Empty repository for the single-element endpoints `/randoms` and
 *      `/random`. Both terminate in `.first()`, which throws
 *      NoSuchElementException on an empty list. Every existing test for these
 *      two endpoints seeds a non-empty repo, so the empty-repo branch — the
 *      most likely real-world first-request state — is untested.
 */
@ExtendWith(MockitoExtension::class)
class LearningControllerErrorPathTest {

    @Mock
    private lateinit var lcRepo: LCRepo

    @InjectMocks
    private lateinit var controller: LearningController

    private fun makeLc(id: Long, learning: String): LearningCollection {
        val lc = LearningCollection(learning, DEFAULT_CAT, Date(id * 1000))
        lc.id = id
        return lc
    }

    private fun makeItems(size: Int): List<LearningCollection> =
        (1..size).map { makeLc(it.toLong(), "learning_$it") }

    // ===== NEGATIVE COUNT: take(n) rejects n < 0 =====

    @TestFactory
    fun `count endpoints throw IllegalArgumentException for negative counts`(): List<DynamicTest> {
        // (label, invocation) for each of the four {count} endpoints.
        val endpoints: List<Pair<String, (Int) -> Unit>> = listOf(
            "recents/{count}" to { c -> controller.getLearningItemRecentShort(c) },
            "recent/{count}" to { c -> controller.getLearningItemRecent(c) },
            "randoms/{count}" to { c -> controller.getLearningItemCountShort(c) },
            "random/{count}" to { c -> controller.getLearningItemCount(c) },
        )
        // -1 is the off-by-one boundary just below the valid range; the other
        // values guard against an accidental `n > -K` style guard slipping in.
        val negatives = listOf(-1, -2, -10, -100, Int.MIN_VALUE)
        val items = makeItems(5)
        return endpoints.flatMap { (label, call) ->
            negatives.map { count ->
                dynamicTest("$label with count=$count throws IllegalArgumentException") {
                    whenever(lcRepo.findAll()).thenReturn(items)
                    val ex = assertThrows(IllegalArgumentException::class.java) { call(count) }
                    // Kotlin stdlib's exact message proves it's take()'s guard
                    // firing, not some unrelated IAE from deeper in the stack.
                    assertTrue(
                        ex.message?.contains("less than zero") == true,
                        "expected take()'s count guard message, got: ${ex.message}",
                    )
                }
            }
        }
    }

    @Test
    fun `negative count does not swallow into an empty result`() {
        // Guards specifically against a regression where someone "fixes" the
        // throw by coercing negatives, which would change the HTTP contract
        // from 500 to 200-empty silently. If the body ever stops throwing,
        // this assertion forces that decision to be made explicitly.
        whenever(lcRepo.findAll()).thenReturn(makeItems(3))
        assertThrows(IllegalArgumentException::class.java) {
            controller.getLearningItemRecent(-1)
        }
    }

    // ===== EMPTY REPO: first() on the single-element endpoints =====

    @Test
    fun `randoms on empty repo throws NoSuchElementException`() {
        whenever(lcRepo.findAll()).thenReturn(emptyList())
        assertThrows(NoSuchElementException::class.java) {
            controller.learningItemCountShort()
        }
    }

    @Test
    fun `random on empty repo throws NoSuchElementException`() {
        whenever(lcRepo.findAll()).thenReturn(emptyList())
        assertThrows(NoSuchElementException::class.java) {
            controller.learningItem()
        }
    }

    @Test
    fun `randoms on single-element repo returns that element not a throw`() {
        // The boundary just above empty: size == 1 must NOT throw, proving the
        // exception above is strictly the empty-list branch and not a blanket
        // failure of the shuffle+first pipeline.
        whenever(lcRepo.findAll()).thenReturn(listOf(makeLc(7, "only")))
        assertEquals("only", controller.learningItemCountShort())
    }

    @Test
    fun `random on single-element repo returns that entity not a throw`() {
        whenever(lcRepo.findAll()).thenReturn(listOf(makeLc(7, "only")))
        assertEquals(7L, controller.learningItem().id)
    }
}
