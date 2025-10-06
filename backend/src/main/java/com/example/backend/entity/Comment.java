package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    @Temporal(TemporalType.TIMESTAMP)
    private Date DateComment;
    //@Enumerated(EnumType.STRING)
    //private VoteComment VoteComment;
    @Enumerated(EnumType.STRING) // Store reaction as string
    private LikePost reaction;
    @OneToMany (cascade = CascadeType.ALL , orphanRemoval = true )
    private Set<Comment> Reponse;
}
