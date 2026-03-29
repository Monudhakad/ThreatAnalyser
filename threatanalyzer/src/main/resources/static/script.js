// # MAIN FUNCTION
async function analyze(){
    const fileInput = document.getElementById("fileInput");
    const githubInput = document.getElementById("githubInput");
    const github = githubInput.value.trim();

    // # VALIDATION
    if (fileInput.files.length === 0 && github === "") {
        alert("Please upload a file or enter a GitHub link");
        return;
    }

    if (github && !github.includes("github.com")) {
        alert("Enter a valid GitHub link");
        return;
    }

    // # UI TRANSITION
    document.getElementById("upload").classList.add("hidden");
    document.getElementById("loading").classList.remove("hidden");

    try {
        let response;

        if (fileInput.files.length > 0) {
            const formdata = new FormData();
            formdata.append("file", fileInput.files[0]);

            response = await fetch("/api/upload", {
                method: "POST",
                body: formdata
            });
        } else {
            const formdata = new URLSearchParams();
            formdata.append("repoUrl", github);

            response = await fetch("/api/upload/github", {
                method: "POST",
                body: formdata
            });
        }

        if (!response.ok) {
            throw new Error(`Upload failed: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();
        console.log("Scan ID:", data.scanId);

        const scanSummary = document.getElementById("scanSummary");
        scanSummary.innerHTML = `
            <p><strong>Scan ID:</strong> ${data.scanId}</p>
            <p>The report is generated as a PDF and can be downloaded below.</p>
        `;

        const pdfRes = await fetch(`/api/scan/${data.scanId}/report/pdf`);
        if (!pdfRes.ok) {
            throw new Error(`PDF fetch failed: ${pdfRes.status} ${pdfRes.statusText}`);
        }

        const blob = await pdfRes.blob();
        const url = URL.createObjectURL(blob);

        const pdfLink = document.getElementById("pdfLink");
        pdfLink.href = url;
        pdfLink.download = `scan-${data.scanId}-report.pdf`;
        pdfLink.textContent = "Download PDF Report";
        pdfLink.style.display = "inline-block";
        pdfLink.click();

        document.getElementById("loading").classList.add("hidden");
        document.getElementById("report").classList.remove("hidden");
    } catch (error) {
        console.error(error);
        alert("An error occurred while fetching the results. Check the browser console for details.");
        document.getElementById("loading").classList.add("hidden");
        document.getElementById("upload").classList.remove("hidden");
    }
}