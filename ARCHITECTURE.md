# ThreatAnalyzer - System Architecture

## High-Level Architecture

```
                          ┌─────────────────────────────────────────┐
                          │   CLIENT LAYER (Web/API Consumers)      │
                          │  Browser│ cURL │ Mobile │ CI/CD Tools   │
                          └──────────────┬──────────────────────────┘
                                         │
                          ┌──────────────▼──────────────┐
                          │    HTTP/REST API Layer      │
                          │  Spring Web MVC + Tomcat    │
                          └──────────────┬──────────────┘
                                         │
              ┌──────────────────────────┼──────────────────────────┐
              │                          │                          │
      ┌───────▼────────┐      ┌──────────▼──────────┐      ┌───────▼────────┐
      │ UploadController│      │ UploadController   │      │ UploadController│
      │  (ZIP Upload)  │      │  (GitHub Scan)     │      │  (Report Fetch) │
      └───────┬────────┘      └──────────┬─────────┘      └───────┬────────┘
              │                          │                         │
              └──────────────────────────┼─────────────────────────┘
                                         │
                          ┌──────────────▼──────────────┐
                          │   SERVICE LAYER             │
                          │  Orchestration & Caching    │
                          └──────────────┬──────────────┘
                                         │
              ┌──────────────────────────┼──────────────────────────┐
              │                          │                          │
      ┌───────▼─────────────┐   ┌────────▼────────────┐   ┌────────▼────────┐
      │  StorageServices    │   │   ScanService       │   │  PdfReportGen   │
      │  ├─ ZIP extraction  │   │   ├─ File walk      │   │  (Report PDF)   │
      │  ├─ GitHub cloning  │   │   ├─ Content scan   │   └─────────────────┘
      │  └─ File mgmt       │   │   ├─ Tech detect    │
      └─────────────────────┘   │   ├─ Vuln check     │
                                │   └─ Risk scoring   │
                                └────────┬────────────┘
                                         │
                          ┌──────────────▼──────────────┐
                          │  ANALYSIS ENGINE            │
                          │  Pattern Matching & Rules   │
                          ├──────────────────────────────┤
                          │ 1. File System Walk          │
                          │ 2. Content Analysis          │
                          │ 3. Dependency Scanning       │
                          │ 4. Technology Fingerprint    │
                          │ 5. Threat Detection          │
                          │ 6. Risk Calculation          │
                          └──────────────┬───────────────┘
                                         │
              ┌──────────────────────────┼──────────────────────────┐
              │                          │                          │
      ┌───────▼──────────────┐  ┌────────▼─────────┐   ┌──────────▼──────┐
      │  THREAT PATTERNS     │  │  TECH DETECTOR   │   │  DEP ANALYZER    │
      │  ├─SQL Injection     │  │  ├─ Java         │   │  ├─Maven pom.xml │
      │  ├─Cmd Execution     │  │  ├─ Spring Boot  │   │  ├─Package.json  │
      │  ├─Secret Leaks      │  │  ├─ NodeJS       │   │  └─ Vulns DB     │
      │  └─Vulnerable Deps   │  │  ├─ React        │   └──────────────────┘
      │                      │  │  └─ Maven        │
      └──────────────────────┘  └──────────────────┘
                                         │
                          ┌──────────────▼──────────────┐
                          │  DATA MODELS & STORAGE      │
                          ├──────────────────────────────┤
                          │  • Scanresult               │
                          │  • ThreatFinding            │
                          │  • TechnologyFinding        │
                          │  • ProjectMetadata          │
                          │  • In-Memory Cache (Map)    │
                          └──────────────┬───────────────┘
                                         │
                          ┌──────────────▼──────────────┐
                          │   OUTPUT & REPORTING        │
                          ├──────────────────────────────┤
                          │  • JSON Response            │
                          │  • PDF Report (iTextPDF)    │
                          │  • HTML Template            │
                          └──────────────────────────────┘
```

## Data Flow: From Upload to Report

```
                              USER UPLOADS ZIP
                                    │
                    ┌───────────────▼────────────────┐
                    │  UploadController.uploadZip()  │
                    │  Generate ScanId: a1b2c3d4     │
                    └───────────────┬────────────────┘
                                    │
                    ┌───────────────▼────────────────┐
                    │ StorageServices.saveZip()      │
                    │ • Save ZIP to workspace/       │
                    │ • Extract contents             │
                    └───────────────┬────────────────┘
                                    │
                    ┌───────────────▼────────────────┐
                    │ ScanService.startScan()        │
                    │ Params: scanId, ZIP_UPLOAD,    │
                    │         LOCAL_UPLOAD           │
                    └───────────────┬────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
    ┌───▼──────────┐    ┌──────────▼──────────┐    ┌──────────▼──┐
    │FILE WALK     │    │CONTENT ANALYSIS     │    │PROM DEP      │
    │• .java files │    │• SQL patterns       │    │• pom.xml     │
    │• .json files │    │• Secret keywords    │    │• Vuln check  │
    │• .xml files  │    │• Cmd execution      │    │• Log4j, etc  │
    └───┬──────────┘    └──────────┬──────────┘    └──────────┬──┘
        │                          │                           │
    ┌───▼──────────────┐   ┌───────▼───────────┐   ┌──────────▼──┐
    │TECH DETECTION    │   │THREAT DETECTION   │   │RISK SCORING │
    │Store TechFinding │   │Store ThreatFinding│   │Calculate    │
    │+ duplicates      │   │+ Remediation      │   │Score/Level  │
    └───┬──────────────┘   └───────┬───────────┘   └──────────┬──┘
        │                          │                          │
        └──────────────────────────┼──────────────────────────┘
                                   │
                    ┌──────────────▼───────────────┐
                    │  Scanresult Object Ready     │
                    │  ├─ findings[]               │
                    │  ├─ technologies[]           │
                    │  ├─ threatScore              │
                    │  ├─ riskLevel                │
                    │  └─ metadata                 │
                    └──────────────┬────────────────┘
                                   │
              ┌────────────────────┼─────────────────────┐
              │                    │                     │
        ┌─────▼──────┐      ┌──────▼────────┐     ┌────▼──────┐
        │CACHE STORE │      │JSON RESPONSE  │     │PDF REPORT │
        │In memory   │      │/api/scan/..   │     │iTextPDF   │
        │Map<id>     │      │Full details    │     │download   │
        └────────────┘      └────────────────┘     └───────────┘
```

## Component Interaction Diagram

```
┌────────────────────────────────────────────────────────────────┐
│                       REST CONTROLLERS                          │
│                                                                │
│  UploadController                                              │
│  ├─ POST   /api/upload           ← File upload                │
│  ├─ POST   /api/upload/github    ← GitHub URL                │
│  ├─ GET    /api/scan/{id}/report ← Get JSON result           │
│  └─ GET    /api/scan/{id}/report/pdf ← Get PDF               │
└────────────┬─────────────────────────────────────────────────┘
             │
             │ delegates to
             ▼
┌────────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                               │
│                                                                │
│  StorageServices                                               │
│  ├─ saveZip(byte[], scanId)                                   │
│  ├─ urlDownload(repoUrl, scanId)                              │
│  ├─ extractZip(scanId)                                        │
│  ├─ generateScanId()                                          │
│  └─ scanWorkspace(scanId)                                     │
│                                                                │
│  ScanService                                                   │
│  ├─ startScan(scanId, sourceType, repoUrl) → Scanresult      │
│  ├─ getScanResult(scanId) → Scanresult                        │
│  ├─ checkpomDependencies(path, result)                        │
│  └─ checkDependency(content, lib, version, result, path)      │
│                                                                │
│  PdfReportGenerator (Utility)                                  │
│  └─ generate(Scanresult) → byte[]                             │
└────────────┬─────────────────────────────────────────────────┘
             │
             │ processes & stores
             ▼
┌────────────────────────────────────────────────────────────────┐
│                    ANALYSIS ENGINE                             │
│                    (Inside ScanService)                         │
│                                                                │
│  1. File System Walk                                           │
│     └─ Files.walk(projectRoot)                                │
│        └─ Filter: !node_modules, !target, !.git               │
│           └─ Filter: !.class, !.jar                           │
│                                                                │
│  2. Technology Detection                                       │
│     ├─ Maven → Check: pom.xml                                 │
│     ├─ Java → Check: *.java files                             │
│     ├─ Spring Boot → Check: "spring-boot" in content          │
│     ├─ React → Check: "react" in content                      │
│     └─ NodeJS → Check: package.json                           │
│     └─ Deduplication: hasTechnology() check                   │
│                                                                │
│  3. Content Analysis                                           │
│     ├─ SQL Injection: "select/insert/update/delete" + "+"     │
│     ├─ Command Exec: "Runtime.exec" OR "ProcessBuilder"       │
│     └─ Secret Leak: "password/secret/api_key/token"           │
│                                                                │
│  4. Dependency Scanning (pom.xml)                             │
│     ├─ log4j 2.14.x                                           │
│     ├─ spring-core 5.2.x                                      │
│     ├─ jackson < 2.15                                         │
│     └─ commons-collections < 3.2.2                            │
│                                                                │
│  5. Risk Scoring                                               │
│     ├─ CRITICAL = +40 points                                  │
│     ├─ HIGH = +30 points                                      │
│     ├─ MEDIUM = +20 points                                    │
│     └─ LOW = +10 points                                       │
│     Result: HIGH (≥70) | MEDIUM (40-69) | LOW (<40)          │
└────────────┬─────────────────────────────────────────────────┘
             │
             │ populates
             ▼
┌────────────────────────────────────────────────────────────────┐
│                    DATA MODELS                                 │
│                                                                │
│  Scanresult                                                    │
│  ├─ scanId: String                                             │
│  ├─ findings: List<ThreatFinding>                             │
│  ├─ technologies: List<TechnologyFinding>                     │
│  ├─ metadata: ProjectMetadata                                 │
│  ├─ threatScore: int                                          │
│  └─ riskLevel: String                                         │
│                                                                │
│  ThreatFinding                                                 │
│  ├─ type: String                                               │
│  ├─ file: String                                               │
│  ├─ severity: String (CRITICAL|HIGH|MEDIUM|LOW)              │
│  └─ remediation: String                                        │
│                                                                │
│  TechnologyFinding                                             │
│  ├─ technology: String                                         │
│  └─ detectedFrom: String                                       │
│                                                                │
│  ProjectMetadata                                               │
│  ├─ scanId: String                                             │
│  ├─ projectName: String                                        │
│  ├─ sourceType: String (GITHUB|ZIP_UPLOAD)                    │
│  ├─ repositoryUrl: String                                      │
│  ├─ totalFiles: int                                            │
│  └─ scanTime: LocalDateTime                                    │
└────────────┬─────────────────────────────────────────────────┘
             │
             │ cached in
             ▼
┌────────────────────────────────────────────────────────────────┐
│                    STORAGE LAYER                               │
│                                                                │
│  In-Memory Cache (ConcurrentHashMap)                           │
│  └─ Map<scanId, Scanresult>                                   │
│     └─ Thread-safe, fast lookup                               │
│                                                                │
│  File System (workspace/)                                      │
│  └─ workspace/uploads/{scanId}/                               │
│     ├─ {scanId}.zip (original)                                │
│     └─ extracted/ (extracted contents)                        │
└──────────────────────────────────────────────────────────────┘
```

## Request-Response Lifecycle

### GitHub Scan Flow
```
User Request
    ↓
POST /api/upload/github?repoUrl=https://...
    ↓
UploadController.uploadwithurl()
    ├─ Validate URL (GitHub only)
    ├─ Generate scanId
    ├─ Call StorageServices.urlDownload()
    │   ├─ Try main/master/develop branches
    │   └─ Download & extract ZIP
    └─ Call ScanService.startScan(scanId, "GITHUB", url)
        ├─ Count files (with filters)
        ├─ Extract projectName from first dir
        ├─ Create ProjectMetadata
        ├─ Walk files & analyze
        │   ├─ Detect technologies
        │   ├─ Scan content for threats
        │   └─ Check pom.xml for vulns
        ├─ Calculate threatScore
        ├─ Set riskLevel
        ├─ Cache result in map
        └─ Return Scanresult
    ↓
Return {"scanId": "a1b2c3d4", "status": "UPLOADED"}
    ↓
User fetches: GET /api/scan/a1b2c3d4/report
    ↓
Return JSON with findings, technologies, metadata
    ↓
User downloads: GET /api/scan/a1b2c3d4/report/pdf
    ↓
PdfReportGenerator.generate(result)
    ├─ Title & metadata
    ├─ List findings + remediation
    ├─ List technologies
    └─ Return PDF bytes
    ↓
Download: report.pdf
```

## Technology Stack Layers

```
┌─────────────────────────────────────────────┐
│  Frontend Layer                             │
│  ├─ HTML5 (index.html, result.html)        │
│  ├─ CSS3 (styles.css)                      │
│  └─ JavaScript (script.js)                 │
└─────────────────────────────────────────────┘
         ↑
         │ HTTP/REST
         │
┌─────────────────────────────────────────────┐
│  REST API Layer (Spring Web MVC)            │
│  ├─ DispatcherServlet                      │
│  ├─ Controllers                             │
│  ├─ HandlerMapping                          │
│  └─ ContentNegotiation (JSON, PDF)         │
└─────────────────────────────────────────────┘
         ↑
         │ Service calls
         │
┌─────────────────────────────────────────────┐
│  Application Layer                          │
│  ├─ Service classes                        │
│  ├─ Business logic                         │
│  └─ File I/O operations                    │
└─────────────────────────────────────────────┘
         ↑
         │ Data access
         │
┌─────────────────────────────────────────────┐
│  Data Layer                                 │
│  ├─ File system (ZIPs, extracted)          │
│  ├─ In-memory cache (ConcurrentHashMap)    │
│  └─ Model objects (Java POJOs)             │
└─────────────────────────────────────────────┘

External Integrations:
  • GitHub API (for cloning)
  • iTextPDF (for PDF generation)
  • Java NIO (for file operations)
```

## Deployment Architecture

```
┌─────────────────────────────────┐
│   Client Layer                  │
│   ├─ Web Browser               │
│   ├─ API Clients               │
│   └─ CI/CD Integration         │
└────────────┬────────────────────┘
             │ HTTPS
             ▼
┌─────────────────────────────────┐
│   Reverse Proxy / Load Balancer │
│   (Optional: Nginx, HAProxy)    │
└────────────┬────────────────────┘
             │ HTTP
             ▼
┌─────────────────────────────────┐
│   Application Container         │
│   Spring Boot + Embedded Tomcat │
│   ├─ Port: 8080               │
│   └─ Context: /               │
└────────────┬────────────────────┘
             │
    ┌────────┼────────┐
    │        │        │
    ▼        ▼        ▼
┌──────┐┌──────┐┌──────┐
│  VM  ││  VM  ││  VM  │  (For scaling)
│(App) ││(App) ││(App) │
└──────┘└──────┘└──────┘
    │        │        │
    └────────┼────────┘
             │
    ┌────────▼────────┐
    │  Shared Storage │  (For scan results)
    │  (Database)     │  Production: PostgreSQL
    └─────────────────┘
```

## Error Handling & Monitoring

```
┌────────────────────────────────────┐
│   Error Scenarios                  │
├────────────────────────────────────┤
│ • Invalid GitHub URL              │
│   └─ 400: "Only GitHub URLs..."   │
│ • Empty repository URL             │
│   └─ 400: "URL cannot be empty"   │
│ • ZIP extraction failure           │
│   └─ 500: "Failed to extract"     │
│ • PDF generation error             │
│   └─ 500: "PDF generation failed" │
│ • Scan result not found            │
│   └─ 404: "Scan not found"        │
└────────────────────────────────────┘

Monitoring Points:
  • Scan execution time
  • File count analyzed
  • Threats detected count
  • Report generation time
  • Cache hit rate
  • Memory usage
  • Disk usage (workspace)
```

---

**Architecture Version**: 1.0  
**Last Updated**: May 2026
