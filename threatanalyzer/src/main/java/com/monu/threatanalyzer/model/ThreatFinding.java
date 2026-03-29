package com.monu.threatanalyzer.model;

public class ThreatFinding {
    private String type;
    private String file;
    private String severity;

    public ThreatFinding(String type, String file, String severity){
        this.type = type;
        this.file = file;
        this.severity = severity;
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
}
