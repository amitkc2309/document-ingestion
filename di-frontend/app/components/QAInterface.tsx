'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useAuth } from '../contexts/AuthContext';
import Button from './ui/Button';
import Input from './ui/Input';
import config from '../config/config';

interface QuestionRequest {
  question: string;
  maxResults: number;
  snippetLength: number;
}

interface Snippet {
  documentId: number;
  documentTitle: string;
  author: string;
  snippet: string;
  relevanceScore: number;
}

interface QAResponse {
  question: string;
  snippets: Snippet[];
  totalResults: number;
}

interface QAForm {
  question: string;
}

const QA_API_URL = `${config.apiUrl}/api/qa`;

export default function QAInterface() {
  const { token } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [response, setResponse] = useState<QAResponse | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<QAForm>();

  const onSubmit = async (data: QAForm) => {
    try {
      setIsLoading(true);
      setError(null);
      
      // Query parameters for pagination
      const queryParams = new URLSearchParams({
        page: '0',
        size: '10'
      });

      // Request body with question and snippet settings
      const requestBody: QuestionRequest = {
        question: data.question,
        maxResults: 5,
        snippetLength: 200
      };

      const res = await fetch(`${QA_API_URL}/ask?${queryParams}`, {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(requestBody)
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => null);
        throw new Error(errorData?.message || 'Failed to get answer');
      }

      const qaResponse = await res.json();
      setResponse(qaResponse);
    } catch (err) {
      console.error('QA error:', err);
      setError('Failed to get answer. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="w-full max-w-4xl space-y-6">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input
          label="Ask a question"
          placeholder="Type your question here..."
          {...register('question', {
            required: 'Please enter a question',
            minLength: {
              value: 3,
              message: 'Question must be at least 3 characters long',
            },
          })}
          error={errors.question?.message}
        />
        <div className="flex justify-end">
          <Button type="submit" isLoading={isLoading}>
            Ask Question
          </Button>
        </div>
      </form>

      {error && (
        <div className="rounded-md bg-red-50 p-4">
          <p className="text-sm text-red-700">{error}</p>
        </div>
      )}

      {response && (
        <div className="space-y-6">
          <div className="rounded-lg bg-white p-6 shadow">
            <h3 className="text-lg font-medium text-gray-900">Question</h3>
            <p className="mt-2 text-gray-700">{response.question}</p>
            <p className="mt-2 text-sm text-gray-500">
              Found {response.totalResults} relevant results
            </p>
          </div>

          {response.snippets.length > 0 && (
            <div className="rounded-lg bg-gray-50 p-6">
              <h3 className="text-lg font-medium text-gray-900">
                Relevant Snippets
              </h3>
              <div className="mt-4 space-y-4">
                {response.snippets.map((snippet, index) => (
                  <div
                    key={`${snippet.documentId}-${index}`}
                    className="rounded-md bg-white p-4 shadow-sm"
                  >
                    <p className="text-sm text-gray-700">{snippet.snippet}</p>
                    <div className="mt-2 flex items-center justify-between">
                      <div className="space-y-1">
                        <p className="text-xs text-gray-500">
                          Document: {snippet.documentTitle}
                        </p>
                        {snippet.author && (
                          <p className="text-xs text-gray-500">
                            Author: {snippet.author}
                          </p>
                        )}
                      </div>
                      <span className="text-xs text-gray-500">
                        Relevance: {Math.round(snippet.relevanceScore * 100)}%
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
} 