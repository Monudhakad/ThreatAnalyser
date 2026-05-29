package com.monu.threatanalyzer.util;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;

import com.monu.threatanalyzer.model.*;

import java.io.ByteArrayOutputStream;

public class PdfReportGenerator {

    private static Color getSeverityColor(String severity) {
        if (severity == null) return ColorConstants.GRAY;
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> new DeviceRgb(153, 27, 27);
            case "HIGH" -> new DeviceRgb(239, 68, 68);
            case "MEDIUM" -> new DeviceRgb(245, 158, 11);
            case "LOW" -> new DeviceRgb(16, 185, 129);
            default -> ColorConstants.GRAY;
        };
    }

    public static byte[] generate(Scanresult result) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title with styling
            document.add(new Paragraph("🛡️ ThreatAnalyzer Security Report")
                    .setFontSize(28)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLACK)
                    .setMarginBottom(10));
            
            document.add(new Paragraph("Comprehensive Vulnerability Assessment")
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginBottom(20));

            // Metadata section
            document.add(new Paragraph("Report Metadata")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(new DeviceRgb(59, 130, 246))
                    .setMarginTop(10)
                    .setMarginBottom(8));
            
            Table metaTable = new Table(new float[]{2, 5});
            metaTable.setWidth(500);
            metaTable.addCell(createStyledCell("Scan ID", true));
            metaTable.addCell(createStyledCell(result.getScanId(), false));
            metaTable.addCell(createStyledCell("Risk Level", true));
            Color riskColor = "HIGH".equals(result.getRiskLevel()) ? new DeviceRgb(239, 68, 68) : 
                             "MEDIUM".equals(result.getRiskLevel()) ? new DeviceRgb(245, 158, 11) : 
                             new DeviceRgb(16, 185, 129);
            metaTable.addCell(createStyledCell(result.getRiskLevel(), false).setFontColor(riskColor));
            metaTable.addCell(createStyledCell("Threat Score", true));
            metaTable.addCell(createStyledCell(String.valueOf(result.getThreatScore()), false));
            document.add(metaTable);
            document.add(new Paragraph(" "));

            // Severity Summary Section
            document.add(new Paragraph("Severity Summary")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(new DeviceRgb(59, 130, 246))
                    .setMarginTop(15)
                    .setMarginBottom(8));

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

            Table summaryTable = new Table(new float[]{2, 1.5f, 1.5f, 1.5f, 1.5f});
            summaryTable.addHeaderCell(createHeaderCell("Category"));
            summaryTable.addHeaderCell(createHeaderCell("Critical"));
            summaryTable.addHeaderCell(createHeaderCell("High"));
            summaryTable.addHeaderCell(createHeaderCell("Medium"));
            summaryTable.addHeaderCell(createHeaderCell("Low"));
            
            summaryTable.addCell(createStyledCell("Count", true));
            summaryTable.addCell(createSeverityCell(String.valueOf(criticalCount), "CRITICAL"));
            summaryTable.addCell(createSeverityCell(String.valueOf(highCount), "HIGH"));
            summaryTable.addCell(createSeverityCell(String.valueOf(mediumCount), "MEDIUM"));
            summaryTable.addCell(createSeverityCell(String.valueOf(lowCount), "LOW"));
            
            summaryTable.addCell(createStyledCell("Total Findings", true));
            summaryTable.addCell(createStyledCell(String.valueOf(result.getFindings().size()), false));
            document.add(summaryTable);
            document.add(new Paragraph(" "));

            // Detailed Findings Section
            if (!result.getFindings().isEmpty()) {
                document.add(new Paragraph("Security Findings")
                        .setFontSize(14)
                        .setBold()
                        .setFontColor(new DeviceRgb(59, 130, 246))
                        .setMarginTop(15)
                        .setMarginBottom(8));

                Table findingsTable = new Table(new float[]{1.5f, 1, 2.5f, 1.2f, 2, 2.8f});
                findingsTable.addHeaderCell(createHeaderCell("Type"));
                findingsTable.addHeaderCell(createHeaderCell("Severity"));
                findingsTable.addHeaderCell(createHeaderCell("Summary"));
                findingsTable.addHeaderCell(createHeaderCell("CVE ID"));
                findingsTable.addHeaderCell(createHeaderCell("Remediation"));
                findingsTable.addHeaderCell(createHeaderCell("File"));

                for (ThreatFinding f : result.getFindings()) {
                    findingsTable.addCell(createStyledCell(f.getType(), false));
                    findingsTable.addCell(createSeverityCell(f.getSeverity() == null ? "Unknown" : f.getSeverity(), f.getSeverity()));
                    findingsTable.addCell(createStyledCell(f.getSummary() == null ? "" : f.getSummary(), false));
                    findingsTable.addCell(createStyledCell(f.getCveId() == null ? "" : f.getCveId(), false));
                    findingsTable.addCell(createStyledCell(f.getRemediation() == null ? "" : f.getRemediation(), false));
                    findingsTable.addCell(createStyledCell(truncateText(f.getFile(), 60), false));
                }

                document.add(findingsTable);
                document.add(new Paragraph(" "));
            }

            // Detected Technologies Section
            if (!result.getTechnologies().isEmpty()) {
                document.add(new Paragraph("Detected Technologies")
                        .setFontSize(14)
                        .setBold()
                        .setFontColor(new DeviceRgb(59, 130, 246))
                        .setMarginTop(15)
                        .setMarginBottom(8));

                Table techTable = new Table(new float[]{3, 4});
                techTable.addHeaderCell(createHeaderCell("Technology"));
                techTable.addHeaderCell(createHeaderCell("Source"));

                for (TechnologyFinding t : result.getTechnologies()) {
                    techTable.addCell(createStyledCell(t.getTechnology(), false));
                    techTable.addCell(createStyledCell(t.getDetectedFrom() == null ? "" : t.getDetectedFrom(), false));
                }

                document.add(techTable);
                document.add(new Paragraph(" "));
            }

            // Project Metadata Section
            if (result.getMetadata() != null) {
                document.add(new Paragraph("Project Metadata")
                        .setFontSize(14)
                        .setBold()
                        .setFontColor(new DeviceRgb(59, 130, 246))
                        .setMarginTop(15)
                        .setMarginBottom(8));

                Table metadataTable = new Table(new float[]{3, 5});
                metadataTable.addCell(createHeaderCell("Property"));
                metadataTable.addCell(createHeaderCell("Value"));
                
                metadataTable.addCell(createStyledCell("Project Name", true));
                metadataTable.addCell(createStyledCell(result.getMetadata().getProjectName() == null ? "Unknown" : result.getMetadata().getProjectName(), false));
                metadataTable.addCell(createStyledCell("Source Type", true));
                metadataTable.addCell(createStyledCell(result.getMetadata().getSourceType() == null ? "Unknown" : result.getMetadata().getSourceType(), false));
                metadataTable.addCell(createStyledCell("Repository URL", true));
                metadataTable.addCell(createStyledCell(result.getMetadata().getRepositoryUrl() == null ? "Local Upload" : result.getMetadata().getRepositoryUrl(), false));
                metadataTable.addCell(createStyledCell("Total Files", true));
                metadataTable.addCell(createStyledCell(String.valueOf(result.getMetadata().getTotalFiles()), false));

                document.add(metadataTable);
            }

            // Footer
            document.add(new Paragraph(" ").setMarginTop(30));
            document.add(new Paragraph("ThreatAnalyzer v1.0 | Enterprise Security Scanning Platform")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY));
            document.add(new Paragraph("Report generated for security analysis purposes")
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY));

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private static Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text)
                        .setBold()
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(new DeviceRgb(59, 130, 246))
                .setPadding(8);
    }

    private static String truncateText(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        int edge = (maxLength - 3) / 2;
        return text.substring(0, edge) + "..." + text.substring(text.length() - edge);
    }

    private static Cell createStyledCell(String text, boolean isBold) {
        Paragraph p = new Paragraph(text)
                .setFontSize(10)
                .setFontColor(ColorConstants.BLACK);
        if (isBold) {
            p.setBold();
            p.setBackgroundColor(new DeviceRgb(240, 240, 240));
        }
        return new Cell().add(p).setPadding(6);
    }

    private static Cell createSeverityCell(String text, String severity) {
        return new Cell()
                .add(new Paragraph(text)
                        .setBold()
                        .setFontColor(ColorConstants.WHITE)
                        .setFontSize(10))
                .setBackgroundColor(getSeverityColor(severity))
                .setPadding(6)
                .setTextAlignment(TextAlignment.CENTER);
    }
}