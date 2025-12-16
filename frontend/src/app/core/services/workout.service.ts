import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs';
import {DataPoint, Page, Workout} from '../models';

@Injectable({
  providedIn: 'root'
})
export class WorkoutService {
  constructor(private api: ApiService) {
  }

  list(page = 0, size = 10): Observable<Page<Workout>> {
    return this.api.get<Page<Workout>>(`/workouts?page=${page}&size=${size}`);
  }

  getById(id: string): Observable<Workout> {
    return this.api.get<Workout>(`/workouts/${id}`);
  }

  start(): Observable<Workout> {
    return this.api.post<Workout>('/workouts');
  }

  finish(id: string, data: {
    distanceMeters?: number;
    caloriesBurned?: number
  } = {}): Observable<Workout> {
    return this.api.put<Workout>(`/workouts/${id}/finish`, data);
  }

  getDataPoints(workoutId: string): Observable<DataPoint[]> {
    return this.api.get<DataPoint[]>(`/workouts/${workoutId}/datapoints`);
  }
}
