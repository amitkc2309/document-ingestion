'use client';

import QAInterface from '../../components/QAInterface';

export default function QAPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Q&A Interface</h1>
        <p className="mt-2 text-sm text-gray-700">
          Ask questions about your documents and get answers with relevant excerpts.
        </p>
      </div>

      <div className="rounded-lg bg-white p-6 shadow">
        <QAInterface />
      </div>
    </div>
  );
} 