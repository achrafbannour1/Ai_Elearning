// src/main/java/com/example/backend/services/CourseService.java
package com.example.backend.services;

import com.example.backend.entity.Course;
import com.example.backend.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course addCourse(Course course) {
        // On ignore course.getUser() côté CRUD (pas d'obligation d'envoyer un user)
        return courseRepository.save(course);
    }

    public Course modifyCourse(Course course) {
        Course existing = courseRepository.findById(course.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Course with ID " + course.getId() + " not found"));

        existing.setTitle(course.getTitle());
        existing.setDescription(course.getDescription());
        // On ne touche pas aux lessons ici (elles ont leurs endpoints)
        // On n’impose pas de user ici (sans user dans le CRUD)
        return courseRepository.save(existing);
    }

    public void deleteCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Course with ID " + courseId + " not found");
        }
        courseRepository.deleteById(courseId);
    }

    public Course retrieveCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Course with ID " + courseId + " not found"));
    }
}
