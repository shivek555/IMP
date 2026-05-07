package com.Athenaeum.dto;
import javax.validation.constraints.NotBlank;

public class NoteCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String folder;
    private String[] tags;

    public NoteCreateRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getFolder() { return folder; }
    public void setFolder(String folder) { this.folder = folder; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }
}