import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from 'src/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit{

  
  loginForm!: FormGroup;
  errorMessage: string = '';
  captchaResolved: boolean = false;
  captchaToken: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onCaptchaResolved(token: string | null) {
    if (token) {
      this.captchaResolved = true;
      this.captchaToken = token;
    } else {
      this.captchaResolved = false;
      this.captchaToken = null;
    }
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    if (!this.captchaResolved || !this.captchaToken) {
      window.alert('Veuillez valider le reCAPTCHA !');
      return;
    }

    const { email, password } = this.loginForm.value;

    this.authService.login(email, password, this.captchaToken).subscribe({
      next: (response) => {
        this.authService.saveToken(response.token);
        window.alert('Connexion réussie !');
        console.log("yes")
      },
      error: (err) => {
        const message = err?.error || 'Email ou mot de passe incorrect';
        console.log(message);
        window.alert(message);
      }
    });
  }

}
