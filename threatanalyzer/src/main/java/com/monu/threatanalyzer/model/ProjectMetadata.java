package com.monu.threatanalyzer.model;

import java.time.LocalDateTime;

public class ProjectMetadata {

    private String scanId;
    private String projectName;
    private String sourceType;
    private String repositoryUrl;
    private int totalFiles;
    private LocalDateTime scanTime;

    public ProjectMetadata(String scanId,
                           String projectName,
                           String sourceType,
                           String repositoryUrl,
                           int totalFiles) {

        this.scanId = scanId;
        this.projectName = projectName;
        this.sourceType = sourceType;
        this.repositoryUrl = repositoryUrl;
        this.totalFiles = totalFiles;
        this.scanTime = LocalDateTime.now();
    }

    public String getScanId() {
        return scanId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public LocalDateTime getScanTime() {
        return scanTime;
    }
}