package com.menketechnologies.learningcollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.web.servlet.config.annotation.CorsRegistration
import org.springframework.web.servlet.config.annotation.CorsRegistry

@ExtendWith(MockitoExtension::class)
class WebConfigTest {

    private val webConfig = WebConfig()

    @Test
    fun `implements WebMvcConfigurer`() {
        assertTrue(webConfig is org.springframework.web.servlet.config.annotation.WebMvcConfigurer)
    }

    @Test
    fun `implements RepositoryRestConfigurer`() {
        assertTrue(webConfig is org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer)
    }

    @Test
    fun `addCorsMappings registers wildcard mapping`() {
        val registry = mock<CorsRegistry>()
        val registration = mock<CorsRegistration>()
        whenever(registry.addMapping("/**")).thenReturn(registration)
        webConfig.addCorsMappings(registry)
        verify(registry).addMapping("/**")
    }

    @Test
    fun `configureRepositoryRestConfiguration registers CORS`() {
        val config = mock<RepositoryRestConfiguration>()
        val cors = mock<CorsRegistry>()
        val registration = mock<CorsRegistration>()
        whenever(cors.addMapping("/**")).thenReturn(registration)
        webConfig.configureRepositoryRestConfiguration(config, cors)
        verify(cors).addMapping("/**")
    }

    @Test
    fun `addCorsMappings uses correct wildcard pattern`() {
        val registry = mock<CorsRegistry>()
        val registration = mock<CorsRegistration>()
        whenever(registry.addMapping(any())).thenReturn(registration)
        webConfig.addCorsMappings(registry)
        verify(registry).addMapping("/**")
        verify(registry, never()).addMapping("/api/**")
    }

    @Test
    fun `addCorsMappings calls addMapping exactly once`() {
        val registry = mock<CorsRegistry>()
        val registration = mock<CorsRegistration>()
        whenever(registry.addMapping(any())).thenReturn(registration)
        webConfig.addCorsMappings(registry)
        verify(registry, times(1)).addMapping(any())
    }

    @Test
    fun `configureRepositoryRestConfiguration calls addMapping exactly once`() {
        val config = mock<RepositoryRestConfiguration>()
        val cors = mock<CorsRegistry>()
        val registration = mock<CorsRegistration>()
        whenever(cors.addMapping(any())).thenReturn(registration)
        webConfig.configureRepositoryRestConfiguration(config, cors)
        verify(cors, times(1)).addMapping(any())
    }

    @Test
    fun `webConfig can be instantiated multiple times`() {
        val configs = (1..10).map { WebConfig() }
        configs.forEach { assertNotNull(it) }
    }

    @TestFactory
    fun `addCorsMappings does not register non-wildcard paths`(): List<DynamicTest> {
        val wrongPaths = listOf(
            "/api/**", "/v1/**", "/rest/**", "/*", "/",
            "/users/**", "/admin/**", "/public/**",
            "/health", "/metrics", "/info",
        )
        return wrongPaths.map { path ->
            dynamicTest("does not register '$path'") {
                val registry = mock<CorsRegistry>()
                val registration = mock<CorsRegistration>()
                whenever(registry.addMapping(any())).thenReturn(registration)
                webConfig.addCorsMappings(registry)
                verify(registry, never()).addMapping(path)
            }
        }
    }

    @TestFactory
    fun `configureRepositoryRestConfiguration does not register non-wildcard paths`(): List<DynamicTest> {
        val wrongPaths = listOf(
            "/api/**", "/v1/**", "/rest/**", "/*", "/",
            "/users/**", "/admin/**", "/public/**",
        )
        return wrongPaths.map { path ->
            dynamicTest("repo rest config does not register '$path'") {
                val config = mock<RepositoryRestConfiguration>()
                val cors = mock<CorsRegistry>()
                val registration = mock<CorsRegistration>()
                whenever(cors.addMapping(any())).thenReturn(registration)
                webConfig.configureRepositoryRestConfiguration(config, cors)
                verify(cors, never()).addMapping(path)
            }
        }
    }

    @TestFactory
    fun `multiple addCorsMappings calls are idempotent`(): List<DynamicTest> =
        (1..20).map { callCount ->
            dynamicTest("$callCount calls to addCorsMappings") {
                val registry = mock<CorsRegistry>()
                val registration = mock<CorsRegistration>()
                whenever(registry.addMapping(any())).thenReturn(registration)
                repeat(callCount) { webConfig.addCorsMappings(registry) }
                verify(registry, times(callCount)).addMapping("/**")
            }
        }

    @TestFactory
    fun `multiple configureRepositoryRestConfiguration calls`(): List<DynamicTest> =
        (1..20).map { callCount ->
            dynamicTest("$callCount calls to configureRepositoryRestConfiguration") {
                val config = mock<RepositoryRestConfiguration>()
                val cors = mock<CorsRegistry>()
                val registration = mock<CorsRegistration>()
                whenever(cors.addMapping(any())).thenReturn(registration)
                repeat(callCount) { webConfig.configureRepositoryRestConfiguration(config, cors) }
                verify(cors, times(callCount)).addMapping("/**")
            }
        }

    @TestFactory
    fun `new WebConfig instances all behave the same`(): List<DynamicTest> =
        (1..30).map { i ->
            dynamicTest("instance #$i behaves correctly") {
                val wc = WebConfig()
                val registry = mock<CorsRegistry>()
                val registration = mock<CorsRegistration>()
                whenever(registry.addMapping("/**")).thenReturn(registration)
                wc.addCorsMappings(registry)
                verify(registry).addMapping("/**")
            }
        }
}
