package com.Athenaeum.service;

import com.Athenaeum.entity.Document;
import com.Athenaeum.entity.DocumentTag;
import com.Athenaeum.entity.Tag;
import com.Athenaeum.entity.User;
import com.Athenaeum.repository.DocumentRepository;
import com.Athenaeum.repository.TagRepository;
import com.Athenaeum.util.FileUtil;
import com.Athenaeum.util.TextExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private TextExtractor textExtractor;

    @Autowired
    private NLPService nlpService;

    public Document uploadDocument(MultipartFile file, String title, String folder, 
                                 String[] tags, User user) throws IOException {

        // Save file to filesystem
        String filePath = fileUtil.saveFile(file);

        // Extract text content
        String content = textExtractor.extractTextFromFile(filePath);

        // Create document entity
        Document document = new Document(
            title != null ? title : file.getOriginalFilename(),
            file.getOriginalFilename(),
            filePath,
            fileUtil.getContentType(file.getOriginalFilename()),
            user
        );

        document.setContent(content);
        document.setFileSize(file.getSize());
        document.setFolder(folder);

        // Generate summary and keywords using NLP
        document.setSummary(nlpService.generateSummary(content));
        document.setKeywords(nlpService.extractKeywords(content));

        // Save document
        document = documentRepository.save(document);

        // Process and save tags
        if (tags != null && tags.length > 0) {
            processTags(document, Arrays.asList(tags));
        }

        return document;
    }

    private void processTags(Document document, List<String> tagNames) {
        for (String tagName : tagNames) {
            if (tagName != null && !tagName.trim().isEmpty()) {
                Tag tag = tagRepository.findByName(tagName.trim())
                    .orElse(new Tag(tagName.trim()));

                if (tag.getId() == null) {
                    tag = tagRepository.save(tag);
                }

                DocumentTag documentTag = new DocumentTag(document, tag);
                document.getDocumentTags().add(documentTag);
            }
        }
    }

    public List<Document> getUserDocuments(User user) {
        return documentRepository.findByUser(user);
    }

    public List<Document> getUserDocumentsByFolder(User user, String folder) {
        return documentRepository.findByUserAndFolder(user, folder);
    }

    public Optional<Document> findById(Long id) {
        return documentRepository.findById(id);
    }

    public void deleteDocument(Long id, User user) throws IOException {
        Optional<Document> documentOptional = documentRepository.findById(id);
        if (documentOptional.isPresent()) {
            Document document = documentOptional.get();
            if (document.getUser().getId().equals(user.getId())) {
                // Delete file from filesystem
                fileUtil.deleteFile(document.getFilePath());
                // Delete from database
                documentRepository.delete(document);
            } else {
                throw new RuntimeException("Unauthorized to delete this document");
            }
        }
    }

    public List<String> getUserFolders(User user) {
        return documentRepository.findDistinctFoldersByUser(user);
    }

    public List<Document> searchDocuments(User user, String keyword) {
        return documentRepository.searchByKeyword(user, keyword);
    }

	public void uploadfolder(String title, String folder, String[] tags, User user) {
		// TODO Auto-generated method stub
		
	}
}