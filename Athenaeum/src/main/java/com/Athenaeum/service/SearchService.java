package com.Athenaeum.service;

import com.Athenaeum.dto.SearchResult;
import com.Athenaeum.entity.Document;
import com.Athenaeum.entity.Note;
import com.Athenaeum.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private NLPService nlpService;

    public List<SearchResult> search(User user, String query) {
        List<SearchResult> results = new ArrayList<>();

        // Search documents
        List<Document> documents = documentService.searchDocuments(user, query);
        for (Document doc : documents) {
            SearchResult result = new SearchResult();
            result.setId(doc.getId());
            result.setTitle(doc.getTitle());
            result.setType("document");
            result.setContent(truncateContent(doc.getContent(), 200));
            result.setSummary(doc.getSummary());
            result.setKeywords(parseKeywords(doc.getKeywords()));
            result.setFolder(doc.getFolder());
            result.setCreatedAt(doc.getCreatedAt());
            result.setUpdatedAt(doc.getUpdatedAt());
            result.setFileName(doc.getFileName());
            result.setFileType(doc.getFileType());
            result.setFileSize(doc.getFileSize());
            results.add(result);
        }

        // Search notes
        List<Note> notes = noteService.searchNotes(user, query);
        for (Note note : notes) {
            SearchResult result = new SearchResult();
            result.setId(note.getId());
            result.setTitle(note.getTitle());
            result.setType("note");
            result.setContent(truncateContent(note.getContent(), 200));
            result.setSummary(note.getSummary());
            result.setKeywords(parseKeywords(note.getKeywords()));
            result.setFolder(note.getFolder());
            result.setCreatedAt(note.getCreatedAt());
            result.setUpdatedAt(note.getUpdatedAt());
            results.add(result);
        }

        // Sort by relevance (simple scoring based on keyword matches)
        return results.stream()
            .sorted((r1, r2) -> {
                int score1 = calculateRelevanceScore(r1, query);
                int score2 = calculateRelevanceScore(r2, query);
                return Integer.compare(score2, score1); // Descending order
            })
            .collect(Collectors.toList());
    }

    private int calculateRelevanceScore(SearchResult result, String query) {
        int score = 0;
        String lowerQuery = query.toLowerCase();

        // Title matches are weighted more heavily
        if (result.getTitle().toLowerCase().contains(lowerQuery)) {
            score += 10;
        }

        // Content matches
        if (result.getContent().toLowerCase().contains(lowerQuery)) {
            score += 5;
        }

        // Keyword matches
        if (result.getKeywords() != null) {
            for (String keyword : result.getKeywords()) {
                if (keyword.toLowerCase().contains(lowerQuery)) {
                    score += 3;
                }
            }
        }

        // Summary matches
        if (result.getSummary() != null && result.getSummary().toLowerCase().contains(lowerQuery)) {
            score += 2;
        }

        return score;
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    private List<String> parseKeywords(String keywordString) {
        if (keywordString == null || keywordString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(keywordString.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    public List<SearchResult> searchByTag(User user, String tagName) {
        // This would be implemented with tag-based searching
        return search(user, tagName);
    }

    public List<SearchResult> searchInFolder(User user, String folderName, String query) {
        List<SearchResult> allResults = search(user, query);
        return allResults.stream()
            .filter(result -> folderName.equals(result.getFolder()))
            .collect(Collectors.toList());
    }
}