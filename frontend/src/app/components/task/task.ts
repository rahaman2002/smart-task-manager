import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { debounceTime } from 'rxjs';

interface Task {
  id: number;
  title: string;
  description: string;
  status: 'OPEN' | 'DONE';
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  dueDate: string;
}

interface UserSession {
  username: string;
  lastLogin: string;
}

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './task.html',
  styleUrls: ['./task.css']
})
export class TaskComponent implements OnInit {

  tasks: Task[] = [];
  taskForm: FormGroup;
  isEditing = false;
  editTaskId: number | null = null;
  filterStatus: string = 'ALL';
  searchTerm: string = '';
  page: number = 0;
  size: number = 5;
  totalPages: number = 1;
  session: UserSession | null = null;

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    this.taskForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      priority: ['MEDIUM'],
      dueDate: ['']
    });
  }

  ngOnInit() {
    this.fetchSession();
    this.fetchTasks();
  }

  get headers() {
    const token = localStorage.getItem('token');
    return { headers: new HttpHeaders({ Authorization: token || '' }) };
  }

  fetchSession() {
    this.http.get<UserSession>('http://localhost:8080/api/tasks/session', this.headers)
      .subscribe(res => this.session = res);
  }

  fetchTasks() {
    const params = { page: this.page.toString(), size: this.size.toString(), search: this.searchTerm };
    this.http.get<any>('http://localhost:8080/api/tasks', { ...this.headers, params })
      .subscribe(res => {
        this.tasks = res.content;
        this.totalPages = res.totalPages;
      });
  }

  addOrUpdateTask() {
    if (this.taskForm.invalid) return;

    const taskData = this.taskForm.value;

    if (this.isEditing && this.editTaskId !== null) {
      this.http.put(`http://localhost:8080/api/tasks/${this.editTaskId}`, taskData, this.headers)
        .subscribe(() => {
          this.isEditing = false;
          this.editTaskId = null;
          this.taskForm.reset({ priority: 'MEDIUM' });
          this.fetchTasks();
        });
    } else {
      this.http.post('http://localhost:8080/api/tasks', taskData, this.headers)
        .subscribe(() => {
          this.taskForm.reset({ priority: 'MEDIUM' });
          this.fetchTasks();
        });
    }
  }

  editTask(task: Task) {
    this.isEditing = true;
    this.editTaskId = task.id;
    this.taskForm.patchValue(task);
  }

  deleteTask(id: number) {
    this.http.delete(`http://localhost:8080/api/tasks/${id}`, this.headers)
      .subscribe(() => this.fetchTasks());
  }

  toggleStatus(task: Task) {
    const updated = { ...task, status: task.status === 'OPEN' ? 'DONE' : 'OPEN' };
    this.http.put(`http://localhost:8080/api/tasks/${task.id}`, updated, this.headers)
      .subscribe(() => this.fetchTasks());
  }

  setFilter(status: string) {
    this.filterStatus = status;
    this.fetchTasks();
  }

  searchTasks(term: string) {
    this.searchTerm = term;
    this.page = 0;
    this.fetchTasks();
  }

  logout() {
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }

  prevPage() {
    if (this.page > 0) { this.page--; this.fetchTasks(); }
  }

  nextPage() {
    if (this.page + 1 < this.totalPages) { this.page++; this.fetchTasks(); }
  }
}
