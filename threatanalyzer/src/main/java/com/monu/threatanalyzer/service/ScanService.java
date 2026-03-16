package com.monu.threatanalyzer.service;

import org.springframework.stereotype.Service;

@Service
public class ScanService {
    public void startScan(String scanId){

        System.out.println("Scanning started for: "+scanId);
    }
}
