package com.monu.threatanalyzer.util;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;

import com.monu.threatanalyzer.model.*;

import java.io.ByteArrayOutputStream;

public class PdfReportGenerator {

    public static byte[] generate(Scanresult result) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Threat Analysis Report"));
            document.add(new Paragraph("Scan ID: " + result.getScanId()));
            document.add(new Paragraph("Risk Level: " + result.getRiskLevel()));
            document.add(new Paragraph("Threat Score: " + result.getThreatScore()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Findings:"));

            for (ThreatFinding f : result.getFindings()) {
                String line = "- " + f.getType()
                        + " | " + f.getSeverity()
                        + " | " + f.getFile();
                if (f.getRemediation() != null && !f.getRemediation().isBlank()) {
                    line += " | Remediation: " + f.getRemediation();
                }
                document.add(new Paragraph(line));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Detected Technologies"));
            for (TechnologyFinding t : result.getTechnologies()) {
                document.add(new Paragraph("- " + t.getTechnology()));
            }

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}