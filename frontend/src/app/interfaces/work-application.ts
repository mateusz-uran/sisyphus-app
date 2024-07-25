export interface WorkApplication {
  id: string;
  workUrl: string;
  appliedDate: string;
  status: string;
}

export interface WorkApplicationDTO {
  workUrl: string;
}

export interface WorkSpecificationDTO {
  companyName: string;
  requirementsExpected: string[];
  technologiesExpected: string[];
}
