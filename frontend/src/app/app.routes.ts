import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { TaskComponent } from './components/task/task';
import { AuthGuard } from './guards/auth.guard';
import { LoginSuccessComponent } from './components/login-success/login-success';

export const routes: Routes = [
  { path: 'login-success', component: LoginSuccessComponent},
  { path: '', component: LoginComponent },
  { path: 'login', component: LoginComponent },
  { path: 'tasks', component: TaskComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: 'register' },

];
