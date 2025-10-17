// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';

// Standalone à importer (PAS dans declarations)
import { NavbarComponent } from './core/navbar/navbar.component';
import { FooterComponent } from './core/footer/footer.component';
import { HomeComponent } from './core/home/home.component';
import { PopularCoursesComponent } from './core/popular-courses/popular-courses.component';
import { CourseComponent } from './core/course/course.component';
import { LessonComponent } from './core/lesson/lesson.component';
import { StudentCoursesComponent } from './core/student/student-courses/student-courses.component';
import { StudentLessonsComponent } from './core/student/student-lessons/student-lessons.component';

// Non-standalone (à déclarer)
import { LoginComponent } from './core/login/login.component';
import { RegisterComponent } from './core/register/register.component';
import { EventComponent } from './core/event/event.component';
import { EventDetailComponent } from './core/event-detail/event-detail.component';
import { AiChatBubbleComponent } from './core/ai-chat-bubble/ai-chat-bubble.component';
import { ExamenComponent } from './examen/examen.component';

import { AdminDashboardComponent } from './core/admin-dashboard/admin-dashboard.component';
import { SubscriptionComponent } from './core/subscription/subscription.component';
import { SuccessComponent } from './core/pages/success/success.component';
import { NgChartsModule } from 'ng2-charts';
import { RevenusComponent } from './core/revenus/revenus.component';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { RecaptchaModule } from 'ng-recaptcha';
import { AuthInterceptor } from './interceptors/auth.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    // ⬇️ Garder uniquement les composants NON-standalone ici
    LoginComponent,
    RegisterComponent,
    EventComponent,
    EventDetailComponent,
    AiChatBubbleComponent,
    AdminDashboardComponent,
    SubscriptionComponent,
    SuccessComponent,
    RevenusComponent,
    ExamenComponent
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
        NgChartsModule,
      AiChatBubbleComponent,
      BrowserModule,
    AppRoutingModule,

    // ⬇️ Composants standalone à importer ici (ou dans leurs parents)
    NavbarComponent,
    FooterComponent,
    HomeComponent,
    PopularCoursesComponent,
    CourseComponent,
    LessonComponent,
    StudentCoursesComponent,
    StudentLessonsComponent,

    ReactiveFormsModule,
    HttpClientModule,
    RecaptchaModule,
    FormsModule
    // ⛔️ NE PAS mettre QuizGeneratorComponent (standalone)
  ],
  
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
