import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  imports: [CommonModule],
  templateUrl: './task.html',
  styleUrls: ['./task.css']
})
export class TaskComponent {

  tasks = [
    { title: 'Design UI', status: 'OPEN' },
    { title: 'Integrate JWT', status: 'DONE' }
  ];

  addTask() {
    alert('Add task clicked');
  }
}
