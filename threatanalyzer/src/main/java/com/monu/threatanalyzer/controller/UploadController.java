package com.monu.threatanalyzer.controller;
import com.monu.threatanalyzer.model.Scanresult;
import com.monu.threatanalyzer.service.ScanService;
import com.monu.threatanalyzer.service.StorageServices;
import com.monu.threatanalyzer.util.PdfReportGenerator;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")


//to implement a ZIP file uploading API.
public class UploadController {
    @Autowired
    private StorageServices storageServices;
    @Autowired
    private ScanService scanService;
    @PostMapping("/upload")
    public Map<String, String> uploadZip(@RequestParam("file") MultipartFile file) {
        String scanId = storageServices.generateScanId();
        try {
            storageServices.saveZip(file.getBytes(), scanId);
        } catch (Exception e) {
            throw new RuntimeException("upload failed");
        }
        Scanresult r = scanService.startScan(
                scanId,
                "ZIP_UPLOAD",
                "LOCAL_UPLOAD"
        );

        Map<String,String> response=new HashMap<>();
        response.put("scanId", scanId);
        response.put("status", "UPLOADED");

        return response;
    }
    @PostMapping("/scan")
    public String scanFromWeb(@RequestParam String repoUrl, Model model) {

        if (repoUrl == null || repoUrl.isBlank()) {
            throw new RuntimeException("Repository URL cannot be empty");
        }
        if (!repoUrl.contains("github.com")) {
            throw new RuntimeException("Only GitHub URLs are supported");
        }

        String scanId = storageServices.generateScanId();

        storageServices.urlDownload(repoUrl, scanId);

        Scanresult result = scanService.startScan(
                scanId,
                "GITHUB",
                repoUrl
        );

        model.addAttribute("result", result);

        return "result";
    }



    @GetMapping("/")
    public String home() {
        return "index";
    }
    //to implement zip file download via URL API
    @PostMapping("/upload/github")
    public Map<String, String> uploadwithurl(@RequestParam String repoUrl){
        if (repoUrl == null || repoUrl.isBlank()) {
            throw new RuntimeException("Repository URL cannot be empty");
        }
        if (!repoUrl.contains("github.com")) {
            throw new RuntimeException("Only GitHub URLs are supported");
        }

        String scanId = storageServices.generateScanId();
        storageServices.urlDownload(repoUrl, scanId);
        Scanresult r = scanService.startScan(
                scanId,
                "GITHUB",
                repoUrl
        );
        Map<String, String> response = new HashMap<>();
        response.put("scanId", scanId);
        response.put("status", "UPLOADED");
        return response;
    }
    @GetMapping("/scan/{scanId}/report")
    public Scanresult getReport(@PathVariable String scanId){
        Scanresult result = scanService.getScanResult(scanId);
        if(result == null){
        throw new RuntimeException("Scan not Found");
        }
        return result;
    }
    public String getMethodName(@RequestParam String param) {
        return new String();
    }
    @GetMapping("/scan/{scanId}/report/pdf")
    public ResponseEntity<byte[]> getPdfReport(@PathVariable String scanId) {

        Scanresult result = scanService.getScanResult(scanId);

        if (result == null) {
        throw new RuntimeException("Scan not found");
        }

        byte[] pdf = PdfReportGenerator.generate(result);

        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", "attachment; filename=report.pdf")
            .body(pdf);
}
    

    
}
