( function () {

    document.getElementById("login_button").addEventListener('click', (e) =>{
        console.log("Clicked")
        let form = e.target.closest("form");

        if (form.checkValidity()) {

            makeCall("POST", 'CheckLogin', e.target.closest("form"), (x) =>{

                if (x.readyState === XMLHttpRequest.DONE){

                    var message = x.responseText;

                    switch (x.status){

                        case 200:
                            setCookie('username', message, 30);
                            window.location.href = "home.html";
                            break;

                        case 500: // server error
                            document.getElementById("error_message").textContent = message;
                            break;

                        case 400: // bad request
                            document.getElementById("error_message").textContent = message;
                            break;

                        default:
                            document.getElementById("error_message").textContent = "JS Error: something went wrong";
                            break;
                    }
                }
            })
        } else {

            form.reportValidity();
        }
    })
})();