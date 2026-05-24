let currentPdfUrl = null;

function toggleUploadType() {
    const uploadType = document.querySelector('input[name="uploadType"]:checked')?.value || 'file';
    const fileSection = document.getElementById('fileSection');
    const githubSection = document.getElementById('githubSection');

    fileSection.classList.toggle('hidden', uploadType !== 'file');
    githubSection.classList.toggle('hidden', uploadType !== 'github');

    clearErrors();
}

function clearErrors() {
    document.getElementById('fileError').classList.add('hidden');
    document.getElementById('githubError').classList.add('hidden');
    document.getElementById('fileError').textContent = '';
    document.getElementById('githubError').textContent = '';
}

function validateFile() {
    const fileInput = document.getElementById('fileInput');
    const fileError = document.getElementById('fileError');

    if (!fileInput.files.length) {
        fileError.classList.add('hidden');
        return true;
    }

    const file = fileInput.files[0];
    if (!file.name.toLowerCase().endsWith('.zip')) {
        fileError.textContent = 'Please select a ZIP file.';
        fileError.classList.remove('hidden');
        return false;
    }

    if (file.size > 500 * 1024 * 1024) {
        fileError.textContent = 'File is larger than 500 MB.';
        fileError.classList.remove('hidden');
        return false;
    }

    fileError.classList.add('hidden');
    return true;
}

function validateGithub() {
    const githubInput = document.getElementById('githubInput');
    const githubError = document.getElementById('githubError');
    const value = githubInput.value.trim();

    if (!value) {
        githubError.classList.add('hidden');
        return true;
    }

    if (!value.includes('github.com')) {
        githubError.textContent = 'Enter a valid GitHub repository URL.';
        githubError.classList.remove('hidden');
        return false;
    }

    githubError.classList.add('hidden');
    return true;
}

function showLoading(message) {
    document.getElementById('uploadSection').classList.add('hidden');
    document.getElementById('loading').classList.remove('hidden');
    document.getElementById('resultsSection').classList.add('hidden');
    document.getElementById('error').classList.add('hidden');
    document.getElementById('loadingStatus').textContent = message;
}

function showResults(result) {
    const riskLevel = result.riskLevel || 'LOW';
    const riskCard = document.getElementById('riskCard');
    const findingsCount = document.getElementById('findingsCount');
    const techCount = document.getElementById('techCount');
    const threatScore = document.getElementById('threatScore');
    const resultsDetail = document.getElementById('resultsDetail');

    const showDependencies = document.getElementById('filterDependencies')?.checked ?? true;
    const showTechnologies = document.getElementById('filterTechnologies')?.checked ?? true;

    const findings = (result.findings || []).filter(finding => {
        if (!showDependencies && finding.type === 'Vulnerable_dependency') {
            return false;
        }

        return true;
    });

    const technologies = showTechnologies ? (result.technologies || []) : [];

    riskCard.className = 'summary-card';
    if (riskLevel === 'HIGH') {
        riskCard.classList.add('risk-high');
    } else if (riskLevel === 'MEDIUM') {
        riskCard.classList.add('risk-medium');
    } else {
        riskCard.classList.add('risk-low');
    }

    findingsCount.textContent = findings.length;
    techCount.textContent = technologies.length;
    threatScore.textContent = result.threatScore || 0;
    document.getElementById('riskLevel').textContent = riskLevel;

    const findingsHtml = findings.map(finding => `
        <li>
            <strong>${finding.type}</strong> — ${finding.severity}
            ${finding.summary ? `<div style="margin-top:0.3rem; color:#fef3c7;">${finding.summary}</div>` : ''}
            ${finding.description ? `<div style="margin-top:0.3rem; color:#cbd5e1;">${finding.description}</div>` : ''}
            ${finding.cveId ? `<div style="margin-top:0.3rem; color:#fda4af;">CVE: ${finding.cveId}</div>` : ''}
            ${finding.remediation ? `<div style="margin-top:0.3rem; color:#cbd5e1;">Fix: ${finding.remediation}</div>` : ''}
            ${finding.source ? `<div style="margin-top:0.3rem; color:#94a3b8;">Source: ${finding.source}</div>` : ''}
            <div style="margin-top:0.3rem; color:#94a3b8;">File: ${finding.file}</div>
        </li>
    `).join('');

    const technologiesHtml = technologies.map(tech => `
        <li>${tech.technology} <span style="color:#94a3b8;">(${tech.detectedFrom || 'Detected'})</span></li>
    `).join('');

    resultsDetail.innerHTML = `
        <h4>Detected Technologies</h4>
        ${technologiesHtml ? `<ul>${technologiesHtml}</ul>` : '<p>No technologies detected.</p>'}
        <h4>Security Findings</h4>
        ${findingsHtml ? `<ul>${findingsHtml}</ul>` : '<p>No findings detected.</p>'}
        <h4>Metadata</h4>
        <p><strong>Project:</strong> ${result.metadata?.projectName || 'Unknown'}</p>
        <p><strong>Source:</strong> ${result.metadata?.sourceType || 'Unknown'}</p>
        <p><strong>Repository:</strong> ${result.metadata?.repositoryUrl || 'Local Upload'}</p>
    `;

    document.getElementById('loading').classList.add('hidden');
    document.getElementById('resultsSection').classList.remove('hidden');

    if (currentPdfUrl) {
        URL.revokeObjectURL(currentPdfUrl);
    }
}

async function downloadReport() {
    const scanId = window.currentScanId;
    if (!scanId) {
        return;
    }

    const response = await fetch(`/api/scan/${scanId}/report/pdf`);
    if (!response.ok) {
        throw new Error(`Failed to download PDF: ${response.status}`);
    }

    const blob = await response.blob();
    currentPdfUrl = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = currentPdfUrl;
    anchor.download = `scan-${scanId}-report.pdf`;
    anchor.click();
}

function showError(message) {
    document.getElementById('loading').classList.add('hidden');
    document.getElementById('uploadSection').classList.remove('hidden');
    document.getElementById('error').classList.remove('hidden');
    document.getElementById('errorMessage').textContent = message;
}

function resetAnalysis() {
    document.getElementById('loading').classList.add('hidden');
    document.getElementById('resultsSection').classList.add('hidden');
    document.getElementById('error').classList.add('hidden');
    document.getElementById('uploadSection').classList.remove('hidden');
    document.getElementById('fileInput').value = '';
    document.getElementById('githubInput').value = '';
    clearErrors();
    window.currentScanId = null;
}

async function analyze() {
    const uploadType = document.querySelector('input[name="uploadType"]:checked')?.value || 'file';

    const isFileValid = uploadType === 'file' ? validateFile() : true;
    const isGithubValid = uploadType === 'github' ? validateGithub() : true;

    if (!isFileValid || !isGithubValid) {
        return;
    }

    const fileInput = document.getElementById('fileInput');
    const githubInput = document.getElementById('githubInput');
    const github = githubInput.value.trim();

    if (uploadType === 'file' && !fileInput.files.length) {
        showError('Please select a ZIP file to analyze.');
        return;
    }

    if (uploadType === 'github' && !github) {
        showError('Please enter a GitHub repository URL.');
        return;
    }

    showLoading('Uploading and scanning your project...');

    try {
        let response;

        if (uploadType === 'file') {
            const formData = new FormData();
            formData.append('file', fileInput.files[0]);
            response = await fetch('/api/upload', { method: 'POST', body: formData });
        } else {
            const formData = new URLSearchParams();
            formData.append('repoUrl', github);
            response = await fetch('/api/upload/github', { method: 'POST', body: formData });
        }

        if (!response.ok) {
            throw new Error(`Upload failed: ${response.status}`);
        }

        const uploadData = await response.json();
        window.currentScanId = uploadData.scanId;

        showLoading('Processing scan results...');

        const reportResponse = await fetch(`/api/scan/${uploadData.scanId}/report`);
        if (!reportResponse.ok) {
            throw new Error(`Report fetch failed: ${reportResponse.status}`);
        }

        const result = await reportResponse.json();
        showResults(result);
    } catch (error) {
        console.error(error);
        showError(error.message || 'An unexpected error occurred.');
    }
}

window.onload = function () {
    toggleUploadType();
    resetAnalysis();
};