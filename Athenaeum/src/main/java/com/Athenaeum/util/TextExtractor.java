package com.Athenaeum.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class TextExtractor {

    public String extractTextFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            return extractTextFromPDF(filePath);
        } else if (fileName.endsWith(".docx")) {
            return extractTextFromDOCX(filePath);
        } else if (fileName.endsWith(".doc")) {
            return extractTextFromDOC(filePath);
        } else if (fileName.endsWith(".txt")) {
            return extractTextFromTXT(filePath);
        } else {
            throw new IOException("Unsupported file type: " + fileName);
        }
    }

    private String extractTextFromPDF(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    private String extractTextFromDOCX(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractTextFromDOC(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractTextFromTXT(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    public String cleanText(String text) {
        if (text == null) return "";

        return text.replaceAll( " ", " ")
                  .replaceAll("[\r\n]+", " ")
                  .trim();
    }

    public String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}