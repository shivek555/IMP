USE athenaeum_db;

-- Drop tables if they exist
DROP TABLE IF EXISTS document_tags;
DROP TABLE IF EXISTS note_tags;
DROP TABLE IF EXISTS search_history;
DROP TABLE IF EXISTS user_sessions;
DROP TABLE IF EXISTS notes;
DROP TABLE IF EXISTS documents;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS users;

-- Create USERS table
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    email NVARCHAR(255) NOT NULL UNIQUE,
    password NVARCHAR(255) NOT NULL,
    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100) NOT NULL,
    enabled BIT DEFAULT 1,
    account_locked BIT DEFAULT 0,
    account_expired BIT DEFAULT 0,
    credentials_expired BIT DEFAULT 0,
    last_login DATETIME2 NULL,
    login_count INT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    created_by NVARCHAR(50) DEFAULT 'system',
    updated_by NVARCHAR(50) DEFAULT 'system'
);

-- Create TAGS table
CREATE TABLE tags (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(255) NULL,
    color NVARCHAR(7) DEFAULT '#007bff',
    usage_count INT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    created_by BIGINT NULL,
    CONSTRAINT FK_tags_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Create DOCUMENTS table
CREATE TABLE documents (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    file_name NVARCHAR(255) NULL,
    file_path NVARCHAR(500) NULL,
    file_type NVARCHAR(100) NULL,
    file_size BIGINT DEFAULT 0,
    content NVARCHAR(MAX) NULL,
    summary NVARCHAR(MAX) NULL,
    keywords NVARCHAR(MAX) NULL,
    folder NVARCHAR(100) NULL,
    status NVARCHAR(20) DEFAULT 'ACTIVE',
    visibility NVARCHAR(20) DEFAULT 'PRIVATE',
    version INT DEFAULT 1,
    checksum NVARCHAR(64) NULL,
    mime_type NVARCHAR(100) NULL,
    page_count INT NULL,
    word_count INT NULL,
    character_count INT NULL,
    language NVARCHAR(10) DEFAULT 'en',
    extracted_at DATETIME2 NULL,
    processed BIT DEFAULT 0,
    processing_error NVARCHAR(MAX) NULL,
    view_count INT DEFAULT 0,
    download_count INT DEFAULT 0,
    last_accessed DATETIME2 NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_documents_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create NOTES table
CREATE TABLE notes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    content NVARCHAR(MAX) NULL,
    summary NVARCHAR(MAX) NULL,
    keywords NVARCHAR(MAX) NULL,
    folder NVARCHAR(100) NULL,
    status NVARCHAR(20) DEFAULT 'ACTIVE',
    visibility NVARCHAR(20) DEFAULT 'PRIVATE',
    version INT DEFAULT 1,
    format NVARCHAR(20) DEFAULT 'PLAIN_TEXT',
    word_count INT DEFAULT 0,
    character_count INT DEFAULT 0,
    language NVARCHAR(10) DEFAULT 'en',
    processed BIT DEFAULT 0,
    processing_error NVARCHAR(MAX) NULL,
    view_count INT DEFAULT 0,
    edit_count INT DEFAULT 0,
    last_accessed DATETIME2 NULL,
    linked_document_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_notes_user_id FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT FK_notes_linked_document FOREIGN KEY (linked_document_id) REFERENCES documents(id)
);

-- Create DOCUMENT_TAGS table
CREATE TABLE document_tags (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    document_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    created_by BIGINT NULL,
    CONSTRAINT FK_document_tags_document FOREIGN KEY (document_id) REFERENCES documents(id),
    CONSTRAINT FK_document_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id),
    CONSTRAINT FK_document_tags_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT UQ_document_tag UNIQUE (document_id, tag_id)
);

-- Create NOTE_TAGS table
CREATE TABLE note_tags (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    note_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    created_by BIGINT NULL,
    CONSTRAINT FK_note_tags_note FOREIGN KEY (note_id) REFERENCES notes(id),
    CONSTRAINT FK_note_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id),
    CONSTRAINT FK_note_tags_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT UQ_note_tag UNIQUE (note_id, tag_id)
);

-- Create SEARCH_HISTORY table
CREATE TABLE search_history (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    query_text NVARCHAR(500) NOT NULL,
    result_count INT DEFAULT 0,
    search_type NVARCHAR(20) DEFAULT 'GENERAL',
    filters_applied NVARCHAR(MAX) NULL,
    execution_time_ms INT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_search_history_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create USER_SESSIONS table
CREATE TABLE user_sessions (
    id NVARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_data NVARCHAR(MAX) NULL,
    ip_address NVARCHAR(45) NULL,
    user_agent NVARCHAR(MAX) NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    last_accessed DATETIME2 DEFAULT GETDATE(),
    expires_at DATETIME2 NOT NULL,
    CONSTRAINT FK_user_sessions_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create indexes for better performance
CREATE INDEX IX_users_username ON users(username);
CREATE INDEX IX_users_email ON users(email);
CREATE INDEX IX_documents_user_id ON documents(user_id);
CREATE INDEX IX_documents_status ON documents(status);
CREATE INDEX IX_documents_created_at ON documents(created_at);
CREATE INDEX IX_notes_user_id ON notes(user_id);
CREATE INDEX IX_notes_status ON notes(status);
CREATE INDEX IX_notes_created_at ON notes(created_at);
CREATE INDEX IX_tags_name ON tags(name);
CREATE INDEX IX_search_history_user_id ON search_history(user_id);
CREATE INDEX IX_search_history_created_at ON search_history(created_at);
CREATE INDEX IX_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IX_user_sessions_expires_at ON user_sessions(expires_at);