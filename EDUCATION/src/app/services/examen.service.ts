import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface Examen {
  topic: string;
  numMCQ: string;
  numTrueFalse: string;
  numShortAnswer: string;
  difficulty: string;
}

@Injectable({
  providedIn: 'root'
})
export class ExamenService {

  constructor(private http: HttpClient) { }

  private baseUrl = 'http://localhost:8083/api/exam';  

  generateExamen(examen: Examen): Observable<any> {
    // Create FormData object
    const formData = new FormData();
    formData.append('text', examen.topic);
    formData.append('numMCQ', examen.numMCQ);
    formData.append('numTrueFalse', examen.numTrueFalse);
    formData.append('numShortAnswer', examen.numShortAnswer);
    formData.append('difficulty', examen.difficulty);

    // Send POST request as multipart/form-data
    return this.http.post(`${this.baseUrl}/generateExamen`, formData);
  }

  modifyExam(payload: { currentExam: string; instruction: string; topic?: string }): Observable<any> {
    return this.http.post(`${this.baseUrl}/modifyExam`, payload);
  }

  generateImage(prompt: string): Observable<any> {
  const proxyUrl = 'https://cors-anywhere.herokuapp.com/';
  const apiUrl = 'https://api.openai.com/v1/images/generations';

  const headers = new HttpHeaders({
    'Authorization': `Bearer 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`,
    'Content-Type': 'application/json'
  });

  const body = {
    model: 'gpt-image-1',
    prompt: prompt,
    size: '512x512'
  };

  return this.http.post(proxyUrl + apiUrl, body, { headers });
}

}
