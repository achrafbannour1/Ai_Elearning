// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppComponent } from './app.component';
import { AppRoutingModule, routes } from './app-routing.module';
import { NavbarComponent } from './core/navbar/navbar.component';
import { FooterComponent } from './core/footer/footer.component';
import { HomeComponent } from './core/home/home.component';
import { PopularCoursesComponent } from './core/popular-courses/popular-courses.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import { RecaptchaModule } from 'ng-recaptcha';
import { LoginComponent } from './core/login/login.component';
import { RegisterComponent } from './core/register/register.component';
import { EventComponent } from './core/event/event.component';
import { EventDetailComponent } from './core/event-detail/event-detail.component';
import {AuthInterceptor} from "./interceptors/auth.interceptor";
import { AiChatBubbleComponent } from './core/ai-chat-bubble/ai-chat-bubble.component';
<<<<<<< HEAD
import { AdminDashboardComponent } from './core/admin-dashboard/admin-dashboard.component';
=======
import { SubscriptionComponent } from './core/subscription/subscription.component';
import { SuccessComponent } from './core/pages/success/success.component';
import { NgChartsModule } from 'ng2-charts';
import { RevenusComponent } from './core/revenus/revenus.component';
>>>>>>> 789b657 (first commit)



@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    EventComponent,
    EventDetailComponent,
    AiChatBubbleComponent,
<<<<<<< HEAD
    AdminDashboardComponent
=======
    SubscriptionComponent,
    SuccessComponent,
    RevenusComponent
>>>>>>> 789b657 (first commit)
  ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        NavbarComponent,
        FooterComponent,
        HomeComponent,
        PopularCoursesComponent,
        ReactiveFormsModule,
        HttpClientModule,
        RecaptchaModule,
        FormsModule,
        NgChartsModule


    ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
