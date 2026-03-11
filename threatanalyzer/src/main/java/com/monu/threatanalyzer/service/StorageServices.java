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
}
