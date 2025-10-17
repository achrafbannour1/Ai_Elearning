// src/app/app-routing.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { HomeComponent } from './core/home/home.component';
import { LoginComponent } from './core/login/login.component';
import { RegisterComponent } from './core/register/register.component';
import {EventComponent} from "./core/event/event.component";
import {EventDetailComponent} from "./core/event-detail/event-detail.component";
import { ExamenComponent } from './examen/examen.component';

import {AdminDashboardComponent} from "./core/admin-dashboard/admin-dashboard.component";

import { SubscriptionComponent } from './core/subscription/subscription.component';
import { SuccessComponent } from './core/pages/success/success.component';
import { RevenusComponent } from './core/revenus/revenus.component';

// 👉 Standalone components (depuis l’étape précédente)
import { CourseComponent } from 'src/app/core/course/course.component';
import { LessonComponent } from 'src/app/core/lesson/lesson.component';

import { StudentCoursesComponent } from 'src/app/core/student/student-courses/student-courses.component';
import { StudentLessonsComponent } from 'src/app/core/student/student-lessons/student-lessons.component';

export const routes: Routes = [
  { path: '', component: HomeComponent, title: 'E-learning | Home' },

  { path: 'login', component: LoginComponent, title: 'E-learning | Login' },
  { path: 'register', component: RegisterComponent, title: 'E-learning | Register' },
  
  // ✅ Student lecture seule
  { path: 'student/courses', component: StudentCoursesComponent, title: 'E-learning | Student Courses' },
  { path: 'student/courses/:id/lessons', component: StudentLessonsComponent, title: 'E-learning | Student Lessons' },

  

  // ✅ Courses & Lessons
  { path: 'courses', component: CourseComponent, title: 'E-learning | Courses' },
  { path: 'courses/:id/lessons', component: LessonComponent, title: 'E-learning | Lessons' },


  // Placeholders de menu (tu pourras créer de vrais composants plus tard)
  { path: 'mentor', component: HomeComponent, title: 'Mentor' },
  { path: 'group', component: HomeComponent, title: 'Group' },
  { path: 'testimonial', component: HomeComponent, title: 'Testimonial' },
  { path: 'examen', component: ExamenComponent, title: 'examen' },
  { path: 'events', component: EventComponent, title: 'E-learning | Events' },
  {path: 'event/:id', component: EventDetailComponent},

  { path: 'admin', component: AdminDashboardComponent, title: 'Admin Dashboard' },

  {path:'subscription',component: SubscriptionComponent},
  { path: 'success', component: SuccessComponent },
    {path: 'revenus' , component: RevenusComponent},






  // 🔧 Wildcard: toujours en dernier + redirige vers une vraie route
  { path: '**', redirectTo: '', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
