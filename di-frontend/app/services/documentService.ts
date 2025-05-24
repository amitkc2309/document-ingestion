import { DocumentDTO, DocumentType, PageResponse, DocumentSearchParams } from '../types/document';

const API_URL = 'http://localhost:8080/api/documents';

export async function uploadDocument(file: File, title: string, author: string, token: string): Promise<DocumentDTO> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('title', title);
  formData.append('author', author);

  const response = await fetch(`${API_URL}/upload`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
    body: formData,
  });

  if (!response.ok) {
    throw new Error('Failed to upload document');
  }

  return response.json();
}

export async function getDocumentById(id: number, token: string): Promise<DocumentDTO> {
  const response = await fetch(`${API_URL}/${id}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error('Failed to fetch document');
  }

  return response.json();
}

export async function deleteDocument(id: number, token: string): Promise<void> {
  const response = await fetch(`${API_URL}/${id}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error('Failed to delete document');
  }
}

export async function searchDocuments(params: DocumentSearchParams, token: string): Promise<PageResponse<DocumentDTO>> {
  const queryParams = new URLSearchParams();
  
  if (params.title) queryParams.append('title', params.title);
  if (params.author) queryParams.append('author', params.author);
  if (params.documentType) queryParams.append('documentType', params.documentType);
  if (params.content) queryParams.append('content', params.content);
  if (params.startDate) queryParams.append('startDate', params.startDate);
  if (params.endDate) queryParams.append('endDate', params.endDate);
  queryParams.append('page', String(params.page || 0));
  queryParams.append('size', String(params.size || 10));

  const response = await fetch(`${API_URL}/search?${queryParams}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error('Failed to search documents');
  }

  return response.json();
}