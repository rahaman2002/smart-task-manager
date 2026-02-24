import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment.prod';

@Component({
  selector: 'app-login',
  standalone: true,
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
  imports: [ReactiveFormsModule, CommonModule],
})
export class LoginComponent {

  isLoginMode = true;
  isLoading = false;

  loginForm: FormGroup;
  registerForm: FormGroup;

  showLoginPassword = false;
  showRegisterPassword = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private toastr: ToastrService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(4)]]
    });

    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(4)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(4)]]
    });
  }

  toggleMode() {
    this.isLoginMode = !this.isLoginMode;
    this.loginForm.reset();
    this.registerForm.reset();
  }

  submitLogin() {
    if (this.loginForm.invalid) return;

    this.isLoading = true;

    this.authService.login(this.loginForm.value).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.authService.saveToken(res.token);
        localStorage.setItem('username', res.username);
        if (res.previousLastLogin) {
          localStorage.setItem('previousLastLogin', res.previousLastLogin);
        } else {
          localStorage.removeItem('previousLastLogin'); // first login
        }
        this.toastr.success('Login successful!');
        this.router.navigate(['tasks']);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        if (err.status === 404) this.toastr.error('User not found');
        else if (err.status === 401) this.toastr.error('Invalid email or password');
        else this.toastr.error('Login failed');
      }
    });
  }

  // ===== GOOGLE LOGIN =====
  loginWithGoogle() {
    // Redirect to backend OAuth2 endpoint
    window.location.href = environment.API_URL + '/oauth2/authorization/google';
  }

  submitRegister() {
    if (this.registerForm.invalid) return;

    this.isLoading = true;

    this.authService.register(this.registerForm.value).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.authService.saveToken(res.token);
        localStorage.setItem('username', res.username);

        this.toastr.success('Registration successful!');
        this.router.navigate(['tasks']);
      },
      error: () => {
        this.isLoading = false;
        this.toastr.error('Registration failed');
      }
    });
  }

  toggleLoginPassword() {
    this.showLoginPassword = !this.showLoginPassword;
  }

  toggleRegisterPassword() {
    this.showRegisterPassword = !this.showRegisterPassword;
  }
}