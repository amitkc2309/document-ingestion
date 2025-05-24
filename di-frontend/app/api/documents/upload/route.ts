import { NextResponse } from 'next/server';

export async function POST(request: Request) {
  try {
    const formData = await request.formData();
    const files = formData.getAll('documents') as File[];

    // TODO: Implement actual file upload to your backend
    // This is a mock implementation
    const uploadedDocuments = files.map((file, index) => ({
      id: `doc-${Date.now()}-${index}`,
      name: file.name,
      uploadedAt: new Date().toISOString(),
      status: 'processing' as const,
    }));

    return NextResponse.json(uploadedDocuments);
  } catch (error) {
    console.error('Upload error:', error);
    return NextResponse.json(
      { error: 'Failed to upload documents' },
      { status: 500 }
    );
  }
} 