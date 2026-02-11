import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { TaskComponent } from './components/task/task';

export const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'login', component: LoginComponent },
  { path: 'tasks', component: TaskComponent },
  { path: '**', redirectTo: 'register' }
];
