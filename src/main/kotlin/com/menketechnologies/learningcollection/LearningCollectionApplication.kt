package com.menketechnologies.learningcollection

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class LearningCollectionApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(LearningCollectionApplication::class.java, *args)
        }
    }
}
