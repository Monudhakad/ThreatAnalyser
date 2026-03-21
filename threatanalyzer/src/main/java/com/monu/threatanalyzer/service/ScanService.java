package com.monu.threatanalyzer.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

@Service
public class ScanService {
    private final Path uploadRoot = Paths.get("workspace/uploads");
    public void startScan(String scanId){
        try {
            Path projectRoot = uploadRoot.resolve(scanId).resolve("extracted");
            System.out.println("scanning directory: "+ projectRoot);
            Files.walk(projectRoot).filter(Files::isRegularFile).forEach(file ->{
                try {
                    String content = Files.readString(file);
                    if(content.toLowerCase().contains("password")|| content.toLowerCase().contains("secret")|| content.toLowerCase().contains("api_key")|| content.toLowerCase().contains("token")){
                        System.out.println("Possible secret found in : "+ file);
                    }
                } catch (Exception ignored) {
                }
                System.out.println("Found file: "+ file);
                //to do block
        });
        } catch (Exception e) {
            throw new RuntimeException("Scan failed", e);
        }
    }
}
