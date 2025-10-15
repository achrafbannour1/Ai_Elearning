import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RevenusService {

    private apiUrl = 'http://localhost:8083/api/revenus';

    constructor(private http: HttpClient) { }


     getRevenus(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

 
}
