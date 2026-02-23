import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { TaskComponent } from './components/task/task';
import { AuthGuard } from './guards/auth.guard';
import { LoginSuccessComponent } from './components/login-success/login-success';
import { SetPasswordComponent } from './components/set-password/set-password.component';

export const routes: Routes = [
  { path: 'login-success', component: LoginSuccessComponent},
  { path: 'set-password', component: SetPasswordComponent },
  { path: '', component: LoginComponent },
  { path: 'login', component: LoginComponent },
  { path: 'tasks', component: TaskComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: 'register' },

];
