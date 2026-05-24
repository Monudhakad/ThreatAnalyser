package com.monu.threatanalyzer.model;

public class ThreatFinding {
    private String type;
    private String file;
    private String severity;
    private String remediation;
    private String summary;
    private String description;
    private String cveId;
    private String cveUrl;
    private String source;

    public ThreatFinding(String type, String file, String severity){
        this.type = type;
        this.file = file;
        this.severity = severity;
    }

    public ThreatFinding(String type, String file, String severity, String remediation){
        this.type = type;
        this.file = file;
        this.severity = severity;
        this.remediation = remediation;
    }

    public ThreatFinding(
            String type,
            String file,
            String severity,
            String remediation,
            String summary,
            String description,
            String cveId,
            String cveUrl,
            String source) {
        this.type = type;
        this.file = file;
        this.severity = severity;
        this.remediation = remediation;
        this.summary = summary;
        this.description = description;
        this.cveId = cveId;
        this.cveUrl = cveUrl;
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public String getFile() {
        return file;
    }

    public String getSeverity() {
        return severity;
    }

    public String getRemediation() {
        return remediation;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public String getCveId() {
        return cveId;
    }

    public String getCveUrl() {
        return cveUrl;
    }

    public String getSource() {
        return source;
    }
}
