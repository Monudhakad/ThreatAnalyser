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
                if(file.getFileName().toString().equalsIgnoreCase("pom.xml")){
                    checkpomDependencies(file);
                }
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
    private void checkpomDependencies(Path pomPath){
        try {
            String content = Files.readString(pomPath);
            System.out.println("Analyzing dependencies in: "+ pomPath);

            checkDependency(content, "log4j", "2.14");
            checkDependency(content, "spring-core", "5.2");
            checkDependency(content, "jackson-databind", "2.9");
        } catch (Exception e) {
            System.out.println("Failed to analyze now");
        }
    }
    private void checkDependency(String content, String lib, String vulnVersion){
        if (content.contains(lib)&& content.contains(vulnVersion)) {
            System.out.println("Vulnerable dependency detected: "+ lib + " " + vulnVersion);
        }
    }
}
