package com.Athenaeum.service;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NLPService {

    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;

    // Common stop words
    private Set<String> stopWords = new HashSet<>(Arrays.asList(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he", "in", "is", "it",
        "its", "of", "on", "that", "the", "to", "was", "will", "with", "the", "this", "but", "they",
        "have", "had", "what", "said", "each", "which", "their", "time", "if", "up", "out", "many",
        "then", "them", "these", "so", "some", "her", "would", "make", "like", "into", "him", "two",
        "more", "very", "after", "first", "well", "way", "been", "other", "see", "now", "look",
        "only", "come", "could", "people", "take", "year", "your", "just", "should", "any", "also"
    ));

    @PostConstruct
    public void initialize() {
        try {
            // Note: In production, you would download these models from OpenNLP
            // For this MVP, we'll use simple rule-based approaches if models aren't available
            loadModels();
        } catch (Exception e) {
            // Log warning and use fallback methods
            System.out.println("Warning: Could not load OpenNLP models. Using fallback methods.");
        }
    }

    private void loadModels() {
        try {
            // These models would need to be downloaded from OpenNLP website
            // For MVP purposes, we'll implement simple alternatives
        } catch (Exception e) {
            // Use fallback methods
        }
    }

    public String generateSummary(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Simple extractive summarization
        String[] sentences = splitIntoSentences(text);
        if (sentences.length <= 3) {
            return text;
        }

        // Return first 2 sentences as summary (simple approach)
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < Math.min(2, sentences.length); i++) {
            summary.append(sentences[i]).append(" ");
        }

        return summary.toString().trim();
    }

    public String extractKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Simple keyword extraction
        String[] words = text.toLowerCase()
                .replaceAll("[^a-zA-Z\\s]", "")
                .split("\\s+");


        Map<String, Integer> wordFreq = new HashMap<>();

        for (String word : words) {
            if (word.length() > 3 && !stopWords.contains(word)) {
                wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
            }
        }

        // Get top 10 most frequent words
        List<String> keywords = wordFreq.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        return String.join(", ", keywords);
    }

    public String[] splitIntoSentences(String text) {
        if (sentenceDetector != null) {
            return sentenceDetector.sentDetect(text);
        } else {
            // Fallback: simple sentence splitting
            return text.split("[.!?]+");
        }
    }

    public String[] tokenize(String text) {
        if (tokenizer != null) {
            return tokenizer.tokenize(text);
        } else {
            // Fallback: simple tokenization
            return text.toLowerCase()
                .replaceAll("[^a-zA-Z\\s]", "")
                .split("\\s+");
        }
    }

    public List<String> extractNamedEntities(String text) {
        // Simple named entity extraction (proper nouns)
        List<String> entities = new ArrayList<>();
        String[] words = text.split("\\s+");

        for (String word : words) {
            if (word.length() > 1 && Character.isUpperCase(word.charAt(0))) {
                // Remove punctuation
                word = word.replaceAll("[^a-zA-Z]", "");
                if (word.length() > 2 && !stopWords.contains(word.toLowerCase())) {
                    entities.add(word);
                }
            }
        }

        return entities.stream().distinct().collect(Collectors.toList());
    }
}