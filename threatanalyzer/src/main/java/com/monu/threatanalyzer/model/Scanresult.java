package com.monu.threatanalyzer.model;
import java.util.ArrayList;
import java.util.List;
public class Scanresult {
    private String scanId;
    private List<ThreatFinding> findings = new ArrayList<>();
    private List<TechnologyFinding> technologies = new ArrayList<>();

    public Scanresult(String scanId){
        this.scanId = scanId;
    }
    public void addFindings(ThreatFinding f){
        findings.add(f);
    }
    public void addTechnology(TechnologyFinding t) {
        technologies.add(t);
    }
    public boolean hasTechnology(String name) {
        return technologies.stream()
                .anyMatch(t ->
                        t.getTechnology()
                         .equalsIgnoreCase(name)
                );
    }

    public String getScanId() {return scanId;}
    public List<ThreatFinding> getFindings() {
        return findings;
    }
    public List<TechnologyFinding> getTechnologies() {
        return technologies;
    }
    private int threatScore;
    private String riskLevel;

    public int getThreatScore() {return threatScore;}
    public void setThreatScore(int threatScore){this.threatScore = threatScore;}

    public String getRiskLevel(){return riskLevel;}
    public void setRiskLevel(String riskLevel){this.riskLevel = riskLevel;}

    private ProjectMetadata metadata;
    public ProjectMetadata getMetadata() {
    return metadata;
    }

    public void setMetadata(ProjectMetadata metadata) {
    this.metadata = metadata;
    }

    public int getTotalFindings() {
        return findings.size();
    }
}
