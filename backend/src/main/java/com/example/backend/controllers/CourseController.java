// src/main/java/com/example/backend/controllers/CourseController.java
package com.example.backend.controllers;

import com.example.backend.entity.Course;
import com.example.backend.services.CourseService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/retrieve-all-courses")
    public List<Course> getCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/retrieve-course/{course-id}")
    public Course retrieveCourse(@PathVariable("course-id") Long courseId) {
        return courseService.retrieveCourse(courseId);
    }

    @PostMapping("/add-course")
    public Course addCourse(@RequestBody Course course) {
        return courseService.addCourse(course);
    }

    @DeleteMapping("/remove-course/{course-id}")
    public void removeCourse(@PathVariable("course-id") Long courseId) {
        courseService.deleteCourse(courseId);
    }

    @PutMapping("/modify-course")
    public Course modifyCourse(@RequestBody Course course) {
        return courseService.modifyCourse(course);
    }
}
