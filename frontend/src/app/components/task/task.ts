import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Subject, combineLatest } from 'rxjs';
import { debounceTime, switchMap, startWith } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

interface Task {
  id: number;
  title: string;
  description: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'CLOSED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  dueDate: string;
}

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './task.html',
  styleUrls: ['./task.css']
})
export class TaskComponent implements OnInit {

  // Reactive state
  public tasksSubject = new BehaviorSubject<Task[]>([]);
  tasks$ = this.tasksSubject.asObservable();

  public filterSubject = new BehaviorSubject<string>('ALL');
  public searchSubject = new Subject<string>();
  public pageSubject = new BehaviorSubject<number>(0);
  public size = 5;
  public totalPages = 1;
  username: string | null = null;
  previousLastLogin: string | null = null;
  taskForm: FormGroup;
  isEditing = false;
  editTaskId: number | null = null;

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router) {
    this.taskForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      priority: ['MEDIUM'],
      dueDate: [new Date().toISOString().slice(0, 10)]
    });
  }

  ngOnInit() {
    this.username = localStorage.getItem('username');
    this.previousLastLogin = localStorage.getItem('previousLastLogin');
    combineLatest([
      this.searchSubject.pipe(startWith(''), debounceTime(300)),
      this.filterSubject,
      this.pageSubject
    ])
    .pipe(
      switchMap(([search, filter, page]) => this.fetchTasks(search, filter, page))
    )
    .subscribe(res => {
      this.tasksSubject.next(res.content);
      this.totalPages = res.totalPages;
    });

    // Initial fetch
    this.searchSubject.next('');
    this.filterSubject.next('OPEN');
    this.pageSubject.next(0);
  }

  get headers() {
    const token = localStorage.getItem('token');
    return { headers: new HttpHeaders({ Authorization: `Bearer ${token}` }) };
  }

  fetchTasks(search: string, filterStatus: string, page: number) {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', this.size.toString())
      .set('search', search || '');

    if (filterStatus !== 'ALL') {
      params = params.set('status', filterStatus);
    }

    return this.http.get<{ content: Task[], totalPages: number }>(
      environment.API_URL + '/api/tasks',
      { ...this.headers, params }
    );
  }

  addOrUpdateTask() {
    if (this.taskForm.invalid) return;

    const taskData: any = { ...this.taskForm.value };
    const d = taskData.dueDate ? new Date(taskData.dueDate) : new Date();
    taskData.dueDate = new Date(d.getFullYear(), d.getMonth(), d.getDate()).toISOString();

    const request$ = this.isEditing && this.editTaskId
      ? this.http.put(`${environment.API_URL}/api/tasks/${this.editTaskId}`, taskData, this.headers)
      : this.http.post(`${environment.API_URL}/api/tasks`, taskData, this.headers);

    request$.subscribe(() => {
      this.isEditing = false;
      this.editTaskId = null;
      this.taskForm.reset({ priority: 'MEDIUM', dueDate: new Date().toISOString().slice(0, 10) });
      this.refreshTasks();
    });
  }

  editTask(task: Task) {
    this.isEditing = true;
    this.editTaskId = task.id;
    this.taskForm.patchValue(task);
  }

  deleteTask(id: number) {
    this.http.delete(`${environment.API_URL}/api/tasks/${id}`, this.headers)
      .subscribe(() => this.refreshTasks());
  }

  toggleStatus(task: Task) {
    let updated: Task;
    if(task.status === 'OPEN') updated = {...task, status: 'IN_PROGRESS'};
    else if(task.status === 'IN_PROGRESS') updated = {...task, status: 'CLOSED'};
    else updated = {...task, status: 'OPEN'};

    this.http.put(`${environment.API_URL}/api/tasks/${task.id}`, updated, this.headers)
      .subscribe(() => this.refreshTasks());
  }

  setFilter(status: string) {
    this.pageSubject.next(0);
    this.filterSubject.next(status);
  }

  searchTasks(term: string) {
    this.pageSubject.next(0);
    this.searchSubject.next(term);
  }

  prevPage() {
    const current = this.pageSubject.value;
    if (current > 0) this.pageSubject.next(current - 1);
  }

  nextPage() {
    const current = this.pageSubject.value;
    if (current + 1 < this.totalPages) this.pageSubject.next(current + 1);
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('previousLastLogin');
    this.router.navigate(['/login']);
  }

  private refreshTasks() {
    this.pageSubject.next(this.pageSubject.value);
  }

  get page() {
    return this.pageSubject.value;
  }
}