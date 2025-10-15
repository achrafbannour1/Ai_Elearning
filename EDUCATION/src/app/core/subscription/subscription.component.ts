import { Component, OnInit } from '@angular/core';
import { Produit } from 'src/models/produit';
import { AuthService } from 'src/services/auth.service';
import { ProduitService } from 'src/services/produit.service';
import { StripeService } from 'src/services/stripe.service';

@Component({
  selector: 'app-subscription',
  templateUrl: './subscription.component.html',
  styleUrls: ['./subscription.component.css']
})
export class SubscriptionComponent implements OnInit{

    products: Produit[] = [];
    userEmail: string | null = null;

    constructor(
    private produitService: ProduitService,
    private stripeService: StripeService,
    private authService: AuthService
  ) { }

 

  ngOnInit(): void {
      const userInfo = this.authService.getUserInfo();
  if (!userInfo) {
    alert('Vous devez être connecté pour vous abonner');
    return;
  }

  this.userEmail = userInfo.sub; // ici tu récupères l'email
  console.log('User info:', userInfo);



    this.produitService.getProducts().subscribe(p => this.products = p);
  }

subscribe(priceId: string) {
  if (!this.userEmail) {
    alert('Vous devez être connecté pour vous abonner');
    return;
  }

  this.stripeService.createCheckoutSession(this.userEmail, priceId)
    .subscribe({
      next: (url: string) => {
        window.location.href = url; // Redirection vers Stripe Checkout
      },
      error: (err) => {
        console.error(err);
        alert('Erreur lors de la création de la session de paiement');
      }
    });
}
}
