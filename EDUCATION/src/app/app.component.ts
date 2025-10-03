import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  template: `
    <app-navbar></app-navbar>
    <main class="page-container">
      <router-outlet></router-outlet>
    </main>
    <app-footer></app-footer>

    <a class="to-top" href="#" aria-label="Back to top">↑</a>
  `,
  styles: [`
    .page-container{min-height: calc(100dvh - 320px); background:#f6f9ff;}
    .to-top{
      position: fixed; right: 20px; bottom: 20px; width:44px; height:44px;
      display:grid; place-items:center; border-radius:10px; text-decoration:none;
      background:#0a2cff; color:#fff; font-weight:700; box-shadow:0 8px 24px rgba(10,44,255,.25);
    }
  `]
})
export class AppComponent {
  title = 'EDUCATION';
}
