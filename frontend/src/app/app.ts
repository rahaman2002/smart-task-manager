import { Component } from '@angular/core';
import { LoginComponent } from './components/login/login';

@Component({
  selector: 'app-root',          // This will be your root element in index.html
  standalone: true,              // Standalone component (Angular 21+)
  template: `<app-login></app-login>`, // Render the login inside
  imports: [LoginComponent],     // Import the login component
})
export class AppComponent {}
