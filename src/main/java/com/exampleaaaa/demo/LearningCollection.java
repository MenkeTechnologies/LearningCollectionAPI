package com.exampleaaaa.demo;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "LearningCollection")
@Data
public class LearningCollection implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column
    String learning;

    @Column
    String category;

    @Column
    LocalDate dateAdded;

}
