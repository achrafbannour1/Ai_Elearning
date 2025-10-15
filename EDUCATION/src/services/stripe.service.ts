import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StripeService {

    private apiUrl = 'http://localhost:8083/api/stripe';

      constructor(private http: HttpClient) { }

   createCheckoutSession(userEmail: string, priceId: string): Observable<string> {
  return this.http.post(
    `${this.apiUrl}/create-checkout-session?email=${userEmail}&priceId=${priceId}`,
    null,
    { responseType: 'text' } // important pour récupérer l'URL
  );
}

}
