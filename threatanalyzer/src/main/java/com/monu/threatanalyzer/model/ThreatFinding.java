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
    public String gettype(){return type;}
    public String getfile(){return file;}
    public String getseverity(){return severity;}
}
