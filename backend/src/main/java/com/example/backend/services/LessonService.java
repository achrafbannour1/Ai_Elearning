// src/main/java/com/example/backend/services/LessonService.java
package com.example.backend.services;

import com.example.backend.entity.Course;
import com.example.backend.entity.Lesson;
import com.example.backend.repository.CourseRepository;
import com.example.backend.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }

    public List<Lesson> getLessonsByCourse(Long courseId) {
        // Vérifie que le course existe (optionnel mais utile)
        if (!courseRepository.existsById(courseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Course with ID " + courseId + " not found");
        }
        return lessonRepository.findByCourseId(courseId);
    }

    public Lesson addLesson(Long courseId, Lesson lesson) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Course with ID " + courseId + " not found"));

        lesson.setCourse(course);
        return lessonRepository.save(lesson);
    }

    public Lesson modifyLesson(Lesson lesson) {
        Lesson existing = lessonRepository.findById(lesson.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Lesson with ID " + lesson.getId() + " not found"));

        if (lesson.getTitle() != null)  existing.setTitle(lesson.getTitle());
        if (lesson.getContent() != null) existing.setContent(lesson.getContent());

        // Déplacement vers un autre course si fourni dans le body
        if (lesson.getCourse() != null && lesson.getCourse().getId() != null) {
            Long newCourseId = lesson.getCourse().getId();
            Course newCourse = courseRepository.findById(newCourseId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Course with ID " + newCourseId + " not found"));
            existing.setCourse(newCourse);
        }

        return lessonRepository.save(existing);
    }

    public void deleteLesson(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Lesson with ID " + lessonId + " not found");
        }
        lessonRepository.deleteById(lessonId);
    }

    public Lesson retrieveLesson(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Lesson with ID " + lessonId + " not found"));
    }
}
