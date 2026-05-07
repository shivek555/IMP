USE athenaeum_db;

-- Delete data in correct order (if any exists)
DELETE FROM document_tags;
DELETE FROM note_tags;
DELETE FROM search_history;
DELETE FROM user_sessions;
DELETE FROM notes;
DELETE FROM documents;
DELETE FROM tags;
DELETE FROM users;

-- Reset identity counters
DBCC CHECKIDENT ('users', RESEED, 0);
DBCC CHECKIDENT ('tags', RESEED, 0);
DBCC CHECKIDENT ('documents', RESEED, 0);
DBCC CHECKIDENT ('notes', RESEED, 0);
DBCC CHECKIDENT ('document_tags', RESEED, 0);
DBCC CHECKIDENT ('note_tags', RESEED, 0);
DBCC CHECKIDENT ('search_history', RESEED, 0);

-- Insert users (will get IDs 1, 2, 3)
INSERT INTO users (username, email, password, first_name, last_name, enabled) 
VALUES ('system', 'system@athenaeum.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM3lbdxSDwwVGHrA7Rlmu', 'System', 'User', 0);

INSERT INTO users (username, email, password, first_name, last_name, enabled) 
VALUES ('admin', 'admin@athenaeum.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM3lbdxSDwwVGHrA7Rlmu', 'Admin', 'User', 1);

INSERT INTO users (username, email, password, first_name, last_name, enabled) 
VALUES ('john.doe', 'john.doe@university.edu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM3lbdxSDwwVGHrA7Rlmu', 'John', 'Doe', 1);

-- Insert tags (will get IDs 1-10)
INSERT INTO tags (name, description, color, created_by) 
VALUES ('academic', 'Academic papers and research materials', '#007bff', 2);

INSERT INTO tags (name, description, color, created_by) 
VALUES ('research', 'Research-related documents and notes', '#28a745', 2);

INSERT INTO tags (name, description, color, created_by) 
VALUES ('personal', 'Personal notes and documents', '#ffc107', 2);

INSERT INTO tags (name, description, color, created_by) 
VALUES ('project', 'Project-related materials', '#17a2b8', 2);

INSERT INTO tags (name, description, color, created_by) 
VALUES ('important', 'Important documents requiring attention', '#dc3545', 2);

INSERT INTO tags (name, description, color, created_by) 
VALUES ('draft', 'Draft documents and work in progress', '#6c757d', 2);

INSERT INTO tags (name, description, color, created_by) 
VALUES ('published', 'Published papers and final documents', '#198754', 2);

INSERT INTO tags (name, description, color, created_by) 
VALUES ('reference', 'Reference materials and citations', '#0d6efd', 2);

INSERT INTO tags (name, description, color, created_by) 
VALUES ('meeting', 'Meeting notes and minutes', '#fd7e14', 2);

INSERT INTO tags (name, description, color, created_by) 
VALUES ('tutorial', 'Tutorial and educational content', '#6f42c1', 2);

-- Insert documents (will get IDs 1, 2, 3)
INSERT INTO documents (title, file_name, file_type, content, summary, keywords, status, user_id) 
VALUES ('Introduction to Machine Learning', 'ml_intro.pdf', 'application/pdf', 'Machine learning is a subset of artificial intelligence that focuses on developing algorithms and statistical models that enable computer systems to improve their performance on a specific task through experience.', 'Comprehensive introduction to machine learning concepts', 'machine learning, AI, algorithms', 'ACTIVE', 2);

INSERT INTO documents (title, file_name, file_type, content, summary, keywords, status, user_id) 
VALUES ('Research Methodology Guide', 'research_methods.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'Research methodology encompasses systematic approaches to conducting research, including problem identification, literature review, hypothesis formation, data collection, and analysis.', 'Guide covering research methodologies', 'research methodology, academic research', 'ACTIVE', 2);

INSERT INTO documents (title, file_name, file_type, content, summary, keywords, status, user_id) 
VALUES ('NLP Project Proposal', 'nlp_project_proposal.txt', 'text/plain', 'PROJECT TITLE: Advanced Natural Language Processing Applications for Document Management\n\nOBJECTIVE: To develop and implement NLP-enhanced features for the Athenaeum document management system.', 'Research proposal for NLP-enhanced document management', 'NLP, project proposal, document management', 'ACTIVE', 3);

INSERT INTO documents (title, file_name, file_type, content, summary, keywords, status, visibility, user_id)
VALUES ('Getting Started Guide', 'getting-started.pdf', 'application/pdf', 'This is a comprehensive guide to get you started with the Athenaeum system. It covers basic navigation, document upload, search functionality, and user management.', 'Introduction guide for new users', 'getting started, user guide, tutorial', 'ACTIVE', 'PUBLIC', 2);

INSERT INTO documents (title, file_name, file_type, content, summary, keywords, status, visibility, user_id)
VALUES ('Project Proposal 2025', 'project-proposal-2025.pdf', 'application/pdf', 'Detailed project proposal for the academic year 2025, covering research objectives, methodology, timeline, and expected outcomes.', 'Academic project proposal for 2025', 'project proposal, academic, 2025', 'ACTIVE', 'SHARED', 3);

-- Insert notes with explicit IDs
SET IDENTITY_INSERT notes ON;

INSERT INTO notes (id, title, content, summary, keywords, status, user_id) 
VALUES (1, 'Project Kickoff Meeting Notes', 'Meeting held on September 1, 2025\n\nDiscussed project scope:\n- Document management system enhancement\n- NLP integration timeline\n- Resource allocation\n- Next milestones\n\nAction items:\n- Setup development environment\n- Research NLP libraries\n- Create project roadmap', 'Detailed notes from project kickoff meeting', 'meeting notes, project planning', 'ACTIVE', 2);

INSERT INTO notes (id, title, content, summary, keywords, status, user_id) 
VALUES (2, 'Literature Review Planning', 'Research areas to explore:\n- Document management systems\n- NLP applications in information retrieval\n- Machine learning for text classification\n- User experience in document systems\n\nKey papers to review:\n- "Modern Information Retrieval" by Baeza-Yates\n- Recent ACL papers on document classification\n- UX research on knowledge management systems', 'Planning document for literature review research', 'literature review, research planning', 'ACTIVE', 3);

INSERT INTO notes (id, title, content, summary, format, status, visibility, user_id)
VALUES (3, 'Meeting Notes - September 2025', 'Key points discussed in the monthly team meeting:\n\n## Agenda\n1. Project timeline updates\n2. Resource allocation\n3. Next milestones\n\n## Action Items\n- [ ] Update project documentation\n- [ ] Schedule next review\n- [ ] Prepare demo for stakeholders', 'Monthly team meeting summary', 'meeting notes, team meeting, september', 'MARKDOWN', 'ACTIVE', 'PRIVATE', 2);

INSERT INTO notes (id, title, content, summary, format, status, visibility, user_id, linked_document_id)
VALUES (4, 'Research Ideas', 'Brainstorming session notes for upcoming research projects.\n\nFocus areas:\n- Artificial Intelligence applications\n- Machine Learning algorithms\n- Data Science methodologies\n- Natural Language Processing\n\nPotential collaborations:\n- University research labs\n- Industry partnerships\n- Open source projects', 'Research brainstorming notes', 'research ideas, brainstorming, AI, ML', 'PLAIN_TEXT', 'ACTIVE', 'PRIVATE', 3, 2);

SET IDENTITY_INSERT notes OFF;

-- Insert document-tag relationships
INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (1, 1, 2); -- ML intro: academic
INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (1, 2, 2); -- ML intro: research
INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (1, 8, 2); -- ML intro: reference
INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (1, 10, 2); -- ML intro: tutorial

INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (2, 1, 2); -- Research methods: academic
INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (2, 8, 2); -- Research methods: reference
INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (2, 10, 2); -- Research methods: tutorial

INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (3, 4, 3); -- NLP proposal: project
INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (3, 6, 3); -- NLP proposal: draft
INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (3, 2, 3); -- NLP proposal: research

INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (4, 5, 2); -- Getting started: important
INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (4, 10, 2); -- Getting started: tutorial

INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (5, 1, 3); -- Project proposal 2025: academic
INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (5, 4, 3); -- Project proposal 2025: project
INSERT INTO document_tags (document_id, tag_id, created_by) VALUES (5, 5, 3); -- Project proposal 2025: important

-- Insert note-tag relationships
INSERT INTO note_tags (note_id, tag_id, created_by) VALUES (1, 9, 2); -- Meeting notes: meeting
INSERT INTO note_tags (note_id, tag_id, created_by) VALUES (1, 4, 2); -- Meeting notes: project
INSERT INTO note_tags (note_id, tag_id, created_by) VALUES (1, 5, 2); -- Meeting notes: important

INSERT INTO note_tags (note_id, tag_id, created_by) VALUES (2, 2, 3); -- Literature review: research
INSERT INTO note_tags (note_id, tag_id, created_by) VALUES (2, 1, 3); -- Literature review: academic
INSERT INTO note_tags (note_id, tag_id, created_by) VALUES (2, 6, 3); -- Literature review: draft

INSERT INTO note_tags (note_id, tag_id, created_by) VALUES (3, 5, 2); -- Meeting Sep 2025: important
INSERT INTO note_tags (note_id, tag_id, created_by) VALUES (3, 9, 2); -- Meeting Sep 2025: meeting

INSERT INTO note_tags (note_id, tag_id, created_by) VALUES (4, 2, 3); -- Research ideas: research
INSERT INTO note_tags (note_id, tag_id, created_by) VALUES (4, 3, 3); -- Research ideas: personal

-- Insert search history
INSERT INTO search_history (user_id, query_text, result_count, search_type) 
VALUES (2, 'machine learning algorithms', 12, 'GENERAL');

INSERT INTO search_history (user_id, query_text, result_count, search_type) 
VALUES (2, 'research methodology', 15, 'DOCUMENT');

INSERT INTO search_history (user_id, query_text, result_count, search_type) 
VALUES (3, 'NLP natural language processing', 22, 'GENERAL');

INSERT INTO search_history (user_id, query_text, result_count, search_type) 
VALUES (2, 'getting started', 5, 'DOCUMENT');

INSERT INTO search_history (user_id, query_text, result_count, search_type) 
VALUES (3, 'project proposal', 1, 'DOCUMENT');