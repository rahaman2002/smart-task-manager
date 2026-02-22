import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private API = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  register(data: { username: string; password: string }) {
  return this.http.post<any>(`${this.API}/register`, data);
  }

  login(data: { username: string; password: string }) {
    return this.http.post<any>(`${this.API}/login`, data);
  }

  saveToken(token: string) {
    localStorage.setItem('token', token);
  }

  logout() {
    localStorage.removeItem('token');
  }

  getToken() {
    return localStorage.getItem('token');
  }

  
}
