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
                document.add(new Paragraph(
                        "- " + f.gettype()
                        + " | " + f.getseverity()
                        + " | " + f.getfile()
                ));
            }

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}