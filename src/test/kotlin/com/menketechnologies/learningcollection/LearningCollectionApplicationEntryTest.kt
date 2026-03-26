package com.menketechnologies.learningcollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.boot.autoconfigure.SpringBootApplication

class LearningCollectionApplicationEntryTest {

    @Test
    fun `application class exists`() {
        assertNotNull(LearningCollectionApplication::class.java)
    }

    @Test
    fun `application class has SpringBootApplication annotation`() {
        val annotation = LearningCollectionApplication::class.java.getAnnotation(SpringBootApplication::class.java)
        assertNotNull(annotation)
    }

    @Test
    fun `application class has companion object with main`() {
        val companion = LearningCollectionApplication::class.java.declaredClasses
        assertTrue(companion.any { it.simpleName == "Companion" })
    }

    @Test
    fun `application main method exists on companion`() {
        val companionClass = LearningCollectionApplication.Companion::class.java
        val mainMethod = companionClass.getDeclaredMethod("main", Array<String>::class.java)
        assertNotNull(mainMethod)
    }

    @Test
    fun `main method has JvmStatic annotation`() {
        val method = LearningCollectionApplication::class.java.getDeclaredMethod("main", Array<String>::class.java)
        val jvmStatic = method.annotations.any { it.annotationClass.simpleName == "JvmStatic" }
        assertTrue(jvmStatic || method.declaringClass == LearningCollectionApplication::class.java)
    }

    @Test
    fun `application class is instantiable`() {
        val instance = LearningCollectionApplication()
        assertNotNull(instance)
    }

    @TestFactory
    fun `application class can be instantiated multiple times`(): List<DynamicTest> =
        (1..100).map { i ->
            dynamicTest("instantiation #$i") {
                val instance = LearningCollectionApplication()
                assertNotNull(instance)
            }
        }

    @Test
    fun `SpringBootApplication has default scan base packages`() {
        val annotation = LearningCollectionApplication::class.java.getAnnotation(SpringBootApplication::class.java)
        assertNotNull(annotation)
        assertTrue(annotation.scanBasePackages.isEmpty())
    }

    @Test
    fun `application is in correct package`() {
        assertEquals("com.menketechnologies.learningcollection", LearningCollectionApplication::class.java.`package`.name)
    }

    @TestFactory
    fun `entity class is in same package as application`(): List<DynamicTest> {
        val classes = listOf(
            "LearningCollection" to LearningCollection::class.java,
            "LearningController" to LearningController::class.java,
        )
        return classes.map { (name, clazz) ->
            dynamicTest("$name is in same package as application") {
                assertEquals(
                    LearningCollectionApplication::class.java.`package`.name,
                    clazz.`package`.name,
                )
            }
        }
    }

    @Test
    fun `LCRepo interface is in same package`() {
        assertEquals(
            LearningCollectionApplication::class.java.`package`.name,
            LCRepo::class.java.`package`.name,
        )
    }

    @Test
    fun `WebConfig is in same package`() {
        assertEquals(
            LearningCollectionApplication::class.java.`package`.name,
            WebConfig::class.java.`package`.name,
        )
    }
}
