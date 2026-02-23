import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-set-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule], // IMPORTANT
  templateUrl: './set-password.component.html',
  styleUrls: ['./set-password.component.css']
})
export class SetPasswordComponent implements OnInit {

  setPasswordForm: FormGroup;
  email: string = '';
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private toastr: ToastrService
  ) {
    this.setPasswordForm = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(4)]]
    });
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.email = params['email'] || '';
    });
  }

  submitSetPassword() {
    if (this.setPasswordForm.invalid) return;

    this.isLoading = true;

    this.authService.setPassword({
      email: this.email,
      password: this.setPasswordForm.value.password
    }).subscribe({
      next: (res) => {
        this.isLoading = false;

        this.authService.saveToken(res.token);
        localStorage.setItem('username', res.username);

        if (res.previousLastLogin) {
          localStorage.setItem('previousLastLogin', res.previousLastLogin);
        }

        this.toastr.success('Welcome! Account setup complete.');

        this.router.navigate(['/tasks']);
      },
      error: () => {
        this.isLoading = false;
        this.toastr.error('Failed to set password');
      }
    });
  }
}