package com.monu.threatanalyzer.model;

public class TechnologyFinding {

    private String technology;
    private String detectedFrom;

    public TechnologyFinding(String technology,
                             String detectedFrom) {

        this.technology = technology;
        this.detectedFrom = detectedFrom;
    }

    public String getTechnology() {
        return technology;
    }

    public String getDetectedFrom() {
        return detectedFrom;
    }
}
