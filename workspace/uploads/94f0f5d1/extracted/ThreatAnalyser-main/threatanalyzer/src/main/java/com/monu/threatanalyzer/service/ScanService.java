package com.monu.threatanalyzer.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import com.monu.threatanalyzer.model.Scanresult;
import com.monu.threatanalyzer.model.ThreatFinding;

import ch.qos.logback.core.net.SyslogOutputStream;

@Service
public class ScanService {
    private final Path uploadRoot = Paths.get("workspace/uploads");
    public Scanresult startScan(String scanId){
        Scanresult result = new Scanresult(scanId);
        try {
            Path projectRoot = uploadRoot.resolve(scanId).resolve("extracted");
            System.out.println("scanning directory: "+ projectRoot);
            Files.walk(projectRoot).filter(Files::isRegularFile).forEach(file ->{
                if(file.getFileName().toString().equalsIgnoreCase("pom.xml")){
                    checkpomDependencies(file, result);              
                }
                
                try {
                    String content = Files.readString(file);
                    String lower = content.toLowerCase();
                    if(lower.contains("runtime.getruntime().exec(") || lower.contains("processbuilder")){//command injection risk detection 
                        result.addFindings(new ThreatFinding("Command_Execution_Risk", file.toString(), "High"));
                    }
                    if(content.toLowerCase().contains("password")|| content.toLowerCase().contains("secret")|| content.toLowerCase().contains("api_key")|| content.toLowerCase().contains("token")){
                        result.addFindings(new ThreatFinding("Secret leak", file.toString(), "Medium"));
                    }
                } catch (Exception ignored) {
                }
                System.out.println("Found file: "+ file);
        });
        } catch (Exception e) {
            throw new RuntimeException("Scan failed", e);
        }
        return result;
    }
    private void checkpomDependencies(Path pomPath, Scanresult result){
        try {
            String content = Files.readString(pomPath);
            System.out.println("Analyzing dependencies in: "+ pomPath);

            checkDependency(content, "log4j", "2.14", result, pomPath);
            checkDependency(content, "spring-core", "5.2", result, pomPath);
            checkDependency(content, "jackson-databind", "2.9", result, pomPath);
        } catch (Exception e) {
            System.out.println("Failed to analyze now");
        }
    }
    private void checkDependency(String content, String lib, String vulnVersion, Scanresult result, Path pomPath){
        if (content.contains(lib)&& content.contains(vulnVersion)) {
            result.addFindings(new ThreatFinding("Vulnerable_dependency", pomPath.toString(), "Critical"));
        }
    }
    
}
