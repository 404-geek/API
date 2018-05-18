$(document).ready(function () {

  $("#login-form").submit(function (e) {
    e.preventDefault();
    username = $("#InputUserID").val();
    password = $("#InputPassword").val();;
    $.ajax({
      crossOrigin: true,
      type: "GET",
      cache: false,
      data: { userId: username, password: password },
      xhrFields: {
        withCredentials: true
       },
      url: "http://localhost:8080/blackbox/login",
      success: function (data) {
        console.log(data);
        response = JSON.parse(data);

        if (response.status == 200) {
          window.location.href = "datasources.html";
        }
        else if(response.status == 404) {
          $("#login-error").html("User not found!");
          $("#login-error").show();
        }
      },
      
      error: function(data) {
        console.log(data);
      }
    });
  });
});
