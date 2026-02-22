import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-login-success',
  template: `<p>Logging you in...</p>`
})
export class LoginSuccessComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (token) {
      localStorage.setItem('token', token);
      this.router.navigate(['/tasks']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}