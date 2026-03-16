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

    // to accept file and scan id for file and then return the file path with creating separate workspace for every file.
    public String saveZip(byte[] data, String scanId){
        try{
            Path scanFolder = scanWorkspace(scanId);
            Path filepath = scanFolder.resolve(scanId + ".zip");
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
    //to download file with given URl
    public String urlDownload(String repoUrl, String scanId){
        String[] branches = {"main", "master"};
        for (String branch : branches) {// to try both main and master for branches.
            try {
                String zipUrl = repoUrl + "/archive/refs/heads/" + branch + ".zip";
                java.net.URL url = new java.net.URL(zipUrl);
                java.io.InputStream in = url.openStream();
                Path scanFolder = scanWorkspace(scanId);
                Path filePath = scanFolder.resolve(scanId + ".zip");
                Files.copy(in, filePath);
                in.close();
                System.out.println("Downloaded repo Zip: " + filePath.toAbsolutePath());
                extractZip(scanId);
                return filePath.toString();
            } catch (Exception e) {
                System.out.println("branch failed:" + branch);
            }
        }
        throw new RuntimeException("Failed to download zip");//user has named his branch differently(need to create another feature for that too :)
        
    }
    //to create separate folder for every zip.
    public Path scanWorkspace(String scanId){
        try {
            Path scanFolder = uploadPath.resolve(scanId);
            Files.createDirectories(scanFolder);
            return scanFolder;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Workspace");
        }
    }

    //to add an unzipping function to unzip the file given.
    public void extractZip(String scanId){
        try {
            Path scanFolder = uploadPath.resolve(scanId);
            Path extractPath = scanFolder.resolve("extracted");
            Files.createDirectories(extractPath);
            Path zipFilePath = scanFolder.resolve(scanId + ".zip");
            java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new java.io.FileInputStream(zipFilePath.toFile()));
            java.util.zip.ZipEntry entry;
            while ((entry = zis.getNextEntry())!=null) {
                Path filepPath = extractPath.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filepPath);
                }else{
                    Files.createDirectories(filepPath.getParent());
                    Files.copy(zis, filepPath);
                }
            }
            zis.close();
            System.out.println("Project extracted for scanId:" + scanId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract zip file", e);
        }
    }
    
}
