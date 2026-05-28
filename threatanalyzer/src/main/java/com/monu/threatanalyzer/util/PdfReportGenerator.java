package com.monu.threatanalyzer.util;

import com.itextpdf.kernel.colors.ColorConstants;
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

            document.add(new Paragraph("Threat Analysis Report")
                    .setFontSize(18)
                    .setBold()
                    .setFontColor(ColorConstants.BLACK));
            document.add(new Paragraph("Scan ID: " + result.getScanId())
                    .setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY));
            document.add(new Paragraph("Risk Level: " + result.getRiskLevel())
                    .setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY));
            document.add(new Paragraph("Threat Score: " + result.getThreatScore())
                    .setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY));
            document.add(new Paragraph(" "));

            int criticalCount = 0;
            int highCount = 0;
            int mediumCount = 0;
            int lowCount = 0;

            for (ThreatFinding f : result.getFindings()) {
                String severity = f.getSeverity() == null ? "" : f.getSeverity().toUpperCase();
                switch (severity) {
                    case "CRITICAL" -> criticalCount++;
                    case "HIGH" -> highCount++;
                    case "MEDIUM" -> mediumCount++;
                    case "LOW" -> lowCount++;
                }
            }

            Table summaryTable = new Table(new float[]{3, 2});
            summaryTable.addHeaderCell(new Cell().add(new Paragraph("Summary").setBold()));
            summaryTable.addHeaderCell(new Cell().add(new Paragraph("Value").setBold()));
            summaryTable.addCell(new Cell().add(new Paragraph("Total Findings")));
            summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(result.getFindings().size()))));
            summaryTable.addCell(new Cell().add(new Paragraph("Critical")));
            summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(criticalCount))));
            summaryTable.addCell(new Cell().add(new Paragraph("High")));
            summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(highCount))));
            summaryTable.addCell(new Cell().add(new Paragraph("Medium")));
            summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(mediumCount))));
            summaryTable.addCell(new Cell().add(new Paragraph("Low")));
            summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(lowCount))));
            document.add(summaryTable);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Findings")
                    .setFontSize(14)
                    .setBold());

            Table findingsTable = new Table(new float[]{2, 1, 2, 1, 2, 3});
            findingsTable.addHeaderCell(new Cell().add(new Paragraph("Type").setBold()));
            findingsTable.addHeaderCell(new Cell().add(new Paragraph("Severity").setBold()));
            findingsTable.addHeaderCell(new Cell().add(new Paragraph("Summary").setBold()));
            findingsTable.addHeaderCell(new Cell().add(new Paragraph("CVE").setBold()));
            findingsTable.addHeaderCell(new Cell().add(new Paragraph("Remediation").setBold()));
            findingsTable.addHeaderCell(new Cell().add(new Paragraph("File / Location").setBold()));

            for (ThreatFinding f : result.getFindings()) {
                findingsTable.addCell(new Cell().add(new Paragraph(f.getType())));
                findingsTable.addCell(new Cell().add(new Paragraph(f.getSeverity() == null ? "Unknown" : f.getSeverity())));
                findingsTable.addCell(new Cell().add(new Paragraph(f.getSummary() == null ? "" : f.getSummary())));
                findingsTable.addCell(new Cell().add(new Paragraph(f.getCveId() == null ? "" : f.getCveId())));
                findingsTable.addCell(new Cell().add(new Paragraph(f.getRemediation() == null ? "" : f.getRemediation())));
                findingsTable.addCell(new Cell().add(new Paragraph(f.getFile() == null ? "" : f.getFile())));
            }

            document.add(findingsTable);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Detected Technologies")
                    .setFontSize(14)
                    .setBold());
            Table techTable = new Table(new float[]{3, 4});
            techTable.addHeaderCell(new Cell().add(new Paragraph("Technology").setBold()));
            techTable.addHeaderCell(new Cell().add(new Paragraph("Source").setBold()));
            for (TechnologyFinding t : result.getTechnologies()) {
                techTable.addCell(new Cell().add(new Paragraph(t.getTechnology())));
                techTable.addCell(new Cell().add(new Paragraph(t.getDetectedFrom() == null ? "" : t.getDetectedFrom())));
            }
            document.add(techTable);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Project Metadata")
                    .setFontSize(14)
                    .setBold());
            Table metadataTable = new Table(new float[]{3, 5});
            metadataTable.addCell(new Cell().add(new Paragraph("Project")));
            metadataTable.addCell(new Cell().add(new Paragraph(result.getMetadata() == null ? "Unknown" : result.getMetadata().getProjectName())));
            metadataTable.addCell(new Cell().add(new Paragraph("Source")));
            metadataTable.addCell(new Cell().add(new Paragraph(result.getMetadata() == null ? "Unknown" : result.getMetadata().getSourceType())));
            metadataTable.addCell(new Cell().add(new Paragraph("Repository")));
            metadataTable.addCell(new Cell().add(new Paragraph(result.getMetadata() == null ? "Local Upload" : result.getMetadata().getRepositoryUrl())));
            metadataTable.addCell(new Cell().add(new Paragraph("Total Files")));
            metadataTable.addCell(new Cell().add(new Paragraph(result.getMetadata() == null ? "Unknown" : String.valueOf(result.getMetadata().getTotalFiles()))));
            document.add(metadataTable);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}