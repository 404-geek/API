$(document).ready(function() {

	$.ajax({
		crossOrigin: true,
      	type: "GET",
		url: "http://localhost:8080/getsrcdest",
		cache: false,
		xhrFields: {
			withCredentials: true
		},
		success: function( data ) {
			console.log(data);
			var obj = JSON.parse(data);
			var sources = obj.source;
			var destinations = obj.destination;
			var sourcehtml = "";
			var destinationhtml = "";

			for(var i=0; i<sources.length; i++) {
				var source = sources[i];
				console.log(source.name);
				sourcehtml += '<div id="' + source._id + '" class="d-inline-block border selector"><div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + source.logo + '"><p class="text-center">' + source.name + '</p></div></div>';
			}

			for(var i=0; i<destinations.length; i++) {
				var destination = destinations[i];
				console.log(destination.name);
				destinationhtml += '<div id="' + destination._id + '" class="d-inline-block border selector"><div class="m-2"><img width="75" height="75" class="rounded-circle" src="' + destination.logo + '"><p class="text-center">' + destination.name + '</p></div></div>';
			}

			$("#sources").html(sourcehtml);
			$("#destinations").html(destinationhtml);


			$("#sources > .selector").click(function(){
				$("#sources div").removeClass("selected");
				$("#selectedSourceLinkValidate").off();
				$("#selectedSourceLinkAuthenticate").off();
				$("#datasource").hide();
				
				$("#sourceValidatedIndicator").hide();
				$("#sourceNotValidatedIndicator").hide();
				console.log("source: " + this.id);
				$("#sources > #" + this.id).addClass("selected");
			
				for(var i=0; i<sources.length; i++) {
					if(this.id == sources[i]._id) {
						$("#selectedSource").html('<div class="d-inline-block border"><div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + sources[i].logo + '"><p class="text-center">' + sources[i].name + '</p></div></div>');
						var url = 'http://localhost:8080/validate?type=source&srcdestId=' + sources[i]._id;
						$("#selectedSourceLinkAuthenticate").click(function(){
							window.open(url, "_blank", 'width=800, height=600, menubar=no, resizable=no, scrollbars=no, status=no, toolbar=no, location=no');
						});
						src = sources[i]._id;
						console.log(url);

						$("#selectedSourceLinkValidate").click(function(){
							$.ajax({
								crossOrigin: true,
								type: "GET",
								url: "http://localhost:8080/isvalid?type=source&srcdestId=" + src,
								cache: false,
								xhrFields: {
									withCredentials: true
								},
								success: function(data) {
									var obj = JSON.parse(data);
									$("#datasource").hide();
									if(obj.isvalid == true) {
										console.log("source validated");
										$("#sourceValidatedIndicator").show();
										$("#sourceNotValidatedIndicator").hide();
										
									}
									else {
										console.log("source not validated");
										$("#sourceValidatedIndicator").hide();
										$("#sourceNotValidatedIndicator").show();
										
									}

									if(($('#sourceValidatedIndicator').css('display') != 'none') && ($('#destinationValidatedIndicator').css('display') != 'none')){
										$("#datasource").show(); 
									}
									
								}
							});
						});
						
						$("#selectRow").show();
						$("#selectedSourceLinkValidate").show();
						$("#selectedSourceLinkAuthenticate").show();
					}
				}
				
			});

			$("#destinations > .selector").click(function(){
				$("#destinations div").removeClass("selected");
				$("#selectedDestinationFormSubmit").off();
				$("#selectedDestinationLinkValidate").off();
				$("#selectedDestinationLinkAuthenticate").off();
				
				$("#destinationValidatedIndicator").hide();
				$("#destinationNotValidatedIndicator").hide();
				console.log("destination: " + this.id);
				$("#destinations > #" + this.id).addClass("selected");

				for(var i=0; i<destinations.length; i++) {
					if(this.id == destinations[i]._id) {
						$("#selectedDestination").html('<div class="d-inline-block border"><div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + destinations[i].logo + '"><p class="text-center">' + destinations[i].name + '</p></div></div>');
						var srcdestid = destinations[i]._id;
						$("#destination-form").submit(function(e){
							e.preventDefault();
							$('#destinationFormModal').modal('hide');
							var url = 'http://localhost:8080/validate?type=destination&srcdestId=' + srcdestid + "&database_name=" + $('#InputDatabaseName').val() + "&db_username=" + $('#InputUsername').val() + "&db_password=" + $('#InputPassword').val() + "&server_host=" + $('#InputHostname').val() + "&server_port=" + $('#InputPort').val();
							window.open(url, "_blank", 'width=800, height=600, menubar=no, resizable=no, scrollbars=no, status=no, toolbar=no, location=no');
						});
						var dst = destinations[i]._id;
						$("#selectedDestinationLinkValidate").click(function(){
							$.ajax({
								crossOrigin: true,
								type: "GET",
								url: "http://localhost:8080/isvalid?type=destination&srcdestId=" + dst,
								cache: false,
								xhrFields: {
									withCredentials: true
								},
								success: function(data) {
									$("#datasource").hide();
									var obj = JSON.parse(data);
									if(obj.isvalid == true) {
										console.log("destination validated");
										$("#destinationNotValidatedIndicator").hide();
										$("#destinationValidatedIndicator").show();
									}
									else {
										console.log("destination not validated");
										$("#destinationValidatedIndicator").hide();
										$("#destinationNotValidatedIndicator").show();
									}
									if(($('#sourceValidatedIndicator').css('display') != 'none') && ($('#destinationValidatedIndicator').css('display') != 'none')){
										$("#datasource").show(); 
									}
								}
								
							});
						});
						
						$("#selectRow").show();
						$("#selectedDestinationLinkValidate").show();
						$("#selectedDestinationLinkAuthenticate").show();
					}
				}
			});

			$("#datasource").click(function() {
				$.ajax({
					crossOrigin: true,
					type: "GET",
					url: "http://localhost:8080/createdatasource",
					cache: false,
					xhrFields: {
						withCredentials: true
					},
					success: function(data) {
						window.location.href = "connections.html"
					}
				});
				
			});
		}
	});
	
});