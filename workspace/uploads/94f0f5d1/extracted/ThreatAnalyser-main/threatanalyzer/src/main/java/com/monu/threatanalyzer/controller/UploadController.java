package com.monu.threatanalyzer.controller;
import com.monu.threatanalyzer.model.Scanresult;
import com.monu.threatanalyzer.service.ScanService;
import com.monu.threatanalyzer.service.StorageServices;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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
        Scanresult r = scanService.startScan(scanId);
        System.out.println("Total findings: "+ r.getFindings().size());

        Map<String,String> response=new HashMap<>();
        response.put("scanId", scanId);
        response.put("status", "UPLOADED");

        return response;
    }
    //to implement zip file download via URL API
    @PostMapping("/upload/github")
    public Map<String, String> uploadwithurl(@RequestParam String repoUrl){
        String scanId = storageServices.generateScanId();
        storageServices.urlDownload(repoUrl, scanId);
        Scanresult r = scanService.startScan(scanId);
        System.out.println("Total findings: "+ r.getFindings().size());
        Map<String, String> response = new HashMap<>();
        response.put("scanId", scanId);
        response.put("status", "UPLOADED");
        return response;
    }
}
