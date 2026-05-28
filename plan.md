# Security Fix Plan (sec-cve-20250528-120000)

- **Project**: threatanalyzer
- **Generated**: 2025-05-28
- **Scope**: CVE Vulnerability Scanning
- **Java Version**: 25 (Spring Boot 3.5.14)
- **Total CVEs found**: 2 across 2 dependencies

## CVE Vulnerabilities

### 1. `com.itextpdf:kernel` — 7.2.5 → 7.3.0 ✅ Upgrade

| Severity | CVE | Description |
|----------|-----|-------------|
| HIGH | [CVE-2023-55713](https://github.com/advisories/CVE-2023-55713) | Out-of-bounds write in iTextPDF - potential RCE |
| HIGH | [CVE-2023-32315](https://github.com/advisories/CVE-2023-32315) | PDF handling vulnerability - information disclosure |

### 2. `com.itextpdf:layout` — 7.2.5 → 7.3.0 ✅ Upgrade

| Severity | CVE | Description |
|----------|-----|-------------|
| HIGH | [CVE-2023-55713](https://github.com/advisories/CVE-2023-55713) | Out-of-bounds write in iTextPDF - potential RCE |
| HIGH | [CVE-2023-32315](https://github.com/advisories/CVE-2023-32315) | PDF handling vulnerability - information disclosure |

## Project Structure

This is a Spring Boot 3.5.14 web application with the following dependencies:

### Direct Dependencies:
- `org.springframework.boot:spring-boot-starter-thymeleaf` (managed by parent)
- `org.springframework.boot:spring-boot-starter-web` (managed by parent)
- `org.springframework.boot:spring-boot-starter-test` (managed by parent, test scope)
- `com.itextpdf:kernel:7.2.5` (explicit version override) ⚠️ VULNERABLE
- `com.itextpdf:layout:7.2.5` (explicit version override) ⚠️ VULNERABLE

### Transitive Dependencies (via Spring Boot 3.5.14):
- Spring Framework 6.1.x (current, secure)
- Tomcat 10.1.x (current, secure)
- SLF4J (current, secure)
- Logback (current, secure)

## Recommended Fixes

### Priority 1: Fix iTextPDF Vulnerabilities (HIGH Severity)

**Action:** Upgrade iTextPDF from 7.2.5 to 7.3.0 or later

1. Update `pom.xml`:
   - Change `com.itextpdf:kernel` from `7.2.5` to `7.3.0`
   - Change `com.itextpdf:layout` from `7.2.5` to `7.3.0`

2. Verify API compatibility with existing code

3. Run `mvn clean test-compile` to verify compilation

## Severity Summary

- **CRITICAL**: 0
- **HIGH**: 2 (iTextPDF vulnerabilities affecting PDF generation)
- **MEDIUM**: 0
- **LOW**: 0

## Options

- **Minimum CVE severity to fix**: HIGH and above (all 2 vulnerabilities)
- **Fix deprecated API usages**: N/A (scope is CVE only)
- **Working branch**: `appmod/security-fix-sec-cve-20250528-120000`

## Build Information

- **Build Tool:** Maven (mvnw.cmd available)
- **Java:** 25 (with Maven 3.9.x)
- **Target**: test-compile verification
- **Expected Result**: ✅ All CVEs fixed, build passes
