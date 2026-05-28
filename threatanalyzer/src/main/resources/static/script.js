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
    const severitySummary = document.getElementById('severitySummary');
    const resultsChart = document.getElementById('resultsChart');

    const showDependencies = document.getElementById('filterDependencies')?.checked ?? true;
    const showTechnologies = document.getElementById('filterTechnologies')?.checked ?? true;

    const findings = (result.findings || []).filter(finding => {
        if (!showDependencies && finding.type === 'Vulnerable_dependency') {
            return false;
        }

        return true;
    });

    const technologies = showTechnologies ? (result.technologies || []) : [];

    const severityCounts = {
        CRITICAL: 0,
        HIGH: 0,
        MEDIUM: 0,
        LOW: 0
    };

    findings.forEach(finding => {
        const severity = (finding.severity || 'UNKNOWN').toUpperCase();
        if (severityCounts[severity] !== undefined) {
            severityCounts[severity] += 1;
        }
    });

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

    const severityHtml = Object.entries(severityCounts).map(([level, count]) => `
        <div class="severity-badge ${level.toLowerCase()}">
            <span class="severity-label">${level}</span>
            <strong>${count}</strong>
        </div>
    `).join('');

    severitySummary.innerHTML = severityHtml;

    const totalFindings = Math.max(findings.length, 1);
    const chartBars = Object.entries(severityCounts).map(([level, count]) => {
        const width = Math.round((count / totalFindings) * 100);
        return `
            <div class="chart-row">
                <span>${level}</span>
                <div class="chart-track">
                    <div class="chart-fill ${level.toLowerCase()}" style="width: ${width}%"></div>
                </div>
                <span>${count}</span>
            </div>
        `;
    }).join('');
    resultsChart.innerHTML = chartBars;

    const findingsRows = findings.map(finding => `
        <tr>
            <td>${finding.type}</td>
            <td><span class="badge ${finding.severity?.toLowerCase() || 'low'}">${finding.severity || 'UNKNOWN'}</span></td>
            <td>${finding.summary || ''}</td>
            <td>${finding.cveId || ''}</td>
            <td>${finding.remediation || ''}</td>
            <td>${finding.file || ''}</td>
        </tr>
    `).join('');

    const technologiesRows = technologies.map(tech => `
        <tr>
            <td>${tech.technology}</td>
            <td>${tech.detectedFrom || 'Detected'}</td>
        </tr>
    `).join('');

    resultsDetail.innerHTML = `
        <div class="section-block">
            <h4>Security Findings</h4>
            ${findingsRows ? `
                <table class="results-table">
                    <thead>
                        <tr>
                            <th>Type</th>
                            <th>Severity</th>
                            <th>Summary</th>
                            <th>CVE</th>
                            <th>Remediation</th>
                            <th>File</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${findingsRows}
                    </tbody>
                </table>
            ` : '<p>No findings detected.</p>'}
        </div>
        <div class="section-block">
            <h4>Detected Technologies</h4>
            ${technologiesRows ? `
                <table class="results-table">
                    <thead>
                        <tr><th>Technology</th><th>Source</th></tr>
                    </thead>
                    <tbody>
                        ${technologiesRows}
                    </tbody>
                </table>
            ` : '<p>No technologies detected.</p>'}
        </div>
        <div class="section-block">
            <h4>Scan Metadata</h4>
            <table class="meta-table">
                <tbody>
                    <tr><th>Project</th><td>${result.metadata?.projectName || 'Unknown'}</td></tr>
                    <tr><th>Source</th><td>${result.metadata?.sourceType || 'Unknown'}</td></tr>
                    <tr><th>Repository</th><td>${result.metadata?.repositoryUrl || 'Local Upload'}</td></tr>
                    <tr><th>Total Files</th><td>${result.metadata?.totalFiles || 'Unknown'}</td></tr>
                </tbody>
            </table>
        </div>
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