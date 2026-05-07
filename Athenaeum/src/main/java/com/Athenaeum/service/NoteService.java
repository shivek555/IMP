package com.Athenaeum.service;

import com.Athenaeum.entity.Note;
import com.Athenaeum.entity.NoteTag;
import com.Athenaeum.entity.Tag;
import com.Athenaeum.entity.User;
import com.Athenaeum.repository.NoteRepository;
import com.Athenaeum.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private NLPService nlpService;

    public Note createNote(String title, String content, String folder, 
                          String[] tags, User user) {

        Note note = new Note(title, content, user);
        note.setFolder(folder);

        // Generate summary and keywords using NLP
        note.setSummary(nlpService.generateSummary(content));
        note.setKeywords(nlpService.extractKeywords(content));

        // Save note
        note = noteRepository.save(note);

        // Process and save tags
        if (tags != null && tags.length > 0) {
            processTags(note, Arrays.asList(tags));
        }

        return note;
    }

    private void processTags(Note note, List<String> tagNames) {
        for (String tagName : tagNames) {
            if (tagName != null && !tagName.trim().isEmpty()) {
                Tag tag = tagRepository.findByName(tagName.trim())
                    .orElse(new Tag(tagName.trim()));

                if (tag.getId() == null) {
                    tag = tagRepository.save(tag);
                }

                NoteTag noteTag = new NoteTag(note, tag);
                note.getNoteTags().add(noteTag);
            }
        }
    }

    public List<Note> getUserNotes(User user) {
        return noteRepository.findByUser(user);
    }

    public List<Note> getUserNotesByFolder(User user, String folder) {
        return noteRepository.findByUserAndFolder(user, folder);
    }

    public Optional<Note> findById(Long id) {
        return noteRepository.findById(id);
    }

    public Note updateNote(Long id, String title, String content, String folder, 
                          String[] tags, User user) {
        Optional<Note> noteOptional = noteRepository.findById(id);
        if (noteOptional.isPresent()) {
            Note note = noteOptional.get();
            if (note.getUser().getId().equals(user.getId())) {
                note.setTitle(title);
                note.setContent(content);
                note.setFolder(folder);

                // Regenerate summary and keywords
                note.setSummary(nlpService.generateSummary(content));
                note.setKeywords(nlpService.extractKeywords(content));

                // Clear existing tags and add new ones
                note.getNoteTags().clear();
                if (tags != null && tags.length > 0) {
                    processTags(note, Arrays.asList(tags));
                }

                return noteRepository.save(note);
            } else {
                throw new RuntimeException("Unauthorized to update this note");
            }
        }
        throw new RuntimeException("Note not found");
    }

    public void deleteNote(Long id, User user) {
        Optional<Note> noteOptional = noteRepository.findById(id);
        if (noteOptional.isPresent()) {
            Note note = noteOptional.get();
            if (note.getUser().getId().equals(user.getId())) {
                noteRepository.delete(note);
            } else {
                throw new RuntimeException("Unauthorized to delete this note");
            }
        }
    }

    public List<String> getUserFolders(User user) {
        return noteRepository.findDistinctFoldersByUser(user);
    }

    public List<Note> searchNotes(User user, String keyword) {
        return noteRepository.searchByKeyword(user, keyword);
    }
}