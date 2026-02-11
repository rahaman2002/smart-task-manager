import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
  imports: [ReactiveFormsModule, CommonModule],
})
export class LoginComponent {
  isLoginMode = true;
  loginForm: FormGroup;
  registerForm: FormGroup;

  constructor(private fb: FormBuilder, private authService: AuthService,
              private toastr: ToastrService) {

    // ===== Login Form =====
    this.loginForm = fb.group({
      username: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(4)]]
    });

    // ===== Register Form =====
    this.registerForm = fb.group({
      username: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(4)]]
    });
  }

  toggleMode() {
    this.isLoginMode = !this.isLoginMode;
  }

  submitLogin() {
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value).subscribe({
        next: () => {
          this.toastr.success('Login successful!');
        },
        error: err => {
          // err.status comes from HTTP response
          if (err.status === 404) {
            this.toastr.error('User not found');
          } else if (err.status === 401) {
            this.toastr.error('Invalid username or password');
          } else {
            this.toastr.error(err.error || 'Login failed');
          }
        }
      });
    }
  }

  submitRegister() {
    if (this.registerForm.valid) {
      this.authService.register(this.registerForm.value).subscribe({
        next: () => {
          this.toastr.success('Registration successful! You can now login.');
          this.toggleMode(); // switch to login
        },
        error: err => {
          if (err.status === 409) {
            this.toastr.error('User exists, please login.');
          } else {
            this.toastr.error(err.error || 'Registration failed');
          }
        }
      });
    }
  }
}
