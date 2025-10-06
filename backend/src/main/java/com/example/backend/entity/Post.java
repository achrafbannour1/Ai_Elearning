package com.example.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private String Subject;
    private String Picture;
    private Boolean isAnonymous;
    private String archivedReason ;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_post", nullable = false, updatable = false)
    @CreationTimestamp
    private Date datePost;
    @Enumerated(EnumType.STRING)
    private LikePost likePost ;

    @OneToMany (cascade = CascadeType.ALL)
    private Set<Comment> Comments;

    //@Enumerated(EnumType.STRING)
    //private StatusComplaint status = StatusComplaint.Pending;

    @ManyToOne
    private User user;

}
