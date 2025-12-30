# Fabflix

Fabflix is a full-stack Netflix-style web application built with a Java (Maven) backend and a React (Vite + TypeScript) frontend. The project includes two frozen backend implementations for comparison and stability:

MongoDBBackendFreeze – Backend using MongoDB

MySQLBackendFreeze – Backend using MySQL

Both branches expose the same REST APIs and frontend behavior.

## Repository Structure
```
Fabflix/
├── src/                       # Java + Maven backend (Servlet-based)
│   └── main/java/
│       ├── ETLPipeline/       # CSV/XML parsing and database population
│       ├── MongoDBMigration/  # MySQL → MongoDB migration utilities
│       ├── config/            # Database configuration
│       ├── models/            # Data models / documents
│       ├── servlets/          # Application servlets
│       └── utils/             # Shared utilities
├── frontend/                  # React + TypeScript frontend (Vite)
├── data/                      # SQL schema files
├── pom.xml                    # Maven configuration
└── createtable.sql
```
## Branches

MongoDBBackendFreeze – MongoDB-backed implementation

MySQLBackendFreeze – MySQL-backed implementation

Checkout the branch corresponding to the database backend you want to run.

## Backend Setup

Build the backend from the repository root:
```
mvn clean install
mvn package
```

Deploy the generated WAR to a servlet container (e.g., Tomcat).

The backend serves REST endpoints consumed by the frontend.

Frontend Setup (React)

Navigate to the frontend directory:
```
cd frontend
```

Install dependencies:
```
npm install
```

Start the development server:
```
npm run dev
```

The frontend will run locally and communicate with the backend via HTTP APIs.

## Data Processing & Migration

### ETLPipeline
Implements a SAX-based ETL pipeline for parsing raw CSV/XML movie data, applying data quality filters, and loading records into the database.

### MongoDBMigration
Provides utilities for migrating an existing MySQL-backed Fabflix database into MongoDB, including batch and performance optimizations.

## Data Availability

The repository includes database schemas and table definitions only.
Due to licensing and usage restrictions, the actual movie, user, and transaction data is not included. Users must supply their own datasets or connect Fabflix to an existing compatible database.
