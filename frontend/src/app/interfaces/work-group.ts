export interface WorkGroup {
  id: string;
  cvData: string;
  cvFileName: string;
  creationTime: string;
  sent: number;
  rejected: number;
  inProgress: number;
  isHired: boolean;
}

export interface TransformedWorkGroup {
  id: string;
  cvData: Blob;
  cvFileName: string;
  creationTime: string;
  sent: number;
  rejected: number;
  inProgress: number;
  isHired: boolean;
}
