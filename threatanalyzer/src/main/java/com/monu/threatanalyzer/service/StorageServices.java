package com.monu.threatanalyzer.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service 

public class StorageServices {
    private final Path uploadPath = Paths.get("workspace/uploads");

    @PostConstruct
    //to create a directory to save the file.
    public void initStorage(){

        try{
            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
                System.out.println("Uploads folder created");
            }
        } catch(Exception e){
            throw new RuntimeException("could not create upload folder");
        }
    }

    // to accept file and scan id for file and then return the file path
    public String saveZip(byte[] data, String scanId){
        try{
            Path filepath = uploadPath.resolve(scanId + ".zip");
            Files.write(filepath, data);
            System.out.println("ZIP saved at: "+ filepath.toAbsolutePath());
            return filepath.toString();
        } catch(Exception e){
            throw new RuntimeException("Failed to save the zip file");
        }
    }
    // to generate Scan id which will be sticked with the filename.
    public String generateScanId(){
        return java.util.UUID.randomUUID()
                .toString()
                .substring(0, 8);
    }
    
}
