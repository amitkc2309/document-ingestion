# Document Ingestion System

A comprehensive document management system that allows users to upload, process, search, and query documents of various formats (PDF, DOCX, XLSX, TXT).

## Features

- **Document Management**: Upload, retrieve, and delete documents
- **Advanced Search**: Search documents by title, author, type, date range, and content
- **Question & Answer**: Ask questions about document content and get relevant snippets
- **Authentication & Authorization**: Secure API with JWT-based authentication and role-based access control
- **Asynchronous Processing**: Background processing of documents using Kafka
- **Full-Text Search**: Elasticsearch integration for powerful content search
- **Caching**: Redis caching for improved performance

## System Architecture

The system consists of the following components:

- **Spring Boot Backend**: RESTful API for document management
- **PostgreSQL**: Database for storing document metadata and user information
- **Kafka**: Message broker for asynchronous document processing
- **Elasticsearch**: Search engine for document content indexing and full-text search
- **Redis**: Caching layer for improved performance

## Prerequisites

- Java 17 or higher
- Maven
- Docker and Docker Compose

## Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/document-ingestion.git
cd document-ingestion
```

### 2. Start the infrastructure services

```bash
cd di-docker
docker-compose up -d
```

This will start PostgreSQL, Kafka, Elasticsearch, and Redis services.

### 3. Build and run the backend

```bash
cd ../di-backend
mvn clean install
mvn spring-boot:run
```

The application will be available at http://localhost:8080

## API Documentation

Once the application is running, you can access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login a user
- `POST /api/auth/logout` - Logout a user

### Document Management

- `POST /api/documents/upload` - Upload a document (requires ADMIN or EDITOR role)
- `GET /api/documents/{id}` - Get document by ID
- `DELETE /api/documents/{id}` - Delete document (requires ADMIN or EDITOR role)
- `GET /api/documents/search` - Search documents by multiple criteria
- `GET /api/documents/by-author` - Find documents by author
- `GET /api/documents/by-title` - Find documents by title
- `GET /api/documents/by-type` - Find documents by type
- `GET /api/documents/by-date-range` - Find documents by date range
- `GET /api/documents/by-content` - Find documents by content using full-text search

### Question & Answer

- `GET /api/qa/ask` - Ask a question and get relevant document snippets

## Configuration

The application can be configured through the `application.yaml` file. Key configuration options include:

- Database connection settings
- Kafka configuration
- Elasticsearch settings
- Redis cache configuration
- JWT security settings
- File storage location

