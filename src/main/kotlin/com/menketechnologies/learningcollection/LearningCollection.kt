package com.menketechnologies.learningcollection

import java.io.Serializable
import java.util.*
import jakarta.persistence.*

@Entity
@Table(name = "LearningCollection")
class LearningCollection : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    var id: Long = 0

    @Column
    lateinit var learning: String

    @Column
    lateinit var category: String

    @Column
    lateinit var dateAdded: Date


    constructor(learning: String, category: String, dateAdded: Date) {
        this.learning = learning
        this.category = category
        this.dateAdded = dateAdded
    }

    constructor()


    companion object {
        private const val serialVersionUID = 1L
    }
}
