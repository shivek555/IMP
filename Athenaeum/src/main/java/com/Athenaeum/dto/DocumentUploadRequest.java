package com.Athenaeum.dto;

import com.Athenaeum.entity.User;

public class DocumentUploadRequest {

    private String title;
    private String folder;
    private String[] tags;

    public DocumentUploadRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFolder() { return folder; }
    public void setFolder(String folder) { this.folder = folder; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

	
}