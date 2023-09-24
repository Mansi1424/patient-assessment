document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("form");
    const riskContainer = document.getElementById("risk");

    form.addEventListener("submit", function (e) {
        e.preventDefault();

        // Get values from the form inputs
        const patId = document.getElementById("patId").value;
        const name = document.getElementById("name").value;
        const familyName = document.getElementById("familyname").value;

         if (!patId && !familyName) {
            // Both fields are empty, show an error message or prevent submission.
            alert("Please enter either Patient ID or Family Name.");
            return;
         }


        // Clear any previous risk value
        riskContainer.textContent = "";

        if (patId) {
            fetch('/assess/id', {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: `patId=${patId}`
            })

                .then((response) => {
                    if (!response.ok) {
                        throw new Error("Network response was not ok");
                    }
                    return response.text();
                })
                .then((riskData) => {
                console.log(riskData);
                    // Process patient history data and display risk
                    const riskElement = document.createElement("div")
                    riskElement.textContent = `Risk for Patient ID: ${JSON.stringify(patId)} - ${riskData}`;
                    riskContainer.appendChild(riskElement);
                })
                .catch((error) => {
                    console.error(error);
                    riskContainer.textContent = "Error occurred while fetching data.";
                });
        }

        else if (familyName) {
            fetch('/assess/familyName', {
                method: "POST",
                headers: {
                   "Content-Type": "application/x-www-form-urlencoded"
                },
                body: `familyName=${familyName}`
            })
                .then((response) => {
                    if (!response.ok) {
                        throw new Error("Network response was not ok");
                    }
                    return response.text();
                })
                .then((riskData) => {
                    // Process patient history data and display risk
                    console.log(riskData);
                    const riskElement = document.createElement("div")
                    riskElement.textContent = `Risk for Family Name: ${JSON.stringify(familyName)} - ${riskData}`;
                    riskContainer.appendChild(riskElement);
                })
                .catch((error) => {
                    console.error(error);
                    riskContainer.textContent = "Error occurred while fetching data.";
                });
        }
        // Make the first API request to get patient data

    });


});