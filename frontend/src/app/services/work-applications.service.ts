import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
  WorkApplication,
  WorkApplicationDTO,
} from '../interfaces/work-application';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class WorkApplicationsService {
  private apiUrl = 'http://localhost:8080/applications';

  constructor(private http: HttpClient) {}

  saveNewWorkApplication(data: WorkApplicationDTO[], workGroupId: string) {
    return this.http.post(`${this.apiUrl}/save/${workGroupId}`, data);
  }

  getWorkApplications(workGroupId: string): Observable<WorkApplication[]> {
    return this.http.get<WorkApplication[]>(
      `${this.apiUrl}/all/` + workGroupId
    );
  }

  updateWorkApplicationStatus(
    applicationId: string,
    status: string
  ): Observable<WorkApplication> {
    return this.http.patch<WorkApplication>(
      `${this.apiUrl}/update/${applicationId}/${status}`,
      null
    );
  }

  deleteWorkApplication(applicationId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/delete/${applicationId}`);
  }
}
