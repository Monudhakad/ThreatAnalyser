package com.monu.threatanalyzer.controller;
import com.monu.threatanalyzer.service.StorageServices;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api")

public class UploadController {
    @Autowired
    private StorageServices storageServices;
    @PostMapping("/upload")
    public Map<String, String> uploadZip(@RequestParam("file") MultipartFile file) {
        String scanId = storageServices.generateScanId();
        try {
            storageServices.saveZip(file.getBytes(), scanId);
        } catch (Exception e) {
            throw new RuntimeException("upload failed");
        }

        Map<String,String> response=new HashMap<>();
        response.put("scanId", scanId);
        response.put("status", "UPLOADED");

        return response;
    }
}
