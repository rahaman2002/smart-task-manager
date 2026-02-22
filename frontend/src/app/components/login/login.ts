import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
  imports: [ReactiveFormsModule, CommonModule],
  providers: [FormBuilder],
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
      username: ['', [Validators.required, Validators.minLength(4)]],
      password: ['', [Validators.required, Validators.minLength(4)]]
    });

    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(4)]],
      password: ['', [Validators.required, Validators.minLength(4)]]
    });
  }

  toggleMode() {
    this.isLoginMode = !this.isLoginMode;

    // Reset forms & password visibility
    this.loginForm.reset();
    this.registerForm.reset();
    this.showLoginPassword = false;
    this.showRegisterPassword = false;
  }

  submitLogin() {
    if (this.loginForm.invalid) return;

    this.isLoading = true;

    this.authService.login(this.loginForm.value).subscribe({
      next: (res) => {
        this.isLoading = false;

        //  Save token
        this.authService.saveToken(res.token);

        //  Save username
        localStorage.setItem('username', this.loginForm.value.username);

        // Save previous last login (may be null if first login)
        if (res.previousLastLogin) {
          localStorage.setItem('previousLastLogin', res.previousLastLogin);
        } else {
          localStorage.removeItem('previousLastLogin');
        }

        this.toastr.success('Login successful!');
        this.router.navigate(['tasks']);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;

        if (err.status === 404) {
          this.toastr.error('User not found');
        } else if (err.status === 401) {
          this.toastr.error('Invalid username or password');
        } else {
          this.toastr.error('Login failed. Please try again.');
        }
      }
    });
  }

  loginWithGoogle() {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  }

  submitRegister() {
    if (this.registerForm.invalid) return;

    this.isLoading = true;

    this.authService.register(this.registerForm.value).subscribe({
      next: (res) => {
        this.isLoading = false;

        // Auto login: save token
        this.authService.saveToken(res.token);

        // Save username
        localStorage.setItem('username', this.registerForm.value.username);

        // First login, so previousLastLogin is null
        localStorage.removeItem('previousLastLogin');

        this.toastr.success('Registration successful!');
        this.router.navigate(['tasks']);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;

        if (err.status === 409) {
          this.toastr.error('User already exists.');
        } else {
          this.toastr.error('Registration failed.');
        }
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
