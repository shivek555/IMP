package com.Athenaeum.entity;

import javax.persistence.*;

@Entity
@Table(name = "note_tags")
public class NoteTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    // Constructors
    public NoteTag() {}

    public NoteTag(Note note, Tag tag) {
        this.note = note;
        this.tag = tag;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }

    public Tag getTag() { return tag; }
    public void setTag(Tag tag) { this.tag = tag; }
}