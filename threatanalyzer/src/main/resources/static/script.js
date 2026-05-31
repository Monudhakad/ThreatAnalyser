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

function handleFileSelection() {
    const fileInput = document.getElementById('fileInput');
    const uploadText = document.getElementById('fileUploadText');
    const file = fileInput.files[0];

    if (!file) {
        uploadText.textContent = '📁 Click to upload or drag ZIP file here';
        return;
    }

    uploadText.textContent = `Selected: ${file.name} (${(file.size / 1024 / 1024).toFixed(1)} MB)`;
    validateFile();
}

function setStartButtonState(enabled) {
    const button = document.getElementById('startAnalysisBtn');
    button.disabled = !enabled;
}

function setLoadingProgress(progress) {
    const progressFill = document.querySelector('.progress-fill');
    if (progress === null || progress === undefined) {
        progressFill.style.animation = 'progress 2s ease-in-out infinite';
        progressFill.style.width = '100%';
        return;
    }

    progressFill.style.animation = 'none';
    progressFill.style.width = `${progress}%`;
}

function showLoading(message, progress = null) {
    document.getElementById('uploadSection').classList.add('hidden');
    document.getElementById('loading').classList.remove('hidden');
    document.getElementById('resultsSection').classList.add('hidden');
    document.getElementById('error').classList.add('hidden');
    document.getElementById('loadingStatus').textContent = message;
    setLoadingProgress(progress);
    setStartButtonState(false);
}

async function uploadFileWithProgress(file) {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('file', file);

        xhr.open('POST', '/api/upload');

        xhr.upload.addEventListener('progress', (event) => {
            if (event.lengthComputable) {
                const percent = Math.round((event.loaded / event.total) * 100);
                setLoadingProgress(percent);
                document.getElementById('loadingStatus').textContent = `Uploading project... ${percent}%`;
            } else {
                setLoadingProgress(null);
                document.getElementById('loadingStatus').textContent = 'Uploading project...';
            }
        });

        xhr.onload = () => {
            if (xhr.status >= 200 && xhr.status < 300) {
                try {
                    resolve(JSON.parse(xhr.responseText));
                } catch (error) {
                    reject(new Error('Upload response could not be parsed.'));
                }
            } else {
                reject(new Error(`Upload failed: ${xhr.status}`));
            }
        };

        xhr.onerror = () => reject(new Error('Upload failed due to a network error.'));
        xhr.send(formData);
    });
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

function escapeHtml(text) {
    return (text || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function truncateText(text, maxLength = 60) {
    if (!text) return '';
    if (text.length <= maxLength) return escapeHtml(text);
    const edge = Math.floor((maxLength - 3) / 2);
    return `${escapeHtml(text.slice(0, edge))}...${escapeHtml(text.slice(text.length - edge))}`;
}

function showLoading(message, progress = null) {
    document.getElementById('uploadSection').classList.add('hidden');
    document.getElementById('loading').classList.remove('hidden');
    document.getElementById('resultsSection').classList.add('hidden');
    document.getElementById('error').classList.add('hidden');
    document.getElementById('loadingStatus').textContent = message;
    setLoadingProgress(progress);
    setStartButtonState(false);
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

    const findingsRows = findings.map(finding => {
        const fileValue = finding.file || '';
        return `
        <tr>
            <td>${escapeHtml(finding.type || '')}</td>
            <td><span class="badge ${escapeHtml((finding.severity || 'low').toLowerCase())}">${escapeHtml(finding.severity || 'UNKNOWN')}</span></td>
            <td>${escapeHtml(finding.summary || '')}</td>
            <td>${escapeHtml(finding.cveId || '')}</td>
            <td>${escapeHtml(finding.remediation || '')}</td>
            <td class="path-cell" title="${escapeHtml(fileValue)}">${truncateText(fileValue, 60)}</td>
        </tr>
    `;
    }).join('');

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
    setStartButtonState(true);
}

function resetAnalysis() {
    document.getElementById('loading').classList.add('hidden');
    document.getElementById('resultsSection').classList.add('hidden');
    document.getElementById('error').classList.add('hidden');
    document.getElementById('uploadSection').classList.remove('hidden');
    document.getElementById('fileInput').value = '';
    document.getElementById('githubInput').value = '';
    document.getElementById('fileUploadText').textContent = '📁 Click to upload or drag ZIP file here';
    clearErrors();
    window.currentScanId = null;
    setStartButtonState(true);
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

    showLoading('Uploading your project...', 0);

    try {
        let uploadData;

        if (uploadType === 'file') {
            uploadData = await uploadFileWithProgress(fileInput.files[0]);
        } else {
            const formData = new URLSearchParams();
            formData.append('repoUrl', github);
            const response = await fetch('/api/upload/github', { method: 'POST', body: formData });
            if (!response.ok) {
                throw new Error(`Upload failed: ${response.status}`);
            }
            uploadData = await response.json();
        }

        window.currentScanId = uploadData.scanId;
        showLoading('Processing scan results...', null);

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