// # MAIN FUNCTION
function analyze(){

    const file = document.getElementById("fileInput").value;
    const github = document.getElementById("githubInput").value;

    // # VALIDATION
    if(file === "" && github.trim() === ""){
        alert("Please upload a file or enter a GitHub link");
        return;
    }

    if(github && !github.includes("github.com")){
        alert("Enter a valid GitHub link");
        return;
    }

    // # UI TRANSITION
    document.getElementById("upload").classList.add("hidden");
    document.getElementById("loading").classList.remove("hidden");

    setTimeout(()=>{
        document.getElementById("loading").classList.add("hidden");
        document.getElementById("report").classList.remove("hidden");
    },2000);
}