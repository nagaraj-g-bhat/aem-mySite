function myFunction(status){
    var servletResource = $(".my-resource").data("current-resource");
    $.ajax({
        "url": servletResource + ".button-component.html",
        "data": {
            "statusFlag" : status
        },
        "type": "POST",
        "success": () => {
            console.log("Success!");
        },
        "failure": () => {
            console.log("Fail!");
        }
    });
}