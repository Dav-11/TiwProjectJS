/**
 * AJAX call management
 */

function makeCall(method, url, formElement, callback, reset = true) {
    var req = new XMLHttpRequest(); // visible by closure
    req.onreadystatechange = function() {
        callback(req)
    }; // closure
    req.open(method, url);
    if (formElement == null) {
        req.send();
    } else {
        console.log(req); //TODO: delete
        req.send(new FormData(formElement));
    }
    if (formElement !== null && reset === true) {
        formElement.reset();
    }
}

function setCookie(key, value, expiryDays) {

    value = value.replace(/(\r\n|\n|\r)/gm, "");

    let date = new Date();
    date.setTime(date.getTime() + (expiryDays * 24 * 60 * 60 * 1000));
    const expires = "expires=" + date.toUTCString();
    const cookie = key + "=" + value + "; " + expires + "; path=/";
    console.log("COOKIE = " + cookie);
    document.cookie = cookie;
}

function getCookie(key) {

    const name = key + "=";

    // gets all the cookies
    const decoded = decodeURIComponent(document.cookie);

    // split the line in an array of string, one for each cookie
    const cookieArray = decoded.split('; ');
    console.log(cookieArray);
    let value = null;

    // for each line of the array
    cookieArray.forEach( (string) => {
        if (string.indexOf(name) === 0) value = string.substr(name.length);
    })

    return value;
}

function cancelCookie(key){

    let date = new Date();
    const expires = "expires=" + date.toUTCString();
    const cookie = key + "=" + "" + "; " + expires + "; path=/";
    console.log("COOKIE = " + cookie);
    document.cookie = cookie;
}