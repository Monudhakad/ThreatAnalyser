package com.monu.threatanalyzer.model;

public class ThreatFinding {
    private String type;
    private String file;
    private String severity;
    private String remediation;

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
}
