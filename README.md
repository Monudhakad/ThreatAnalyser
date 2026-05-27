# ThreatAnalyzer

**An automated vulnerability detection and threat analysis platform for scanning project repositories and source code.**

[![Java 25](https://img.shields.io/badge/Java-25-ED8B00?style=flat-square)](https://www.java.com/)
[![Spring Boot 3.3.5](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=flat-square)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=flat-square)](https://maven.apache.org/)

## 🚀 Quick Start

```bash
# Clone repository
git clone https://github.com/Monudhakad/ThreatAnalyser.git
cd ThreatAnalyser/threatanalyzer

# Build
./mvnw clean install

# Run
./mvnw spring-boot:run

# Access
Open http://localhost:8080
```

## ✨ Key Features

- **Multi-Source Scanning**: ZIP files and GitHub repositories
- **Threat Detection**: SQL injection, command execution, secret leaks
- **Vulnerability Detection**: Identifies vulnerable dependencies
- **Technology Fingerprinting**: Detects Java, Spring Boot, React, Maven, NodeJS
- **Smart Filtering**: Excludes build artifacts, focuses on source code
- **Comprehensive Reports**: JSON API responses and PDF exports
- **Risk Scoring**: Automatic threat level calculation
- **Remediation Guidance**: Actionable fix suggestions for each finding

## 📊 Supported Threats

| Threat Type | Pattern | Severity | Remediation |
|---|---|---|---|
| **SQL Injection** | String concatenation in SQL queries | HIGH | Use PreparedStatement |
| **Command Execution** | Runtime.exec() / ProcessBuilder | HIGH | Avoid dynamic execution |
| **Secret Leaks** | Hardcoded credentials in code | MEDIUM | Use environment variables |
| **Vulnerable Dependencies** | Log4j 2.14, spring-core 5.2, etc. | CRITICAL | Update to safe versions |

## 📡 API Quick Reference

### Scan GitHub Repository
```bash
curl -X POST \
  -d "repoUrl=https://github.com/user/repo" \
  http://localhost:8080/api/upload/github
```

### Upload ZIP File
```bash
curl -X POST \
  -F "file=@project.zip" \
  http://localhost:8080/api/upload
```

### Get Results (JSON)
```bash
curl http://localhost:8080/api/scan/{scanId}/report | jq
```

### Download PDF Report
```bash
curl http://localhost:8080/api/scan/{scanId}/report/pdf -o report.pdf
```

## 📚 Documentation

- **[Full README](threatanalyzer/README_FULL.md)** - Complete API documentation, setup, configuration
- **[Architecture](ARCHITECTURE.md)** - Detailed system design, data flow, component interaction
- **[API Examples](threatanalyzer/README_FULL.md#-api-endpoints)** - Request/response samples
- **[Deployment Guide](threatanalyzer/README_FULL.md#-deployment)** - Production setup

## 🏗️ Architecture Overview

```
REST API → Controllers → Services → Analysis Engine → Reports
                                         ↓
                    [File System Walk, Pattern Matching, Risk Scoring]
                                         ↓
                    [JSON Response, PDF Export, Cached Results]
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed diagrams and component interactions.

## 📁 Project Structure

```
threatanalyzer/
├── src/main/
│   ├── java/com/monu/threatanalyzer/
│   │   ├── controller/     → REST API endpoints
│   │   ├── service/        → Business logic (scanning, storage)
│   │   ├── model/          → Data models (Scanresult, ThreatFinding, etc.)
│   │   └── util/           → PDF report generation
│   └── resources/
│       ├── static/         → CSS, JavaScript UI
│       ├── templates/      → HTML pages
│       └── vulndb/         → Vulnerability database
├── pom.xml                 → Maven dependencies
└── mvnw / mvnw.cmd        → Maven Wrapper
```
<img width="958" height="580" alt="_- visual selection" src="https://github.com/user-attachments/assets/512d2d02-a2e4-4967-9bab-3535d3a9f247" />


## 🔧 Tech Stack

- **Framework**: Spring Boot 3.3.5
- **Language**: Java 25
- **Build**: Maven 3.9+
- **PDF**: iTextPDF 7.2.5
- **Runtime**: Embedded Tomcat

## 🧪 Testing

```bash
# Run tests
./mvnw clean test

# Run with coverage
./mvnw clean test jacoco:report
```

## 📈 Response Example

```json
{
  "scanId": "a1b2c3d4",
  "threatScore": 65,
  "riskLevel": "MEDIUM",
  "metadata": {
    "projectName": "spring-boot-app",
    "sourceType": "GITHUB",
    "totalFiles": 42,
    "scanTime": "2026-05-23T15:30:00"
  },
  "findings": [
    {
      "type": "SQL_INJECTION_RISK",
      "severity": "HIGH",
      "file": "src/main/java/UserDao.java",
      "remediation": "Use PreparedStatement to prevent SQL Injection"
    }
  ],
  "technologies": [
    {"technology": "Java"},
    {"technology": "Spring Boot"},
    {"technology": "Maven"}
  ]
}
```

## 🔐 Security Considerations

- ✅ URL validation (GitHub URLs only)
- ✅ Path traversal protection (workspace isolation)
- ✅ File upload limits (500MB default)
- ⚠️ No authentication (add Spring Security for production)
- ⚠️ In-memory storage (use database for production)

## 🐛 Troubleshooting

| Issue | Solution |
|---|---|
| "Only GitHub URLs are supported" | Use format: `https://github.com/user/repo` |
| "Repository URL cannot be empty" | Provide repoUrl parameter |
| "Project directory does not exist" | Ensure ZIP contains valid project |
| "PDF generation failed" | Verify iTextPDF dependencies installed |

See [Full README](threatanalyzer/README_FULL.md#-troubleshooting) for more help.

## 🚦 Deployment

### Local Development
```bash
./mvnw spring-boot:run
```

### Docker
```bash
./mvnw clean package
docker build -t threatanalyzer .
docker run -p 8080:8080 threatanalyzer
```

### Production (Recommended)
- Add Spring Security for authentication
- Replace in-memory cache with PostgreSQL database
- Enable HTTPS/SSL
- Set up reverse proxy (Nginx)
- Configure rate limiting
- Enable monitoring and logging

## 📊 Risk Scoring Algorithm

```
Score Calculation:
  • CRITICAL finding: +40 points
  • HIGH finding:     +30 points
  • MEDIUM finding:   +20 points
  • LOW finding:      +10 points

Risk Level:
  • Score ≥ 70:  HIGH risk
  • Score 40-69: MEDIUM risk
  • Score < 40:  LOW risk
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/my-feature`
3. Commit changes: `git commit -m 'Add my feature'`
4. Push: `git push origin feature/my-feature`
5. Open Pull Request

## 📜 License

MIT License - See [LICENSE](LICENSE) file

## 👨‍💻 Author

**Monu Dhakad**

## 📞 Support & Issues

- 🐛 [Report Bug](https://github.com/Monudhakad/ThreatAnalyser/issues)
- ✨ [Request Feature](https://github.com/Monudhakad/ThreatAnalyser/issues)
- 📖 [Full Documentation](threatanalyzer/README_FULL.md)
- 🏗️ [Architecture Details](ARCHITECTURE.md)

## 🎯 Roadmap

- [ ] Database persistence (PostgreSQL/MongoDB)
- [ ] User authentication
- [ ] Scheduled scanning
- [ ] CI/CD integration
- [ ] Advanced filtering rules
- [ ] Analytics dashboard
- [ ] SBOM export
- [ ] NVD/CVE API integration

## 📈 Performance

- Typical scan: **2-10 seconds** (project size dependent)
- PDF generation: **~500ms**
- Memory usage: **200-500MB** (cached results)
- Supported project size: **Up to 1GB** (ZIP)

---

**Version**: 1.0.0 | **Status**: Active Development | **Last Updated**: May 2026

[Full Documentation →](threatanalyzer/README_FULL.md) | [Architecture →](ARCHITECTURE.md)
