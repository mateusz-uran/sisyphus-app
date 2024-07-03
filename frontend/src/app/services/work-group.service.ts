import {
  HttpClient,
  HttpEvent,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, EMPTY, Observable } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { TransformedWorkGroup, WorkGroup } from '../interfaces/work-group';

@Injectable({
  providedIn: 'root',
})
export class WorkGroupService {
  private apiUrl = 'http://localhost:8080/group';

  private workGroupsSubject = new BehaviorSubject<TransformedWorkGroup[]>([]);
  workGroups$ = this.workGroupsSubject.asObservable();

  constructor(private http: HttpClient) {}

  loadInitialWorkGroups(): void {
    this.getWorkGroups().subscribe();
  }

  getWorkGroups(): Observable<TransformedWorkGroup[]> {
    return this.http.get<WorkGroup[]>(`${this.apiUrl}/all`).pipe(
      map((workGroups) =>
        workGroups.map((workGroup) => ({
          ...workGroup,
          cvData: this.decodeToPdf(workGroup.cvData),
        }))
      ),
      tap((groupList: TransformedWorkGroup[]) => {
        this.workGroupsSubject.next(groupList);
      }),
      catchError((err) => {
        console.log(err);
        return EMPTY;
      })
    );
  }

  addWorkGroup(cv: File): Observable<HttpEvent<any>> {
    const formData: FormData = new FormData();
    formData.append('cv', cv);

    const request = new HttpRequest('POST', `${this.apiUrl}/create`, formData, {
      responseType: 'json',
    });

    return this.http.request(request).pipe(
      tap((event) => {
        if (event instanceof HttpResponse) {
          this.getWorkGroups().subscribe();
        }
      })
    );
  }

  deleteWorkGroup(workGroupId: string): Observable<any> {
    return this.http
      .delete(`${this.apiUrl}/delete/${workGroupId}`)
      .pipe(tap(() => this.getWorkGroups().subscribe()));
  }

  private decodeToPdf(cv: string): Blob {
    const byteArray = new Uint8Array(
      atob(cv)
        .split('')
        .map((char) => char.charCodeAt(0))
    );
    return new Blob([byteArray], { type: 'application/pdf' });
  }
}
