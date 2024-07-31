import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LoadingApplicationService {
  private loadingMap = new BehaviorSubject<Map<string, boolean>>(new Map());

  loading$ = this.loadingMap.asObservable();

  setLoading(isLoading: boolean, appId: string): void {
    const currentMap = this.loadingMap.getValue();
    currentMap.set(appId, isLoading);
    this.loadingMap.next(new Map(currentMap));
  }

  isLoading(appId: string): boolean {
    return this.loadingMap.getValue().get(appId) || false;
  }
}
