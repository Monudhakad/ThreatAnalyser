@echo off
REM Search for naming inconsistencies in project

echo ===== Searching for threatanalyser (British) =====
findstr /r /s "threatanalyser" *.xml *.md *.java *.html *.yml 2>nul || echo (no matches)

echo.
echo ===== Searching for threatanalyzer (American) =====
findstr /r /s "threatanalyzer" *.xml *.md *.java *.html *.yml 2>nul || echo (no matches)

echo.
echo ===== Checking pom.xml content =====
type threatanalyzer\pom.xml 2>nul || type pom.xml 2>nul || echo (pom.xml not found)
