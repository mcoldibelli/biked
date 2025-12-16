export interface User {
  id: string;
  email: string;
  name: string;
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  name: string;
  password: string;
}

export interface TokenResponse {
  token: string;
}


export interface Workout {
  id: string;
  status: 'IN_PROGRESS' | 'COMPLETED' | 'PAUSED' | 'CANCELLED';
  startedAt: string;
  finishedAt?: string;
  durationSeconds?: number;
  avgCadence?: number;
  maxCadence?: number;
  avgSpeed?: number;
  maxSpeed?: number;
  distanceMeters?: number;
  caloriesBurned?: number;
  createdAt: string;
}

export interface DataPoint {
  id: string;
  cadence: number;
  speed: number;
  heartRate?: number;
  recordedAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
