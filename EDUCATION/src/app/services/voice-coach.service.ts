import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface VoiceAnalysisResponse {
  transcription: string;
  originalText: string;
  score: number;
  feedback: string;
  pronunciationErrors: string[];
  suggestions: string[];
}

export interface Exercise {
  id: number;
  sentence: string;
  difficulty: string;
  category: string;
}

@Injectable({
  providedIn: 'root'
})
export class VoiceCoachService {
  private apiUrl = 'http://localhost:8083/api/voice';

  // Meaningful, real-world phrases for pronunciation practice
  exercises: Exercise[] = [
    { id: 1, sentence: 'Could you please help me with my homework?', difficulty: 'Easy', category: 'Request' },
    { id: 2, sentence: 'I would like a cup of coffee, please.', difficulty: 'Easy', category: 'Daily Life' },
    { id: 3, sentence: 'What time does the train leave for Paris?', difficulty: 'Medium', category: 'Travel' },
    { id: 4, sentence: 'I am looking forward to meeting you tomorrow.', difficulty: 'Medium', category: 'Social' },
    { id: 5, sentence: 'Can you recommend a good restaurant nearby?', difficulty: 'Medium', category: 'Conversation' },
    { id: 6, sentence: 'The weather forecast says it will rain this weekend.', difficulty: 'Medium', category: 'Weather' },
    { id: 7, sentence: 'Please let me know if you have any questions.', difficulty: 'Easy', category: 'Professional' },
    { id: 8, sentence: 'I apologize for the inconvenience caused.', difficulty: 'Hard', category: 'Professional' },
    { id: 9, sentence: 'Learning a new language opens many doors.', difficulty: 'Easy', category: 'Education' },
    { id: 10, sentence: 'Thank you for your time and consideration.', difficulty: 'Easy', category: 'Politeness' }
  ];

  constructor(private http: HttpClient) { }

  /**
   * Analyze pronunciation by sending audio to backend
   */
  analyzePronunciation(audioBlob: Blob, originalText: string): Observable<VoiceAnalysisResponse> {
    const formData = new FormData();
    formData.append('file', audioBlob, 'recording.webm');
    formData.append('originalText', originalText);

    return this.http.post<VoiceAnalysisResponse>(`${this.apiUrl}/analyze`, formData);
  }

  /**
   * Get text-to-speech audio for a given text
   */
  getTextToSpeech(text: string): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/text-to-speech`, { text }, {
      responseType: 'blob'
    });
  }

  /**
   * Check if backend is reachable
   */
  checkHealth(): Observable<string> {
    return this.http.get(`${this.apiUrl}/health`, { responseType: 'text' });
  }

  /**
   * Check if OpenAI API is configured
   */
  checkConfiguration(): Observable<any> {
    return this.http.get(`${this.apiUrl}/config-check`);
  }

  /**
   * Get all available exercises
   */
  getExercises(): Exercise[] {
    return this.exercises;
  }

  /**
   * Get exercises by difficulty
   */
  getExercisesByDifficulty(difficulty: string): Exercise[] {
    return this.exercises.filter(ex => ex.difficulty === difficulty);
  }

  /**
   * Get a random exercise
   */
  getRandomExercise(): Exercise {
    const randomIndex = Math.floor(Math.random() * this.exercises.length);
    return this.exercises[randomIndex];
  }
}
