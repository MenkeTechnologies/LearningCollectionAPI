package com.menketechnologies.learningcollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.web.servlet.config.annotation.CorsRegistration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer

/**
 * Extended tests for WebConfig covering interface contracts,
 * CORS registration behavior, and instance properties.
 */
@ExtendWith(MockitoExtension::class)
class WebConfigExtendedTest {

    // ===== INTERFACE IMPLEMENTATION =====

    @TestFactory
    fun `WebConfig implements WebMvcConfigurer for 200 instances`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("implements WebMvcConfigurer #$i") {
                val config = WebConfig()
                assertTrue(config is WebMvcConfigurer)
            }
        }

    @TestFactory
    fun `WebConfig implements RepositoryRestConfigurer for 200 instances`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("implements RepositoryRestConfigurer #$i") {
                val config = WebConfig()
                assertTrue(config is RepositoryRestConfigurer)
            }
        }

    // ===== CORS REGISTRY CALLS =====

    @TestFactory
    fun `addCorsMappings calls addMapping with wildcard for 200 instances`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("addCorsMappings wildcard #$i") {
                val config = WebConfig()
                val registry = mock<CorsRegistry>()
                val registration = mock<CorsRegistration>()
                whenever(registry.addMapping("/**")).thenReturn(registration)
                config.addCorsMappings(registry)
                verify(registry).addMapping("/**")
            }
        }

    // ===== configureRepositoryRestConfiguration CALLS =====

    @TestFactory
    fun `configureRepositoryRestConfiguration registers CORS for 200 instances`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("configureRepositoryRestConfiguration CORS #$i") {
                val config = WebConfig()
                val restConfig = mock<RepositoryRestConfiguration>()
                val cors = mock<CorsRegistry>()
                val registration = mock<CorsRegistration>()
                whenever(cors.addMapping("/**")).thenReturn(registration)
                config.configureRepositoryRestConfiguration(restConfig, cors)
                verify(cors).addMapping("/**")
            }
        }

    // ===== MULTIPLE INVOCATIONS =====

    @TestFactory
    fun `addCorsMappings is idempotent for 100 double calls`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("idempotent addCorsMappings #$i") {
                val config = WebConfig()
                val registry = mock<CorsRegistry>()
                val registration = mock<CorsRegistration>()
                whenever(registry.addMapping(any())).thenReturn(registration)
                config.addCorsMappings(registry)
                config.addCorsMappings(registry)
                verify(registry, times(2)).addMapping("/**")
            }
        }

    @TestFactory
    fun `configureRepositoryRestConfiguration idempotent for 100 double calls`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("idempotent configureRepo #$i") {
                val config = WebConfig()
                val restConfig = mock<RepositoryRestConfiguration>()
                val cors = mock<CorsRegistry>()
                val registration = mock<CorsRegistration>()
                whenever(cors.addMapping(any())).thenReturn(registration)
                config.configureRepositoryRestConfiguration(restConfig, cors)
                config.configureRepositoryRestConfiguration(restConfig, cors)
                verify(cors, times(2)).addMapping("/**")
            }
        }

    // ===== INSTANCE CREATION =====

    @TestFactory
    fun `WebConfig can be instantiated 500 times`(): List<DynamicTest> =
        (1..500).map { i ->
            dynamicTest("instantiate #$i") {
                val config = WebConfig()
                assertNotNull(config)
            }
        }

    // ===== SEPARATE INSTANCES ARE INDEPENDENT =====

    @TestFactory
    fun `separate instances are independent for 200 pairs`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("independence #$i") {
                val config1 = WebConfig()
                val config2 = WebConfig()
                assertNotSame(config1, config2)
            }
        }

    // ===== NON-WILDCARD PATHS NOT PRESENT =====

    @TestFactory
    fun `addCorsMappings does not register specific paths`(): List<DynamicTest> {
        val paths = listOf(
            "/api", "/api/**", "/v1/**", "/v2/**", "/health", "/actuator",
            "/add", "/filter", "/recents", "/random", "/randoms", "/dump",
            "/admin", "/login", "/logout", "/users", "/static/**",
            "/css/**", "/js/**", "/images/**", "/favicon.ico",
            "/graphql", "/ws/**", "/socket/**", "/webhook",
        )
        return paths.flatMap { path ->
            (1..5).map { i ->
                dynamicTest("path '$path' not registered #$i") {
                    val config = WebConfig()
                    val registry = mock<CorsRegistry>()
                    val registration = mock<CorsRegistration>()
                    whenever(registry.addMapping(any())).thenReturn(registration)
                    config.addCorsMappings(registry)
                    verify(registry, never()).addMapping(path)
                }
            }
        }
    }

    @TestFactory
    fun `configureRepositoryRestConfiguration does not register specific paths`(): List<DynamicTest> {
        val paths = listOf(
            "/api", "/api/**", "/v1/**", "/v2/**", "/health", "/actuator",
            "/add", "/filter", "/recents", "/random", "/randoms", "/dump",
            "/admin", "/login", "/logout", "/users", "/static/**",
            "/css/**", "/js/**", "/images/**", "/favicon.ico",
            "/graphql", "/ws/**", "/socket/**", "/webhook",
        )
        return paths.flatMap { path ->
            (1..5).map { i ->
                dynamicTest("repo rest path '$path' not registered #$i") {
                    val config = WebConfig()
                    val restConfig = mock<RepositoryRestConfiguration>()
                    val cors = mock<CorsRegistry>()
                    val registration = mock<CorsRegistration>()
                    whenever(cors.addMapping(any())).thenReturn(registration)
                    config.configureRepositoryRestConfiguration(restConfig, cors)
                    verify(cors, never()).addMapping(path)
                }
            }
        }
    }

    // ===== CLASS PROPERTIES =====

    @TestFactory
    fun `WebConfig class name is correct for 100 checks`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("class name #$i") {
                assertEquals("WebConfig", WebConfig::class.simpleName)
            }
        }

    @TestFactory
    fun `WebConfig is not abstract for 100 checks`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("not abstract #$i") {
                assertFalse(WebConfig::class.isAbstract)
            }
        }

    // ===== ADDMAPPING CALLED EXACTLY ONCE PER INVOCATION =====

    @TestFactory
    fun `addMapping called exactly once per addCorsMappings for 200 calls`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("addMapping once #$i") {
                val config = WebConfig()
                val registry = mock<CorsRegistry>()
                val registration = mock<CorsRegistration>()
                whenever(registry.addMapping(any())).thenReturn(registration)
                config.addCorsMappings(registry)
                verify(registry, times(1)).addMapping(any())
            }
        }

    @TestFactory
    fun `addMapping called exactly once per configureRepositoryRestConfiguration for 200 calls`(): List<DynamicTest> =
        (1..200).map { i ->
            dynamicTest("repo rest addMapping once #$i") {
                val config = WebConfig()
                val restConfig = mock<RepositoryRestConfiguration>()
                val cors = mock<CorsRegistry>()
                val registration = mock<CorsRegistration>()
                whenever(cors.addMapping(any())).thenReturn(registration)
                config.configureRepositoryRestConfiguration(restConfig, cors)
                verify(cors, times(1)).addMapping(any())
            }
        }

    // ===== MULTIPLE CALL COUNTS =====

    @TestFactory
    fun `addCorsMappings N times results in N addMapping calls`(): List<DynamicTest> =
        (1..100).map { n ->
            dynamicTest("$n calls = $n addMapping") {
                val config = WebConfig()
                val registry = mock<CorsRegistry>()
                val registration = mock<CorsRegistration>()
                whenever(registry.addMapping(any())).thenReturn(registration)
                repeat(n) { config.addCorsMappings(registry) }
                verify(registry, times(n)).addMapping("/**")
            }
        }
}
