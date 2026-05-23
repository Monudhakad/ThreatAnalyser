package com.monu.threatanalyzer.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.monu.threatanalyzer.model.ProjectMetadata;
import com.monu.threatanalyzer.model.Scanresult;
import com.monu.threatanalyzer.model.ThreatFinding;
import com.monu.threatanalyzer.model.TechnologyFinding;


@Service
public class ScanService {
    private final Path uploadRoot = Paths.get("workspace/uploads");
    public Scanresult startScan(
            String scanId,
            String sourceType,
            String repositoryUrl
    ){
        Scanresult result = new Scanresult(scanId);

        try {

            Path projectRoot =
                    uploadRoot.resolve(scanId)
                              .resolve("extracted");

            if (!Files.exists(projectRoot)) {
                throw new RuntimeException("Project directory does not exist: " + projectRoot);
            }

            System.out.println("Scanning started: " + scanId);

            // ===== COUNT FILES FIRST =====
            long totalFiles;

            try (var countStream = Files.walk(projectRoot)) {

                totalFiles = countStream
                        .filter(Files::isRegularFile)
                        .filter(path -> !path.toString().contains("node_modules"))
                        .filter(path -> !path.toString().contains("target"))
                        .filter(path -> !path.toString().contains(".git"))
                        .filter(path -> !path.toString().endsWith(".class"))
                        .filter(path -> !path.toString().endsWith(".jar"))
                        .count();
            }

            String projectName = "Unknown";
            try (var dirStream = Files.list(projectRoot)) {
                projectName = dirStream
                        .findFirst()
                        .map(path -> path.getFileName().toString())
                        .orElse("Unknown");
            }

            // ===== CREATE METADATA =====
            ProjectMetadata metadata =
                    new ProjectMetadata(
                            scanId,
                            projectName,
                            sourceType,
                            repositoryUrl,
                            (int) totalFiles
                    );

            result.setMetadata(metadata);

            // ===== NOW START SCANNING =====
            try (var stream = Files.walk(projectRoot)) {

                stream
                        .filter(Files::isRegularFile)
                        .filter(path -> !path.toString().contains("node_modules"))
                        .filter(path -> !path.toString().contains("target"))
                        .filter(path -> !path.toString().contains(".git"))
                        .filter(path -> !path.toString().endsWith(".class"))
                        .filter(path -> !path.toString().endsWith(".jar"))
                        .forEach(file -> {

                    if (file.getFileName()
                        .toString()
                        .equalsIgnoreCase("pom.xml")) {

                    if (!result.hasTechnology("Maven")) {
                        result.addTechnology(
                            new TechnologyFinding(
                                "Maven",
                                file.toString()
                            )
                        );
                    }

                    checkpomDependencies(file, result);
                    }

                    if (file.toString().endsWith(".java")
                        && !result.hasTechnology("Java")) {
                    result.addTechnology(
                        new TechnologyFinding(
                            "Java",
                            file.toString()
                        )
                    );
                    }

                    if (file.getFileName()
                        .toString()
                        .equalsIgnoreCase("package.json")
                        && !result.hasTechnology("NodeJS")) {
                    result.addTechnology(
                        new TechnologyFinding(
                            "NodeJS",
                            file.toString()
                        )
                    );
                    }

                try {

                    String content = Files.readString(file);
                    String lower = content.toLowerCase();

                    // COMMAND RISK
                    if (lower.contains("runtime.getruntime().exec(")
                            || lower.contains("processbuilder")) {

                        result.addFindings(
                                new ThreatFinding(
                                        "Command_Execution_Risk",
                                        file.toString(),
                                        "HIGH"
                                )
                        );
                    }

                    // SECRET LEAK
                    if (lower.contains("password")
                            || lower.contains("secret")
                            || lower.contains("api_key")
                            || lower.contains("token")) {

                        result.addFindings(
                                new ThreatFinding(
                                        "Secret Leak",
                                        file.toString(),
                                        "MEDIUM"
                                )
                        );
                    }
                        if (lower.contains("spring-boot")
                            && !result.hasTechnology("Spring Boot")) {
                        result.addTechnology(
                            new TechnologyFinding(
                                "Spring Boot",
                                file.toString()
                            )
                        );
                        }
                        if (lower.contains("react")
                            && !result.hasTechnology("React")) {
                        result.addTechnology(
                            new TechnologyFinding(
                                "React",
                                file.toString()
                            )
                        );
                        }
                    // SQL INJECTION RISK
                    if ((lower.contains("select ")
                            || lower.contains("insert ")
                            || lower.contains("update ")
                            || lower.contains("delete "))
                            && lower.contains("+")) {
                            
                        result.addFindings(
                                new ThreatFinding(
                                        "SQL_INJECTION_RISK",
                                        file.toString(),
                                        "HIGH"
                                )
                        );
                    }

                } catch (Exception ignored) {
                }
            });
        }

        System.out.println("Scanning completed: " + scanId);

    } catch (Exception e) {
        throw new RuntimeException("Scan failed", e);
    }
        int score = 0;
        for (ThreatFinding f : result.getFindings()) {
            switch (f.getSeverity()) {
                case "CRITICAL": score += 40; break;
                case "HIGH": score += 30; break;
                case "MEDIUM": score += 20; break;
                case "LOW": score += 10; break;
            }
        }
        result.setThreatScore(score);
        if (score>=70) {
            result.setRiskLevel("HIGH");
        }else if (score>=40) {
            result.setRiskLevel("MEDIUM");
        }else{result.setRiskLevel("LOW");}

        System.out.println("Findings count: " + result.getFindings().size());

        scanStore.put(scanId, result);
        return result;
    }
    private void checkpomDependencies(Path pomPath, Scanresult result){
        try {
            String content = Files.readString(pomPath);

            checkDependency(content, "log4j", "2.14", result, pomPath);
            checkDependency(content, "spring-core", "5.2", result, pomPath);
            checkDependency(content, "jackson", "2.9", result, pomPath);
            checkDependency(content, "commons-collections", "3.2", result, pomPath);
        } catch (Exception ignored) {
        }
    }
    private void checkDependency(String content, String lib, String vulnVersion, Scanresult result, Path pomPath){
        if (content.contains(lib)&& content.contains(vulnVersion)) {
            result.addFindings(new ThreatFinding("Vulnerable_dependency", pomPath.toString(), "CRITICAL"));
        }
    }
    public Scanresult getScanResult(String scanId){
        return scanStore.get(scanId);
    }
    private Map<String, Scanresult> scanStore = new ConcurrentHashMap<>();
    
}
