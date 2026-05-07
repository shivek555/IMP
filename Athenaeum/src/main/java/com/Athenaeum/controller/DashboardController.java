package com.Athenaeum.controller;

import com.Athenaeum.dto.SearchResult;
import com.Athenaeum.entity.Document;
import com.Athenaeum.entity.Note;
import com.Athenaeum.entity.User;
import com.Athenaeum.service.AuthService;
import com.Athenaeum.service.DocumentService;
import com.Athenaeum.service.NoteService;
import com.Athenaeum.service.SearchService;
import com.Athenaeum.service.GraphService;
import com.Athenaeum.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;  // ✅ Correct - Spring version


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class DashboardController {

    @Autowired
    private AuthService authService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GraphService graphService;

    @Autowired
    private FileUtil fileUtil;

    @RequestMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = authService.findByUsername(username).orElse(null);

        if (user == null) {
            return "redirect:/auth/login";
        }

        List<Document> documents = documentService.getUserDocuments(user);
        List<Note> notes = noteService.getUserNotes(user);
        List<String> documentFolders = documentService.getUserFolders(user);
        List<String> noteFolders = noteService.getUserFolders(user);
        Map<String, Object> graphData = graphService.getKnowledgeGraph(user);

        model.addAttribute("user", user);
        model.addAttribute("documents", documents);
        model.addAttribute("notes", notes);
        model.addAttribute("documentFolders", documentFolders);
        model.addAttribute("noteFolders", noteFolders);
        model.addAttribute("graphData", graphData);
        model.addAttribute("totalDocuments", documents.size());
        model.addAttribute("totalNotes", notes.size());

        return "dashboard";
    }

    @RequestMapping("/dashboard/graph")
    public String graphView(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = authService.findByUsername(username).orElse(null);

        if (user == null) {
            return "redirect:/auth/login";
        }

        Map<String, Object> graphData = graphService.getKnowledgeGraph(user);
        model.addAttribute("graphData", graphData);
        return "graph-view";
    }

    @GetMapping("/documents/upload")
    public String uploadForm() {
        return "upload";
    }

    @PostMapping("/documents/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file,
                               @RequestParam(value = "title", required = false) String title,
                               @RequestParam(value = "folder", required = false) String folder,
                               @RequestParam(value = "tags", required = false) String[] tags,
                               Model model) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = authService.findByUsername(username).orElse(null);

        if (user == null) {
            return "redirect:/auth/login";
        }

        documentService.uploadDocument(file, title, folder, tags, user);
        return "redirect:/dashboard";
    }

    @GetMapping("/notes/create")
    public String showCreateNoteForm(Model model) {
        model.addAttribute("noteRequest", new com.Athenaeum.dto.NoteCreateRequest());
        return "note-create";
    }

    @PostMapping("/notes/create")
    public String createNote(@ModelAttribute("noteRequest") com.Athenaeum.dto.NoteCreateRequest noteRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = authService.findByUsername(username).orElse(null);

        if (user == null) {
            return "redirect:/auth/login";
        }

        noteService.createNote(
                noteRequest.getTitle(),
                noteRequest.getContent(),
                noteRequest.getFolder(),
                noteRequest.getTags(),
                user
        );
        return "redirect:/dashboard";
    }
    

    @GetMapping("/notes/{id}")
    public String viewNote(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = authService.findByUsername(username).orElse(null);

        if (user == null) {
            return "redirect:/auth/login";
        }

        Optional<Note> noteOpt = noteService.findById(id);
        if (noteOpt.isEmpty() || !noteOpt.get().getUser().getId().equals(user.getId())) {
            return "redirect:/dashboard"; // or a 404 page if preferred
        }

        model.addAttribute("note", noteOpt.get());
        model.addAttribute("relatedItems", graphService.getRelatedItems(id, "Note"));
        return "note-view"; // Thymeleaf template to display the note
    }
    @PostMapping("/notes/{id}/delete")
    public String deleteNote(@PathVariable Long id, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = authService.findByUsername(username).orElse(null);

            if (user == null) {
                return "redirect:/auth/login";
            }

           noteService.deleteNote(id, user);
            graphService.deleteNode(id, "Note");
            model.addAttribute("success", "Note deleted successfully");
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Delete failed: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }
    
    @GetMapping("/notes/{id}/edit")
    @Transactional(readOnly = true)
    public String showEditNoteForm(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = authService.findByUsername(username).orElse(null);

        if (user == null) {
            return "redirect:/auth/login";
        }

        Optional<Note> noteOpt = noteService.findById(id);
        if (noteOpt.isEmpty() || !noteOpt.get().getUser().getId().equals(user.getId())) {
            return "redirect:/dashboard";
        }

        Note note = noteOpt.get();
        model.addAttribute("note", note);
        
        // Convert tags to comma-separated string for display
        String tagsString = "";
        if (note.getNoteTags() != null && !note.getNoteTags().isEmpty()) {
            tagsString = note.getNoteTags().stream()
                    .map(noteTag -> noteTag.getTag().getName())
                    .reduce((tag1, tag2) -> tag1 + ", " + tag2)
                    .orElse("");
        }
        model.addAttribute("tagsString", tagsString);
        
        return "note-edit";
    }

    @PostMapping("/notes/{id}/edit")
    public String updateNote(@PathVariable Long id,
                            @RequestParam String title,
                            @RequestParam String content,
                            @RequestParam(required = false) String folder,
                            @RequestParam(required = false) String tags,
                            Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = authService.findByUsername(username).orElse(null);

        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            // Convert tags string to array
            String[] tagsArray = new String[0];
            if (tags != null && !tags.trim().isEmpty()) {
                tagsArray = tags.split(",");
                for (int i = 0; i < tagsArray.length; i++) {
                    tagsArray[i] = tagsArray[i].trim();
                }
            }

            noteService.updateNote(id, title, content, folder, tagsArray, user);
            return "redirect:/notes/" + id + "?success=updated";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update note: " + e.getMessage());
            Optional<Note> noteOpt = noteService.findById(id);
            if (noteOpt.isPresent()) {
                model.addAttribute("note", noteOpt.get());
                model.addAttribute("tagsString", tags);
            }
            return "note-edit";
        }
    }

    @GetMapping("/search")
    public String searchForm() {
        return "search";
    }

    @PostMapping("/search")
    public String search(@RequestParam("q") String query, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = authService.findByUsername(username).orElse(null);

        if (user == null) return "redirect:/auth/login";

        List<SearchResult> results = searchService.search(user, query);
        model.addAttribute("query", query);
        model.addAttribute("results", results);
        return "search-results";
    }

    @GetMapping("/auth/logout")
    public String logout() {
        return "redirect:/auth/login";
    }

  

    // Added DocumentController methods below

    @GetMapping("/documents/{id}")
    public String viewDocument(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = authService.findByUsername(username).orElse(null);

        if (user == null) {
            return "redirect:/auth/login";
        }

        Optional<Document> documentOptional = documentService.findById(id);
        if (documentOptional.isPresent()) {
            Document document = documentOptional.get();
            if (document.getUser().getId().equals(user.getId())) {
                model.addAttribute("document", document);
                List<Map<String, Object>> relatedItems = 
                    graphService.getRelatedItems(id, "Document");
                model.addAttribute("relatedItems", relatedItems);
                return "document-view";
            }
        }
        model.addAttribute("error", "Document not found");
        return "redirect:/dashboard";
    }

    @GetMapping("/documents/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadDocument(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = authService.findByUsername(username).orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Document> documentOptional = documentService.findById(id);
            if (documentOptional.isPresent()) {
                Document document = documentOptional.get();
                if (document.getUser().getId().equals(user.getId())) {
                    byte[] data = fileUtil.readFile(document.getFilePath());
                    ByteArrayResource resource = new ByteArrayResource(data);
                    return ResponseEntity.ok()
                        .contentLength(data.length)
                        .contentType(MediaType.parseMediaType(document.getFileType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                               "attachment; filename=\"" + document.getFileName() + "\"")
                        .body(resource);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/documents/{id}/delete")
    public String deleteDocument(@PathVariable Long id, Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = authService.findByUsername(username).orElse(null);

            if (user == null) {
                return "redirect:/auth/login";
            }

            documentService.deleteDocument(id, user);
            graphService.deleteNode(id, "Document");
            model.addAttribute("success", "Document deleted successfully");
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Delete failed: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/documents/folder/{folder}")
    public String viewFolder(@PathVariable String folder, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = authService.findByUsername(username).orElse(null);

        if (user == null) {
            return "redirect:/auth/login";
        }

        List<Document> documents = documentService.getUserDocumentsByFolder(user, folder);
        model.addAttribute("documents", documents);
        model.addAttribute("folderName", folder);
        model.addAttribute("itemType", "documents");
        return "folder-view";
    }
}
