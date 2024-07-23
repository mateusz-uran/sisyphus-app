export interface WorkGroup {
  id: string;
  cvData: string;
  cvFileName: string;
  creationTime: string;
  applied: number;
  rejected: number;
  inProgress: number;
  isHired: boolean;
}

export interface TransformedWorkGroup {
  id: string;
  cvData: Blob;
  cvFileName: string;
  creationTime: string;
  applied: number;
  rejected: number;
  inProgress: number;
  isHired: boolean;
}
