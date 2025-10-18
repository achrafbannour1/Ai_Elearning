import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './core/home/home.component';
import { LoginComponent } from './core/login/login.component';
import { RegisterComponent } from './core/register/register.component';
import { NgModule } from '@angular/core';
import {EventComponent} from "./core/event/event.component";
import {EventDetailComponent} from "./core/event-detail/event-detail.component";
import { ExamenComponent } from './examen/examen.component';

import {AdminDashboardComponent} from "./core/admin-dashboard/admin-dashboard.component";

import { SubscriptionComponent } from './core/subscription/subscription.component';
import { SuccessComponent } from './core/pages/success/success.component';
import { RevenusComponent } from './core/revenus/revenus.component';
import { VoiceCoachComponent } from './core/voice-coach/voice-coach.component';


export const routes: Routes = [
  { path: '', component: HomeComponent, title: 'E-learning | Home' },
  { path: '**', redirectTo: 'E-learning | Home' ,pathMatch :'full' },
   { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  // place-holders pour le menu (crée-les plus tard si besoin)
  { path: 'courses', component: HomeComponent, title: 'Courses' },
  { path: 'mentor', component: HomeComponent, title: 'Mentor' },
  { path: 'group', component: HomeComponent, title: 'Group' },
  { path: 'testimonial', component: HomeComponent, title: 'Testimonial' },
  { path: 'examen', component: ExamenComponent, title: 'examen' },
  { path: 'voice', component: VoiceCoachComponent, title: 'examen' },
  { path: 'events', component: EventComponent, title: 'E-learning | Events' },
  {path: 'event/:id', component: EventDetailComponent},

  { path: 'admin', component: AdminDashboardComponent, title: 'Admin Dashboard' },

  {path:'subscription',component: SubscriptionComponent},
  { path: 'success', component: SuccessComponent },
    {path: 'revenus' , component: RevenusComponent}






];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
