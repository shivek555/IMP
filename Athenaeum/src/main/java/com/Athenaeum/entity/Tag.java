package com.Athenaeum.entity;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DocumentTag> documentTags = new HashSet<>();

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<NoteTag> noteTags = new HashSet<>();

    // Constructors
    public Tag() {}

    public Tag(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Set<DocumentTag> getDocumentTags() { return documentTags; }
    public void setDocumentTags(Set<DocumentTag> documentTags) { this.documentTags = documentTags; }

    public Set<NoteTag> getNoteTags() { return noteTags; }
    public void setNoteTags(Set<NoteTag> noteTags) { this.noteTags = noteTags; }
}
