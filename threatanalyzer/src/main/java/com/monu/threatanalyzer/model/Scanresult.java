package com.monu.threatanalyzer.model;
import java.util.ArrayList;
import java.util.List;
public class Scanresult {
    private String scanId;
    private List<ThreatFinding> findings = new ArrayList<>();

    public Scanresult(String scanId){
        this.scanId = scanId;
    }
    public void addFindings(ThreatFinding f){
        findings.add(f);
    }

    public String getScanId() {return scanId;}
    public List<ThreatFinding> getFindings() {
        return findings;
    }
}
