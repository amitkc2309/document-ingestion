import { DocumentDTO, DocumentType, PageResponse } from '../types/document';

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

export async function findByAuthor(author: string, page: number = 0, size: number = 10, token: string): Promise<PageResponse<DocumentDTO>> {
  const queryParams = new URLSearchParams({
    author,
    page: String(page),
    size: String(size)
  });

  const response = await fetch(`${API_URL}/by-author?${queryParams}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error('Failed to fetch documents by author');
  }

  return response.json();
}

export async function findByTitle(title: string, page: number = 0, size: number = 10, token: string): Promise<PageResponse<DocumentDTO>> {
  const queryParams = new URLSearchParams({
    title,
    page: String(page),
    size: String(size)
  });

  const response = await fetch(`${API_URL}/by-title?${queryParams}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error('Failed to fetch documents by title');
  }

  return response.json();
}

export async function findByDocumentType(documentType: DocumentType, page: number = 0, size: number = 10, token: string): Promise<PageResponse<DocumentDTO>> {
  const queryParams = new URLSearchParams({
    documentType,
    page: String(page),
    size: String(size)
  });

  const response = await fetch(`${API_URL}/by-type?${queryParams}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error('Failed to fetch documents by type');
  }

  return response.json();
}