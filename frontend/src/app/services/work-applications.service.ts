import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
  WorkApplication,
  WorkApplicationDTO,
  WorkSpecificationDTO,
} from '../interfaces/work-application';
<<<<<<< HEAD
import { Observable, tap } from 'rxjs';
=======
import { Observable } from 'rxjs';
>>>>>>> dev
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class WorkApplicationsService {
  private apiUrl = environment.apiUrl + '/applications';

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

  scrapWorkApplicationSpecification(
    url: string,
    applicationId: string
  ): Observable<WorkSpecificationDTO> {
    return this.http.post<WorkSpecificationDTO>(
      `${this.apiUrl}/spec/${applicationId}`,
      { url }
    );
  }
}
