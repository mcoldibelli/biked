import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {User, Workout} from "../../core/models";
import {AuthService} from '../../core/services/auth.service';
import {WorkoutService} from '../../core/services/workout.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  user: User | null = null;
  workouts: Workout[] = [];
  loading = true;

  constructor(
    private authService: AuthService,
    private workoutService: WorkoutService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.user = user;
    });
    this.loadWorkouts();
  }

  loadWorkouts(): void {
    this.workoutService.list().subscribe({
      next: (page) => {
        this.workouts = page.content;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  startWorkout(): void {
    this.workoutService.start().subscribe({
      next: (workout) => {
        this.router.navigate(['/workout', workout.id]);
      }
    })
  }

  viewWorkout(workout: Workout): void {
    this.router.navigate(['/workout', workout.id]);
  }

  logout(): void {
    this.authService.logout();
  }

  formatDuration(seconds: number | undefined): string {
    if (!seconds) return '--:--';
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
