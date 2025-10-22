package com.example.backend.controllers;

import com.example.backend.entity.Lesson;
import com.example.backend.services.LessonService;
import com.example.backend.ai.AiLocalService; // 🔹 import du service IA
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/lesson")
public class LessonController {

    private final LessonService lessonService;
    private final AiLocalService aiLocalService; // 🔹 injection du service IA

    @GetMapping("/retrieve-all-lessons")
    public List<Lesson> getLessons() {
        return lessonService.getAllLessons();
    }

    @GetMapping("/retrieve-lesson/{lesson-id}")
    public Lesson retrieveLesson(@PathVariable("lesson-id") Long lessonId) {
        return lessonService.retrieveLesson(lessonId);
    }

    @GetMapping("/by-course/{course-id}")
    public List<Lesson> getLessonsByCourse(@PathVariable("course-id") Long courseId) {
        return lessonService.getLessonsByCourse(courseId);
    }

    @PostMapping("/add-lesson/{course-id}")
    public Lesson addLesson(@PathVariable("course-id") Long courseId,
                            @RequestBody Lesson lesson) {
        return lessonService.addLesson(courseId, lesson);
    }

    @DeleteMapping("/remove-lesson/{lesson-id}")
    public void removeLesson(@PathVariable("lesson-id") Long lessonId) {
        lessonService.deleteLesson(lessonId);
    }

    @PutMapping("/modify-lesson")
    public Lesson modifyLesson(@RequestBody Lesson lesson) {
        return lessonService.modifyLesson(lesson);
    }

    @PostMapping("/autosummary/{lesson-id}")
    public ResponseEntity<?> autoSummary(@PathVariable("lesson-id") Long lessonId) {
        Lesson lesson = lessonService.retrieveLesson(lessonId);
        if (lesson == null || lesson.getContent() == null || lesson.getContent().isEmpty()) {
            return ResponseEntity.badRequest().body("Le contenu de la leçon est vide !");
        }

        // 🔹 Appel au service IA
        String summary = aiLocalService.summarize(lesson.getContent());

        // 🔹 Créer une réponse temporaire (sans sauvegarde DB)
        return ResponseEntity.ok(new Object() {
            public final Long id = lesson.getId();
            public final String title = lesson.getTitle();
            public final String content = lesson.getContent();
            public final String ai_summary = summary;
        });
    }

}
