import { Component, OnInit, OnDestroy } from '@angular/core';
import { VoiceCoachService, Exercise, VoiceAnalysisResponse } from '../../services/voice-coach.service';

@Component({
  selector: 'app-voice-coach',
  templateUrl: './voice-coach.component.html',
  styleUrls: ['./voice-coach.component.css']
})
export class VoiceCoachComponent implements OnInit, OnDestroy {

  // Recording
  mediaRecorder!: MediaRecorder;
  audioChunks: Blob[] = [];
  audioUrl?: string;
  isRecording = false;
  recordingTime = 0;
  recordingInterval: any;

  // Exercises
  exercises: Exercise[] = [];
  selectedExercise?: Exercise;
  currentSentence = '';
  selectedDifficulty = 'All';

  // Analysis Results
  analysisResult?: VoiceAnalysisResponse;
  isAnalyzing = false;
  hasRecorded = false;

  // Text-to-Speech
  exampleAudioUrl?: string;
  isPlayingExample = false;

  // History
  attemptHistory: any[] = [];

  // Configuration Status
  backendReachable = false;
  apiConfigured = false;
  showConfigWarning = false;

  constructor(private voiceCoachService: VoiceCoachService) { }

  ngOnInit() {
    this.exercises = this.voiceCoachService.getExercises();
    this.checkBackendHealth();
    this.selectRandomExercise();
  }

  ngOnDestroy() {
    if (this.recordingInterval) {
      clearInterval(this.recordingInterval);
    }
  }

  /**
   * Check if backend is reachable and properly configured
   */
  checkBackendHealth() {
    // Check backend health
    this.voiceCoachService.checkHealth().subscribe({
      next: (response) => {
        this.backendReachable = true;
        console.log('✅ Backend is reachable:', response);
        
        // Check API configuration
        this.voiceCoachService.checkConfiguration().subscribe({
          next: (config) => {
            this.apiConfigured = config.configured;
            if (!this.apiConfigured) {
              this.showConfigWarning = true;
              console.warn('⚠️ OpenAI API not configured:', config.message);
            } else {
              console.log('✅ OpenAI API configured');
            }
          },
          error: (err) => {
            console.error('Error checking configuration:', err);
          }
        });
      },
      error: (err) => {
        this.backendReachable = false;
        this.showConfigWarning = true;
        console.error('❌ Backend not reachable:', err);
        console.error('Make sure backend is running on http://localhost:8083');
      }
    });
  }

  /**
   * Select a random exercise
   */
  selectRandomExercise() {
    this.selectedExercise = this.voiceCoachService.getRandomExercise();
    this.currentSentence = this.selectedExercise.sentence;
    this.resetAnalysis();
  }

  /**
   * Select specific exercise
   */
  selectExercise(exercise: Exercise) {
    this.selectedExercise = exercise;
    this.currentSentence = exercise.sentence;
    this.resetAnalysis();
  }

  /**
   * Filter exercises by difficulty
   */
  filterByDifficulty(difficulty: string) {
    this.selectedDifficulty = difficulty;
    if (difficulty === 'All') {
      this.exercises = this.voiceCoachService.getExercises();
    } else {
      this.exercises = this.voiceCoachService.getExercisesByDifficulty(difficulty);
    }
  }
  /**
   * Fallback: Use browser's built-in speech synthesis
   */
  playExampleWithBrowserTTS() {
    if (!this.currentSentence) return;

    if ('speechSynthesis' in window) {
      this.isPlayingExample = true;
      
      // Cancel any ongoing speech
      window.speechSynthesis.cancel();
      
      const utterance = new SpeechSynthesisUtterance(this.currentSentence);
      utterance.lang = 'en-US';
      utterance.rate = 0.85; // Slightly slower for learning
      utterance.pitch = 1.0;
      utterance.volume = 1.0;
      
      // Calculate estimated duration (rough estimate: 150 words per minute)
      const words = this.currentSentence.split(' ').length;
      const estimatedDuration = (words / 150) * 60 * 1000 / 0.85; // Adjusted for rate
      
      utterance.onend = () => {
        this.isPlayingExample = false;
      };
      
      utterance.onerror = () => {
        this.isPlayingExample = false;
        alert('Could not play audio. Please check your browser settings.');
      };
      
      window.speechSynthesis.speak(utterance);
      
      // Fallback timeout to reset state (in case onend doesn't fire)
      setTimeout(() => {
        this.isPlayingExample = false;
      }, estimatedDuration + 1000); // Add 1 second buffer
    } else {
      alert('Text-to-speech is not supported in your browser.\n\nPlease:\n1. Check if backend is running on port 8083\n2. Verify OpenAI API key in application.properties\n3. See TROUBLESHOOTING_VOICE_COACH.md for help');
    }
  }

  /**
   * Start recording audio
   */
  startRecording() {
    // Reset previous results when starting a new recording
    this.analysisResult = undefined;
    
    navigator.mediaDevices.getUserMedia({ audio: true })
      .then(stream => {
        this.mediaRecorder = new MediaRecorder(stream);
        this.audioChunks = [];
        this.isRecording = true;
        this.recordingTime = 0;
        this.hasRecorded = false;

        // Update recording time
        this.recordingInterval = setInterval(() => {
          this.recordingTime++;
        }, 1000);

        this.mediaRecorder.ondataavailable = (e) => {
          this.audioChunks.push(e.data);
        };

        this.mediaRecorder.onstop = () => {
          const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
          this.audioUrl = URL.createObjectURL(audioBlob);
          this.hasRecorded = true;

          // Stop all tracks to release microphone
          stream.getTracks().forEach(track => track.stop());

          // Automatically analyze after recording stops
          this.analyzePronunciation(audioBlob);
          this.isAnalyzing = false;
        };

        this.mediaRecorder.start();
      })
      .catch(err => {
        console.error('Error accessing microphone:', err);
        alert('Could not access microphone. Please check your permissions.');
        this.isRecording = false;
      });
  }

  /**
   * Stop recording audio
   */
  stopRecording() {
    if (this.mediaRecorder && this.isRecording) {
      this.mediaRecorder.stop();
      this.isRecording = false;
      if (this.recordingInterval) {
        clearInterval(this.recordingInterval);
      }
    }
  }

  /**
   * Analyze pronunciation
   */
  analyzePronunciation(audioBlob: Blob) {
    if (!this.currentSentence) {
      alert('Please select a sentence first!');
      return;
    }

    this.isAnalyzing = true;
    this.voiceCoachService.analyzePronunciation(audioBlob, this.currentSentence).subscribe({
      next: (result) => {
        this.analysisResult = result;
        this.isAnalyzing = false;

        // Add to history
        this.attemptHistory.unshift({
          sentence: this.currentSentence,
          score: parseFloat(result.score.toFixed(2)),
          timestamp: new Date()
        });

        // Keep only last 10 attempts
        if (this.attemptHistory.length > 10) {
          this.attemptHistory.pop();
        }
      },
      error: (err) => {
        console.error('Error analyzing pronunciation:', err);
        this.isAnalyzing = false;
        alert('Error analyzing pronunciation. Please try again.');
      }
    });
  }

  /**
   * Reset analysis and recording
   */
  resetAnalysis() {
    this.analysisResult = undefined;
    this.audioUrl = undefined;
    this.hasRecorded = false;
    this.isAnalyzing = false;
  }

  /**
   * Get score color based on value
   */
  getScoreColor(score: number): string {
    if (score >= 90) return '#4CAF50'; // Green
    if (score >= 70) return '#FFC107'; // Yellow
    if (score >= 50) return '#FF9800'; // Orange
    return '#F44336'; // Red
  }

  /**
   * Get score label
   */
  getScoreLabel(score: number): string {
    if (score >= 90) return 'Excellent!';
    if (score >= 70) return 'Good!';
    if (score >= 50) return 'Fair';
    return 'Needs Practice';
  }

  /**
   * Format recording time
   */
  formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }
}