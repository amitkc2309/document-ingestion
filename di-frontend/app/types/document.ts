export enum DocumentType {
  PDF = 'PDF',
  WORD = 'WORD',
  TEXT = 'TEXT',
  OTHER = 'OTHER'
}

export interface DocumentDTO {
  id: number;
  title: string;
  fileName: string;
  contentType: string;
  fileSize: number;
  author: string;
  textContent: string;
  uploadDate: string;
  lastModifiedDate: string;
  uploadedBy: string;
  documentType: DocumentType;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface DocumentSearchParams {
  title?: string;
  author?: string;
  documentType?: DocumentType;
  content?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
} 