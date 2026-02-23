import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-login-success',
  template: '<p>Logging you in via Google...</p>'
})
export class LoginSuccessComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      const username = params['username'];
      const previousLastLogin = params['previousLastLogin'];

      if (token) {
        this.authService.saveToken(token);
        if (username) localStorage.setItem('username', username);
        if (previousLastLogin) localStorage.setItem('previousLastLogin', previousLastLogin);

        this.toastr.success('Logged in via Google!');
        this.router.navigate(['tasks']);
      } else {
        this.toastr.error('Google login failed.');
        this.router.navigate(['login']);
      }
    });
  }
}