import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {WorkoutService} from '../../core/services/workout.service';
import {DataPoint, Workout} from '../../core/models';

@Component({
  selector: 'app-workout',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './workout.component.html',
  styleUrl: './workout.component.scss'
})
export class WorkoutComponent implements OnInit {
  workout: Workout | null = null;
  dataPoints: DataPoint[] = [];
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private workoutService: WorkoutService
  ) {
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadWorkout(id);
    }
  }

  loadWorkout(id: string): void {
    this.workoutService.getById(id).subscribe({
      next: (workout) => {
        this.workout = workout;
        this.loadDataPoints(id);
      },
      error: () => {
        this.router.navigate(['/dashboard']);
      }
    });
  }

  loadDataPoints(workoutId: string): void {
    this.workoutService.getDataPoints(workoutId).subscribe({
      next: (dataPoints) => {
        this.dataPoints = dataPoints;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  finishWorkout(): void {
    if (this.workout) {
      this.workoutService.finish(this.workout.id).subscribe({
        next: (workout) => {
          this.workout = workout;
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  formatDuration(seconds: number | undefined): string {
    if (!seconds) return '--:--';
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  formatTime(date: string): string {
    return new Date(date).toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }
}
