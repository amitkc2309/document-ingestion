'use client';

import { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { useAuth } from '../contexts/AuthContext';
import { DocumentDTO, DocumentType, PageResponse, DocumentSearchParams } from '../types/document';
import * as documentService from '../services/documentService';
import toast from 'react-hot-toast';

export default function DocumentsPage() {
  const { token } = useAuth();
  const [documents, setDocuments] = useState<DocumentDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState<DocumentSearchParams>({
    page: 0,
    size: 10
  });
  const [searchForm, setSearchForm] = useState({
    title: '',
    author: '',
    documentType: ''
  });
  const [totalPages, setTotalPages] = useState(0);

  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    if (!token) return;

    const file = acceptedFiles[0];
    if (!file) return;

    try {
      setLoading(true);
      const result = await documentService.uploadDocument(
        file,
        file.name,
        'Unknown Author',
        token
      );
      toast.success('Document uploaded successfully');
      // Refresh the document list
      handleSearch({ ...searchParams, page: 0 });
    } catch (error) {
      toast.error('Failed to upload document');
      console.error('Upload error:', error);
    } finally {
      setLoading(false);
    }
  }, [token, searchParams]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: false
  });

  const handleSearch = async (params: DocumentSearchParams) => {
    if (!token) return;

    try {
      setLoading(true);
      const response = await documentService.searchDocuments(params, token);
      setDocuments(response.content);
      setTotalPages(response.totalPages);
      setSearchParams(params);
    } catch (error) {
      toast.error('Failed to search documents');
      console.error('Search error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleSearch({
      ...searchParams,
      page: 0,
      title: searchForm.title,
      author: searchForm.author,
      documentType: searchForm.documentType as DocumentType || undefined
    });
  };

  const handleDelete = async (id: number) => {
    if (!token) return;

    try {
      await documentService.deleteDocument(id, token);
      toast.success('Document deleted successfully');
      // Refresh the document list
      handleSearch(searchParams);
    } catch (error) {
      toast.error('Failed to delete document');
      console.error('Delete error:', error);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-gray-100 p-6 rounded-lg shadow">
        <h2 className="text-2xl font-bold mb-4 text-gray-900">Upload Document</h2>
        <div
          {...getRootProps()}
          className={`border-2 border-dashed p-8 text-center rounded-lg cursor-pointer ${
            isDragActive ? 'border-blue-500 bg-blue-50' : 'border-gray-300'
          }`}
        >
          <input {...getInputProps()} />
          {isDragActive ? (
            <p className="text-gray-700">Drop the file here...</p>
          ) : (
            <p className="text-gray-700">Drag and drop a file here, or click to select a file</p>
          )}
        </div>
      </div>

      <div className="bg-gray-100 p-6 rounded-lg shadow">
        <h2 className="text-2xl font-bold mb-4 text-gray-900">Search Documents</h2>
        <form onSubmit={handleSearchSubmit} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <input
              type="text"
              placeholder="Search by title"
              className="border rounded p-2 text-gray-900 bg-white w-full"
              value={searchForm.title}
              onChange={(e) => setSearchForm(prev => ({ ...prev, title: e.target.value }))}
            />
            <input
              type="text"
              placeholder="Search by author"
              className="border rounded p-2 text-gray-900 bg-white w-full"
              value={searchForm.author}
              onChange={(e) => setSearchForm(prev => ({ ...prev, author: e.target.value }))}
            />
            <select
              className="border rounded p-2 text-gray-900 bg-white w-full"
              value={searchForm.documentType}
              onChange={(e) => setSearchForm(prev => ({ ...prev, documentType: e.target.value }))}
            >
              <option value="">All Types</option>
              {Object.values(DocumentType).map((type) => (
                <option key={type} value={type}>{type}</option>
              ))}
            </select>
          </div>
          <div className="flex justify-end">
            <button
              type="submit"
              className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              disabled={loading}
            >
              {loading ? 'Searching...' : 'Search'}
            </button>
          </div>
        </form>

        <div className="mt-6">
          {loading ? (
            <div className="text-center py-4 text-gray-700">Loading...</div>
          ) : (
            <div className="overflow-x-auto bg-white rounded-lg">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Title</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Author</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Upload Date</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {documents.map((doc) => (
                    <tr key={doc.id}>
                      <td className="px-6 py-4 whitespace-nowrap text-gray-900">{doc.title}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-gray-900">{doc.author}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-gray-900">{doc.documentType}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-gray-900">
                        {new Date(doc.uploadDate).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <button
                          onClick={() => handleDelete(doc.id)}
                          className="text-red-600 hover:text-red-900"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                  {documents.length === 0 && (
                    <tr>
                      <td colSpan={5} className="px-6 py-4 text-center text-gray-500">
                        No documents found
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}

          {totalPages > 1 && (
            <div className="flex justify-center mt-4 space-x-2">
              {Array.from({ length: totalPages }, (_, i) => (
                <button
                  key={i}
                  onClick={() => handleSearch({ ...searchParams, page: i })}
                  className={`px-3 py-1 rounded ${
                    searchParams.page === i
                      ? 'bg-blue-500 text-white'
                      : 'bg-gray-200 text-gray-700'
                  }`}
                >
                  {i + 1}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
} 