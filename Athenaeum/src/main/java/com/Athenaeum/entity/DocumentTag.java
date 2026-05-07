package com.Athenaeum.entity;

import javax.persistence.*;

@Entity
@Table(name = "document_tags")
public class DocumentTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    // Constructors
    public DocumentTag() {}

    public DocumentTag(Document document, Tag tag) {
        this.document = document;
        this.tag = tag;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public Tag getTag() { return tag; }
    public void setTag(Tag tag) { this.tag = tag; }
}