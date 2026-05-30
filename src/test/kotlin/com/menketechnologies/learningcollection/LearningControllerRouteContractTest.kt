package com.menketechnologies.learningcollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Route-contract pins for LearningController. Existing tests verify endpoint
 * behavior under mocked LCRepo; this file pins the wiring contract that
 * holds those endpoints to their published HTTP paths.
 *
 * Why this matters: a silent rename of `@GetMapping("/recent/{count}")` to
 * `@GetMapping("/recents/{count}")` would silently break every external
 * caller (sequel-front, scripts in zpwr) without a single existing test
 * failing — they all mock through method names. This file pins the path
 * strings via reflection so a path rename forces a test update.
 */
class LearningControllerRouteContractTest {

    private val controllerClass = LearningController::class.java

    @Test
    fun `LearningController is annotated with @RestController`() {
        // Pin: controller is exposed as a REST endpoint (not just @Controller,
        // which would require explicit @ResponseBody on every method).
        assertNotNull(controllerClass.getAnnotation(RestController::class.java))
    }

    @TestFactory
    fun `each controller endpoint method is bound to its expected GET path`(): List<DynamicTest> {
        // method-name -> expected @GetMapping("<path>")
        // These are the documented public URLs; any drift is breaking.
        val routes = listOf(
            "add" to "/add",
            "filterLearn" to "/filter",
            "learningItemRecentShortDefault" to "/recents",
            "getLearningItemRecentShort" to "/recents/{count}",
            "getLearningItemRecent" to "/recent/{count}",
            "getDump" to "/dump",
            "learningItemCountShort" to "/randoms",
            "getLearningItemCountShort" to "/randoms/{count}",
            "getLearningItemCount" to "/random/{count}",
            "learningItem" to "/random",
        )
        return routes.map { (methodName, expectedPath) ->
            dynamicTest("$methodName -> @GetMapping(\"$expectedPath\")") {
                val method = controllerClass.declaredMethods.firstOrNull { it.name == methodName }
                assertNotNull(method, "method $methodName missing from controller")
                val getMapping = method!!.getAnnotation(GetMapping::class.java)
                assertNotNull(getMapping, "method $methodName missing @GetMapping")
                assertArrayEquals(arrayOf(expectedPath), getMapping.value,
                    "method $methodName has unexpected @GetMapping value")
            }
        }
    }

    @TestFactory
    fun `each path-variable endpoint binds @PathVariable("count")`(): List<DynamicTest> {
        // The {count} path segment must be wired with @PathVariable("count"),
        // not the default reflection-based name (which compiles but breaks
        // on JDK builds that strip parameter names without `-parameters`).
        val pathVarMethods = listOf(
            "getLearningItemRecentShort",
            "getLearningItemRecent",
            "getLearningItemCountShort",
            "getLearningItemCount",
        )
        return pathVarMethods.map { methodName ->
            dynamicTest("$methodName uses @PathVariable(\"count\")") {
                val method = controllerClass.declaredMethods.first { it.name == methodName }
                val param = method.parameters.firstOrNull { p ->
                    p.getAnnotation(PathVariable::class.java) != null
                }
                assertNotNull(param, "$methodName has no @PathVariable parameter")
                val annot = param!!.getAnnotation(PathVariable::class.java)
                assertEquals("count", annot.value,
                    "$methodName @PathVariable name must be 'count'")
            }
        }
    }

    @Test
    fun `add endpoint binds @RequestParam("learning") on its single parameter`() {
        // The /add endpoint is the single write-side controller method.
        // Its single parameter must carry @RequestParam("learning") so that
        // GET /add?learning=foo binds correctly. Default reflection naming
        // breaks under JDKs built without `-parameters`.
        val method = controllerClass.declaredMethods.first { it.name == "add" }
        val param = method.parameters.first()
        val annot = param.getAnnotation(RequestParam::class.java)
        assertNotNull(annot, "add() param is missing @RequestParam")
        assertEquals("learning", annot!!.value, "add() @RequestParam must be 'learning'")
    }

    @Test
    fun `filter endpoint binds @RequestParam("learning")`() {
        // Same contract as /add but for /filter — the query param name is
        // "learning", matching the documented API.
        val method = controllerClass.declaredMethods.first { it.name == "filterLearn" }
        val param = method.parameters.first()
        val annot = param.getAnnotation(RequestParam::class.java)
        assertNotNull(annot, "filterLearn() param is missing @RequestParam")
        assertEquals("learning", annot!!.value, "filterLearn() @RequestParam must be 'learning'")
    }

    @Test
    fun `controller exposes exactly 10 GET endpoint methods`() {
        // Pin the surface area so adding/removing an endpoint is intentional
        // and forces a test update — silent surface drift is a compat break.
        val getMappingMethods = controllerClass.declaredMethods.count {
            it.getAnnotation(GetMapping::class.java) != null
        }
        assertEquals(10, getMappingMethods,
            "controller endpoint count drift — update tests AND callers")
    }
}
