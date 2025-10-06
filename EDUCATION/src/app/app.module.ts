// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';

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



@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    EventComponent,
    EventDetailComponent,
    AiChatBubbleComponent
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
