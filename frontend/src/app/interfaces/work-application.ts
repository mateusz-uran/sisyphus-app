export interface WorkApplication {
  id: string;
  workUrl: string;
  appliedDate: string;
  status: string;
  specification: WorkSpecificationDTO;
}

export interface WorkApplicationDTO {
  workUrl: string;
}

export interface WorkSpecificationDTO {
  companyName: string;
  requirements: string[];
  technologies: string[];
}
