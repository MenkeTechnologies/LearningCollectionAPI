package com.menketechnologies.learningcollection

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.servlet.http.HttpServletResponse

@RestController
class LearningController {
    @Autowired
    private lateinit var lcRepo: LCRepo

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/add")
    fun add(@RequestParam("learning") learning: String): LearningCollection {
        val learningCollection = LearningCollection()
        learningCollection.category = "programming"
        learningCollection.learning = learning
        learningCollection.dateAdded = Date()
        return lcRepo.save(learningCollection)
    }

    @GetMapping("/filter")
    fun filterLearn(@RequestParam("learning") learning: String) =
        lcRepo.findAllByLearningContaining(learning).map { it.learning }

    @GetMapping("/recents")
    fun learningItemRecentShortDefault() = lcRepo.findAll().map { it.learning }.reversed().take(20)

    @GetMapping("/recents/{count}")
    fun getLearningItemRecentShort(@PathVariable("count") count: Int) =
        lcRepo.findAll().map { it.learning }.reversed().take(count)

    @GetMapping("/recent/{count}")
    fun getLearningItemRecent(@PathVariable("count") count: Int) =
        lcRepo.findAll().reversed().take(count)

    @GetMapping("/dump")
    fun getDump(res: HttpServletResponse) {
        log.info("mysqldump to servlet output stream")
        res.writer.println(
            Runtime.getRuntime().exec("mysqldump --extended-insert=FALSE root").inputStream
                .bufferedReader().readText()
        )
    }

    @GetMapping("/randoms")
    fun learningItemCountShort() = lcRepo.findAll().shuffled().map { it.learning }.first()

    @GetMapping("/randoms/{count}")
    fun getLearningItemCountShort(@PathVariable("count") count: Int) =
        lcRepo.findAll().shuffled().take(count).map { it.learning }

    @GetMapping("/random/{count}")
    fun getLearningItemCount(@PathVariable("count") count: Int) =
        lcRepo.findAll().shuffled().take(count)

    @GetMapping("/random")
    fun learningItem() = lcRepo.findAll().shuffled().first()

}