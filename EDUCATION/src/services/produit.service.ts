import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Produit } from 'src/models/produit';

@Injectable({
  providedIn: 'root'
})
export class ProduitService {

    private products: Produit[] = [
    { name: 'Abonnement Mensuel', description: 'Accès à tous les cours pour 1 mois', price: 9.99, priceId: 'price_1SG4AnLDDekZ5pn61IiUuiKz' },
    { name: 'Abonnement Trimestriel', description: 'Accès à tous les cours pour 3 mois', price: 24.99, priceId: 'price_1SG4BULDDekZ5pn6nHPur1Il' },
    { name: 'Abonnement Annuel', description: 'Accès à tous les cours pour 12 mois', price: 89.99, priceId: 'price_1SG4CqLDDekZ5pn6QHcZtXAZ' }

  ];


   getProducts(): Observable<Produit[]> {
    return of(this.products);
  }



 
}
