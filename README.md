# 🎓 Plateforme E-Learning Intelligente avec IA

Une plateforme d'apprentissage en ligne moderne intégrant l'intelligence artificielle pour améliorer l'expérience utilisateur et faciliter la gestion administrative.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-brightgreen)
![Angular](https://img.shields.io/badge/Angular-16-red)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![AI](https://img.shields.io/badge/AI-LLaMA%203%20%7C%20Hugging%20Face-orange)

## 📋 Table des Matières

- [Présentation](#présentation)
- [Fonctionnalités](#fonctionnalités)
- [Technologies](#technologies)
- [Modules IA](#modules-ia)
- [Installation](#installation)
- [Utilisation](#utilisation)
- [Architecture](#architecture)
- [Captures d'écran](#captures-décran)
- [Contributeurs](#contributeurs)
- [Licence](#licence)

## 🎯 Présentation

Cette plateforme e-learning de nouvelle génération combine les technologies web modernes avec l'intelligence artificielle pour offrir une expérience d'apprentissage enrichie et personnalisée. Le projet intègre plusieurs modules d'IA pour automatiser les tâches répétitives et améliorer l'interaction utilisateur.

### Objectifs du Projet

- ✅ Créer une plateforme e-learning complète et moderne
- ✅ Intégrer l'IA pour améliorer l'expérience utilisateur
- ✅ Automatiser la création de contenu (examens, quiz, images)
- ✅ Faciliter la gestion administrative
- ✅ Offrir une assistance intelligente via chatbot

## ⚡ Fonctionnalités

### Gestion des Utilisateurs
- 👤 Inscription et authentification sécurisée (JWT)
- 🔐 Gestion des rôles (Admin, Enseignant, Étudiant)
- 📊 Profils personnalisés

### Gestion des Cours
- 📚 Création et gestion de cours
- 📝 Organisation par chapitres et modules
- 📥 Téléchargement de ressources pédagogiques
- 🎯 Génération automatique de syllabus avec IA

### Gestion des Examens
- ✏️ Génération automatique d'examens via IA (Gemini API)
- 📋 QCM, questions ouvertes, vrai/faux
- ✅ Correction automatique
- 🔄 Régénération rapide des examens
- 📊 Feedback personnalisé

### Gestion des Événements
- 📅 Création et gestion de conférences/séminaires
- 👥 Inscription des étudiants
- 🖼️ **Génération automatique d'images avec IA (Hugging Face)**
- 📧 Notifications automatiques

### Quiz Intelligents
- 🧠 Génération de quiz via T5 (Transformers.js)
- 🎮 Interface interactive
- 📈 Suivi des scores
- 💡 Explications des réponses

### Assistant Vocal (Voice Coach)
- 🎤 Exercices de prononciation
- 🗣️ Analyse vocale en temps réel
- 💬 Feedback personnalisé via IA (Gemini)
- 📊 Suivi de progression

### Paiement et Abonnements
- 💳 Intégration Stripe
- 📊 Prédiction des revenus avec IA (Python/ML)
- 💰 Gestion des abonnements
- 📈 Tableau de bord analytics

### 🤖 Chatbot Intelligent (LLaMA 3)
- 💬 Assistant conversationnel intelligent
- 🎙️ Mode vocal intégré (Speech-to-Text)
- ✅ Inscription automatique aux événements
- 🧭 Navigation contextuelle
- 💡 Recommandations personnalisées
- 🔒 Hébergement local pour confidentialité

## 🛠️ Technologies

### Backend
- **Framework:** Spring Boot 3.0
- **Langage:** Java 17
- **Base de données:** MySQL 8.0
- **Sécurité:** Spring Security + JWT
- **AI/ML:** Python (pour prédiction de revenus)
- **API IA:** Gemini API, Hugging Face API

### Frontend
- **Framework:** Angular 16
- **UI/UX:** Angular Material, Tailwind CSS
- **Charts:** ng2-charts
- **HTTP:** HttpClient
- **Paiement:** Stripe SDK

### Intelligence Artificielle
- **Chatbot:** LLaMA 3 via Ollama (local)
- **Génération d'examens:** Gemini API
- **Génération d'images:** Hugging Face API (Stable Diffusion)
- **Quiz IA:** Transformers.js avec T5
- **Voice Coach:** Gemini API
- **Prédiction:** scikit-learn, pandas (Python)

### Outils de Développement
- **IDE:** IntelliJ IDEA, VS Code
- **Version Control:** Git & GitHub
- **Tests:** JUnit, GitHub Copilot
- **Qualité du code:** SonarQube AI
- **AI Assistant:** Grok, GitHub Copilot

## 🤖 Modules IA

### 1. Chatbot Intelligent (LLaMA 3 & Ollama)

Un assistant conversationnel intelligent hébergé localement pour garantir la confidentialité.

**Fonctionnalités:**
- Réponses contextuelles en temps réel
- Inscription automatique aux événements
- Recommandations personnalisées
- Mode vocal (Speech-to-Text)
- Navigation intelligente

**Technologies:**
- LLaMA 3 via Ollama (localhost:11434)
- Spring Boot (OllamaService, OllamaController)
- Angular 16 (interface + mode vocal)

### 2. Générateur d'Images IA (Hugging Face)

Automatise la création d'images pour les événements via l'API Hugging Face.

**Fonctionnalités:**
- Génération automatique depuis descriptions
- Intégration directe dans le formulaire d'ajout
- Régénération possible
- Images de haute qualité

**Technologies:**
- API Hugging Face (Stable Diffusion)
- Spring Boot (HuggingFaceService)
- Angular (intégration frontend)

### 3. Générateur d'Examens IA (Gemini)

Assiste les enseignants dans la création automatique d'examens.

**Fonctionnalités:**
- Génération selon paramètres (sujet, difficulté, type)
- Régénération rapide
- Format JSON structuré
- Correction automatique

### 4. Quiz Intelligent (T5 - Transformers.js)

Génère des quiz interactifs à partir de contenu pédagogique.

**Fonctionnalités:**
- Exécution locale dans le navigateur
- QCM avec explications
- Analyse automatique du contenu
- Sans dépendance serveur

### 5. Voice Coach IA

Coach vocal pour améliorer la prononciation.

**Fonctionnalités:**
- Analyse vocale en temps réel
- Feedback personnalisé (Gemini API)
- Exercices adaptatifs
- Suivi de progression

### 6. Prédiction des Revenus

Modèle ML pour prédire les revenus futurs de la plateforme.

**Fonctionnalités:**
- Analyse des données Stripe
- Régression linéaire
- Prévisions mensuelles/annuelles
- Tableau de bord analytics

## 📦 Installation

### Prérequis
```bash
