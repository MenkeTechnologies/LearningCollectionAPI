package com.menketechnologies.learningcollection

import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "LearningCollection")
class LearningCollection : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column
    lateinit var learning: String

    @Column
    lateinit var category: String

    @Column
    lateinit var dateAdded: Date

    companion object {
        private const val serialVersionUID = 1L
    }
}
