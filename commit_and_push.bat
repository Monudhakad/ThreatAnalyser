@echo off
REM Git automation script for ThreatAnalyzer enhancements

setlocal enabledelayedexpansion
cd /d "D:\MinorProject\ThreatAnalyser.worktrees\copilot-worktree-2026-05-28T12-33-25"

echo ============================================
echo ThreatAnalyzer - Git Automation Script
echo ============================================
echo.

REM Check current branch
echo [INFO] Current git status:
git status --short
echo.

REM Display branch info
echo [INFO] Current branch:
git branch --show-current
echo.

REM Check for uncommitted changes
for /f "tokens=*" %%A in ('git status --short ^| find /c /v ""') do set COUNT=%%A
if %COUNT% gtr 0 (
    echo [INFO] Found %COUNT% uncommitted changes
    echo.
    echo [INFO] Staging all changes...
    git add .
    
    if "%1"=="" (
        echo.
        echo [ERROR] No commit message provided!
        echo Usage: commit_and_push.bat "Commit message"
        exit /b 1
    )
    
    echo [INFO] Committing with message: %1
    git commit -m "%1" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
    
    if errorlevel 1 (
        echo [ERROR] Commit failed!
        exit /b 1
    )
    
    echo [INFO] Pushing to GitHub...
    git push origin HEAD
    
    if errorlevel 1 (
        echo [ERROR] Push failed!
        exit /b 1
    )
    
    echo [SUCCESS] Changes committed and pushed to GitHub
) else (
    echo [INFO] No uncommitted changes found
)

echo.
echo ============================================
echo Done!
echo ============================================
