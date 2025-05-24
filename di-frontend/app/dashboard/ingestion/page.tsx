'use client';

import { useState, useEffect } from 'react';

interface IngestionStatus {
  id: string;
  documentName: string;
  status: 'queued' | 'processing' | 'completed' | 'failed';
  progress: number;
  error?: string;
  startedAt: string;
  completedAt?: string;
}

export default function IngestionStatusPage() {
  const [statuses, setStatuses] = useState<IngestionStatus[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchStatuses = async () => {
    try {
      const response = await fetch('/api/ingestion/status');
      if (!response.ok) {
        throw new Error('Failed to fetch ingestion statuses');
      }
      const data = await response.json();
      setStatuses(data);
      setError(null);
    } catch (err) {
      setError('Failed to load ingestion statuses');
      console.error('Fetch error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchStatuses();
    const interval = setInterval(fetchStatuses, 5000); // Poll every 5 seconds
    return () => clearInterval(interval);
  }, []);

  const getStatusColor = (status: IngestionStatus['status']) => {
    switch (status) {
      case 'completed':
        return 'bg-green-100 text-green-800';
      case 'processing':
        return 'bg-yellow-100 text-yellow-800';
      case 'queued':
        return 'bg-blue-100 text-blue-800';
      case 'failed':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Ingestion Status</h1>
        <p className="mt-2 text-sm text-gray-700">
          Monitor the processing status of your uploaded documents.
        </p>
      </div>

      {error && (
        <div className="rounded-md bg-red-50 p-4">
          <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      <div className="overflow-hidden rounded-lg border border-gray-200 bg-white shadow">
        <div className="px-4 py-5 sm:p-6">
          {isLoading ? (
            <div className="flex justify-center">
              <div className="h-8 w-8 animate-spin rounded-full border-2 border-blue-500 border-t-transparent" />
            </div>
          ) : statuses.length === 0 ? (
            <p className="text-center text-sm text-gray-500">
              No documents are currently being processed.
            </p>
          ) : (
            <div className="space-y-4">
              {statuses.map((status) => (
                <div
                  key={status.id}
                  className="rounded-lg border border-gray-200 p-4"
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-sm font-medium text-gray-900">
                        {status.documentName}
                      </h3>
                      <div className="mt-1 flex items-center space-x-2">
                        <span
                          className={`inline-flex rounded-full px-2 text-xs font-semibold leading-5 ${getStatusColor(
                            status.status
                          )}`}
                        >
                          {status.status}
                        </span>
                        {status.startedAt && (
                          <span className="text-xs text-gray-500">
                            Started: {new Date(status.startedAt).toLocaleString()}
                          </span>
                        )}
                        {status.completedAt && (
                          <span className="text-xs text-gray-500">
                            Completed:{' '}
                            {new Date(status.completedAt).toLocaleString()}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  {status.status === 'processing' && (
                    <div className="mt-3">
                      <div className="relative h-2 rounded-full bg-gray-200">
                        <div
                          className="absolute h-2 rounded-full bg-blue-500"
                          style={{ width: `${status.progress}%` }}
                        />
                      </div>
                      <p className="mt-1 text-right text-xs text-gray-500">
                        {status.progress}%
                      </p>
                    </div>
                  )}

                  {status.error && (
                    <p className="mt-2 text-sm text-red-600">{status.error}</p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
} 