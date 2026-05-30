package com.menketechnologies.learningcollection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * Round-4 schema contract pins for the JPA entity LearningCollection.
 * Drift in @Table name, @Column field set, @GeneratedValue strategy,
 * or the IDENTITY-vs-SEQUENCE choice would silently break the existing
 * MySQL database (LearningCollection table holds years of saved data)
 * and force every external caller to migrate.
 *
 * Existing tests verify entity behavior under reflection (constructors,
 * fields). This file pins the JPA mapping annotations that determine
 * the physical schema layout.
 */
class LearningCollectionSchemaContractTest {

    private val entityClass = LearningCollection::class.java

    @Test
    fun `entity is annotated with @Entity (JPA-managed)`() {
        // Pin: removing @Entity would silently drop the class from the
        // EntityManager's persistence context — saves would no-op.
        assertNotNull(entityClass.getAnnotation(Entity::class.java),
            "LearningCollection must remain a JPA entity")
    }

    @Test
    fun `entity @Table name is exactly "LearningCollection" (DB compat floor)`() {
        // Pin: this is the legacy table name MenkeTechnologies' MySQL
        // instance has used for years of saved learnings. Renaming it
        // (even to a "better" snake_case form) would orphan all history.
        val tableAnnot = entityClass.getAnnotation(Table::class.java)
        assertNotNull(tableAnnot, "LearningCollection must have @Table annotation")
        assertEquals("LearningCollection", tableAnnot.name,
            "@Table name drift would orphan existing MySQL data")
    }

    @Test
    fun `id field uses GenerationType IDENTITY (not SEQUENCE or TABLE)`() {
        // Pin: IDENTITY uses MySQL's AUTO_INCREMENT. SEQUENCE would
        // require a sequence object MySQL doesn't natively support
        // before 8.0; TABLE is portable but adds a write round-trip
        // per insert. The current MySQL backend depends on IDENTITY.
        val idField = entityClass.getDeclaredField("id")
        assertNotNull(idField.getAnnotation(Id::class.java),
            "id field must carry @Id")
        val gv = idField.getAnnotation(GeneratedValue::class.java)
        assertNotNull(gv, "id field must carry @GeneratedValue")
        assertEquals(GenerationType.IDENTITY, gv.strategy,
            "id GenerationType must be IDENTITY — MySQL AUTO_INCREMENT compat")
    }

    @Test
    fun `entity declares exactly four @Column fields id learning category dateAdded`() {
        // Pin: the persistent attribute set is closed. Adding a new
        // @Column without an Alembic-style migration would force every
        // running instance to fail at startup with "Unknown column 'x'".
        // Removing one would drop a year of saved data on next migration.
        val expectedColumnFields = setOf("id", "learning", "category", "dateAdded")
        val actualColumnFields = entityClass.declaredFields
            .filter { it.getAnnotation(Column::class.java) != null }
            .map { it.name }
            .toSet()
        assertEquals(expectedColumnFields, actualColumnFields,
            "@Column field set drift — schema migration needed")
    }

    @Test
    fun `entity implements Serializable (cross-JVM transport contract)`() {
        // Pin: the entity implements Serializable so it can flow through
        // a second-level cache or cross-JVM RMI invocation. Dropping the
        // marker interface would prevent caching frameworks from storing
        // entity instances and silently degrade query performance.
        assertTrue(java.io.Serializable::class.java.isAssignableFrom(entityClass),
            "LearningCollection must implement Serializable")
    }
}
