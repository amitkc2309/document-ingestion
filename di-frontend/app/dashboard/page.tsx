'use client';

import { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { useAuth } from '../contexts/AuthContext';
import { DocumentDTO, DocumentType } from '../types/document';
import * as documentService from '../services/documentService';
import toast from 'react-hot-toast';

export default function DocumentsPage() {
  const { token } = useAuth();
  const [documents, setDocuments] = useState<DocumentDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [uploadForm, setUploadForm] = useState({
    title: '',
    author: '',
    file: null as File | null
  });
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [searchType, setSearchType] = useState<'title' | 'author' | 'type'>('title');
  const [searchValue, setSearchValue] = useState('');
  const [selectedDocType, setSelectedDocType] = useState<DocumentType>(DocumentType.PDF);

  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    const file = acceptedFiles[0];
    if (!file) return;
    setUploadForm(prev => ({ ...prev, file }));
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: false
  });

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !uploadForm.file) return;

    if (!uploadForm.title.trim() || !uploadForm.author.trim()) {
      toast.error('Title and author are required');
      return;
    }

    try {
      setLoading(true);
      await documentService.uploadDocument(
        uploadForm.file,
        uploadForm.title.trim(),
        uploadForm.author.trim(),
        token
      );
      toast.success('Document uploaded successfully');
      // Reset form
      setUploadForm({
        title: '',
        author: '',
        file: null
      });
      // Refresh the document list
      handleSearch();
    } catch (error) {
      toast.error('Failed to upload document');
      console.error('Upload error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!token || (!searchValue && searchType !== 'type')) {
      if (searchType !== 'type') {
        toast.error('Please enter a search value');
      }
      return;
    }

    try {
      setLoading(true);
      let response;

      switch (searchType) {
        case 'title':
          response = await documentService.findByTitle(searchValue, currentPage, pageSize, token);
          break;
        case 'author':
          response = await documentService.findByAuthor(searchValue, currentPage, pageSize, token);
          break;
        case 'type':
          response = await documentService.findByDocumentType(selectedDocType, currentPage, pageSize, token);
          break;
      }

      setDocuments(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      toast.error('Failed to search documents');
      console.error('Search error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!token) return;

    try {
      await documentService.deleteDocument(id, token);
      toast.success('Document deleted successfully');
      // Refresh the document list
      handleSearch();
    } catch (error) {
      toast.error('Failed to delete document');
      console.error('Delete error:', error);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-gray-100 p-6 rounded-lg shadow">
        <h2 className="text-2xl font-bold mb-4 text-gray-900">Upload Document</h2>
        <form onSubmit={handleUpload} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <input
              type="text"
              placeholder="Document Title *"
              className="border rounded p-2 text-gray-900 bg-white w-full"
              value={uploadForm.title}
              onChange={(e) => setUploadForm(prev => ({ ...prev, title: e.target.value }))}
              required
            />
            <input
              type="text"
              placeholder="Author Name *"
              className="border rounded p-2 text-gray-900 bg-white w-full"
              value={uploadForm.author}
              onChange={(e) => setUploadForm(prev => ({ ...prev, author: e.target.value }))}
              required
            />
          </div>
          <div
            {...getRootProps()}
            className={`border-2 border-dashed p-8 text-center rounded-lg cursor-pointer ${
              isDragActive ? 'border-blue-500 bg-blue-50' : 'border-gray-300'
            }`}
          >
            <input {...getInputProps()} />
            {uploadForm.file ? (
              <p className="text-gray-700">Selected file: {uploadForm.file.name}</p>
            ) : isDragActive ? (
              <p className="text-gray-700">Drop the file here...</p>
            ) : (
              <p className="text-gray-700">Drag and drop a file here, or click to select a file</p>
            )}
          </div>
          <div className="flex justify-end">
            <button
              type="submit"
              className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50"
              disabled={loading || !uploadForm.file || !uploadForm.title.trim() || !uploadForm.author.trim()}
            >
              {loading ? 'Uploading...' : 'Upload Document'}
            </button>
          </div>
        </form>
      </div>

      <div className="bg-gray-100 p-6 rounded-lg shadow">
        <h2 className="text-2xl font-bold mb-4 text-gray-900">Search Documents</h2>
        <div className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Search Type</label>
              <select
                className="border rounded p-2 text-gray-900 bg-white w-full"
                value={searchType}
                onChange={(e) => {
                  setSearchType(e.target.value as 'title' | 'author' | 'type');
                  setSearchValue('');
                }}
              >
                <option value="title">By Title</option>
                <option value="author">By Author</option>
                <option value="type">By Document Type</option>
              </select>
            </div>

            {searchType === 'type' ? (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Document Type</label>
                <select
                  className="border rounded p-2 text-gray-900 bg-white w-full"
                  value={selectedDocType}
                  onChange={(e) => setSelectedDocType(e.target.value as DocumentType)}
                >
                  {Object.values(DocumentType).map((type) => (
                    <option key={type} value={type}>{type}</option>
                  ))}
                </select>
              </div>
            ) : (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  {searchType === 'title' ? 'Title' : 'Author'}
                </label>
                <input
                  type="text"
                  placeholder={`Enter ${searchType}...`}
                  className="border rounded p-2 text-gray-900 bg-white w-full"
                  value={searchValue}
                  onChange={(e) => setSearchValue(e.target.value)}
                  onKeyPress={handleKeyPress}
                />
              </div>
            )}

            <div className="flex items-end">
              <button
                onClick={handleSearch}
                className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                disabled={loading}
              >
                {loading ? 'Searching...' : 'Search'}
              </button>
            </div>
          </div>
        </div>

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
                  onClick={() => {
                    setCurrentPage(i);
                    handleSearch();
                  }}
                  className={`px-3 py-1 rounded ${
                    currentPage === i
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