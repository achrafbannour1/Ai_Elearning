package com.example.backend.ai;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SyllabusService {

    private static final Map<String, List<String>> LEVEL_OBJECTIVES = Map.of(
            "debutant", List.of("Comprendre les bases", "Acquérir le vocabulaire clé", "Réaliser des exercices guidés"),
            "intermediaire", List.of("Approfondir les concepts", "Mettre en pratique sur des cas réels", "Analyser et corriger les erreurs"),
            "avance", List.of("Maîtriser les patterns avancés", "Optimiser les performances", "Concevoir un mini-projet abouti")
    );

    public SyllabusResponse generate(SyllabusRequest req) {
        String title = req.getTitle() == null ? "Nouveau cours" : req.getTitle().trim();
        String level = normalizeLevel(req.getLevel());
        int duration = Math.max(1, Math.min(24, req.getDuration())); // 1..24 semaines

        // Heuristique simple : nombre de modules ≈ sqrt(semaines)+2
        int modulesCount = Math.max(3, (int) Math.round(Math.sqrt(duration) + 2));
        // Leçons par module : 2 à 5 selon la durée
        int lessonsPerModule = Math.max(2, Math.min(5, (duration / modulesCount) + 2));

        List<String> baseTracks = inferTracksFromTitle(title);

        List<ModuleDto> modules = new ArrayList<>();
        for (int i = 1; i <= modulesCount; i++) {
            String track = baseTracks.get((i - 1) % baseTracks.size());
            String moduleTitle = String.format("Module %d — %s", i, prettifyTrack(track));

            List<String> objectives = new ArrayList<>(LEVEL_OBJECTIVES.get(level));
            objectives.add("Relier théorie et pratique");
            if (i == 1) objectives.add("Installer l’environnement et découvrir l’écosystème");

            List<LessonDto> lessons = new ArrayList<>();
            for (int j = 1; j <= lessonsPerModule; j++) {
                String lTitle = lessonTitle(track, i, j, level);
                List<String> outcomes = List.of(
                        "Savoir expliquer le concept",
                        "L’appliquer sur un exemple concret",
                        "Identifier erreurs fréquentes et solutions"
                );
                lessons.add(new LessonDto(lTitle, outcomes));
            }

            List<String> exercises = List.of(
                    "Exercice guidé (pas à pas)",
                    "Mini-TP d’application",
                    "Quiz de validation"
            );

            modules.add(new ModuleDto(moduleTitle, objectives, lessons, exercises));
        }

        return new SyllabusResponse(title, level, req.getAudience(), duration, modules);
    }

    private String normalizeLevel(String level) {
        if (level == null) return "debutant";
        String l = level.toLowerCase(Locale.ROOT);
        if (l.contains("avan")) return "avance";
        if (l.contains("inter")) return "intermediaire";
        return "debutant";
    }

    private List<String> inferTracksFromTitle(String title) {
        String t = title.toLowerCase(Locale.ROOT);
        List<String> tracks = new ArrayList<>();
        if (t.contains("spring")) tracks.addAll(List.of("intro-spring", "rest-api", "data-jpa", "securite", "tests"));
        if (t.contains("angular")) tracks.addAll(List.of("intro-angular", "components", "services-http", "forms", "routing"));
        if (t.contains("docker")) tracks.addAll(List.of("intro-docker", "images", "containers", "compose", "ci-cd"));
        if (t.contains("graphql")) tracks.addAll(List.of("intro-graphql", "queries", "mutations", "schema-design", "security"));
        if (tracks.isEmpty()) tracks.addAll(List.of("fondamentaux", "pratique", "outillage", "bonnes-pratiques", "projet"));
        // dédoublonner
        return tracks.stream().distinct().collect(Collectors.toList());
    }

    private String prettifyTrack(String track) {
        return switch (track) {
            case "intro-spring" -> "Introduction à Spring";
            case "rest-api" -> "REST API & Controllers";
            case "data-jpa" -> "Data & JPA/Hibernate";
            case "securite" -> "Sécurité & Auth";
            case "tests" -> "Tests & Qualité";
            case "intro-angular" -> "Introduction à Angular";
            case "components" -> "Components & Templates";
            case "services-http" -> "Services & HTTP";
            case "forms" -> "Formulaires (Reactive/Template)";
            case "routing" -> "Routing & Guards";
            case "intro-docker" -> "Introduction à Docker";
            case "images" -> "Images & Registry";
            case "containers" -> "Containers & Réseau";
            case "compose" -> "Docker Compose";
            case "ci-cd" -> "CI/CD & Delivery";
            case "intro-graphql" -> "Introduction à GraphQL";
            case "queries" -> "Queries & Fetch";
            case "mutations" -> "Mutations & CUD";
            case "schema-design" -> "Schema Design";
            case "security" -> "Sécurité GraphQL";
            case "fondamentaux" -> "Fondamentaux";
            case "pratique" -> "Mise en pratique";
            case "outillage" -> "Outils & Productivité";
            case "bonnes-pratiques" -> "Bonnes pratiques";
            case "projet" -> "Mini-projet de synthèse";
            default -> capitalize(track.replace("-", " "));
        };
    }

    private String lessonTitle(String track, int i, int j, String level) {
        String base = prettifyTrack(track);
        String suffix = switch (level) {
            case "debutant" -> "Bases";
            case "intermediaire" -> "Approfondissement";
            default -> "Avancé";
        };
        return String.format("%s — Séance %d.%d (%s)", base, i, j, suffix);
    }

    private String capitalize(String s) {
        if (s.isBlank()) return s;
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}
