package com.jakobmenke.lc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.spring.data.rest.configuration.SpringDataRestConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SpringBootApplication
@Slf4j
@EnableSwagger2WebMvc
@Import(SpringDataRestConfiguration.class)
public class SdrApplication {
    @Autowired
    LCRepo lcRepo;

    @RestController
    class RandomLearning {
        @GetMapping("/add")
        LearningCollection add(@RequestParam("learning") String learning) {

            LearningCollection learningCollection = new LearningCollection();
            learningCollection.setCategory("programming");
            learningCollection.setLearning(learning);
            learningCollection.setDateAdded(LocalDate.now());

            return lcRepo.save(learningCollection);
        }

        @GetMapping("/filter")
        List<String> filterLearn(@RequestParam("learning") String learning) {

            List<LearningCollection> ll = lcRepo.findAllByLearningContaining(learning);

            return ll.stream().map(LearningCollection::getLearning).collect(Collectors.toList());
        }

        @GetMapping("/recents/{count}")
        List<String> getLearningItemRecentShort(@PathVariable("count") Integer count) {

            List<LearningCollection> ll = StreamSupport.stream(lcRepo.findAll().spliterator(), true).collect(Collectors.toList());

            Collections.reverse(ll);

            return ll.stream().map(LearningCollection::getLearning).limit(count).collect(Collectors.toList());
        }

        @GetMapping("/recent/{count}")
        List<LearningCollection> getLearningItemRecent(@PathVariable("count") Integer count) {

            List<LearningCollection> ll = StreamSupport.stream(lcRepo.findAll().spliterator(), true).collect(Collectors.toList());

            Collections.reverse(ll);

            return ll.stream().limit(count).collect(Collectors.toList());
        }

        @GetMapping("/dump")
        void getDump(HttpServletResponse response) {
            log.info("mysqldump to servlet output stream");
            try (Scanner scanner = new Scanner(Runtime.getRuntime().exec("mysqldump --extended-insert=FALSE root").getInputStream())) {
                while (scanner.hasNextLine()) {
                    response.getOutputStream().write((scanner.nextLine() + "\n").getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @GetMapping("/randoms")
        String getLearningItemCountShort() {

            List<LearningCollection> ll = StreamSupport.stream(lcRepo.findAll().spliterator(), true).collect(Collectors.toList());

            Collections.shuffle(ll);

            return ll.get(0).getLearning();
        }

        @GetMapping("/randoms/{count}")
        List<String> getLearningItemCountShort(@PathVariable("count") Integer count) {

            List<LearningCollection> ll = StreamSupport.stream(lcRepo.findAll().spliterator(), true).collect(Collectors.toList());

            Collections.shuffle(ll);

            return ll.stream().map(LearningCollection::getLearning).limit(count).collect(Collectors.toList());
        }

        @GetMapping("/random/{count}")
        List<LearningCollection> getLearningItemCount(@PathVariable("count") Integer count) {

            List<LearningCollection> ll = StreamSupport.stream(lcRepo.findAll().spliterator(), true).collect(Collectors.toList());

            Collections.shuffle(ll);

            return ll.stream().limit(count).collect(Collectors.toList());
        }

        @GetMapping("/random")
        LearningCollection getLearningItem() {

            List<LearningCollection> ll = StreamSupport.stream(lcRepo.findAll().spliterator(), true).collect(Collectors.toList());

            Collections.shuffle(ll);

            return ll.get(0);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SdrApplication.class, args);
    }
}
