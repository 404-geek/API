$(document).ready(function() {
    var logo;
    $.ajax({
		crossOrigin: true,
      	type: "GET",
		url: "/getconnectionids",
		cache: false,
		xhrFields: {
			withCredentials: true
		},
		success: function( data ) {
            var obj = JSON.parse(data);
            var srcdst = obj.data.srcdestId;
            logo = obj.images;
            var srcdesthtml="";
            
            for(var i=0; i<srcdst.length; i++) {
				var srcdest = srcdst[i];
                srcdesthtml = srcdesthtml + '<div id="connections" class="bg-grey rounded border-colored p-2 m-2 d-inline-block"><div>' + srcdest.connectionId + '</div><hr><div class="d-inline-block text-center"><div class="d-inline-block border text-center">Source<div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + logo.source[srcdest.sourceName].logo + '"><p class="text-center">' + logo.source[srcdest.sourceName].name + '</p></div></div></div><div class="d-inline-block text-center"><div class="d-inline-block border text-center">Destination<div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + logo.destination[srcdest.destName].logo + '"><p class="text-center">' + logo.destination[srcdest.destName].name + '</p></div></div></div><div><button id="view" type="button" data-toggle="modal" data-target="#display-modal" data-button-type="view" data-connId="' + srcdest.connectionId + '"  data-source="' + srcdest.sourceName + '" data-destination="' + srcdest.destName + '" class="btn btn-light">View</button><button data-toggle="modal" data-target="#display-modal" data-button-type="export" data-connId="' + srcdest.connectionId + '" data-source="' + srcdest.sourceName + '" data-destination="' + srcdest.destName + '" id="export" type="button" class="btn btn-light">Export</button><button id="delete" type="button" data-toggle="modal" data-target="#display-modal" data-button-type="delete" data-connId="' + srcdest.connectionId + '" class="btn btn-light">Delete</button></div></div>';
            }
            $("#connections").html(srcdesthtml);

        }
    });

    $('#display-modal').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget); // Button that triggered the modal
        var connId = button.data('connid');
        var source = button.data('source');
        var destination = button.data('destination');
        var buttonType = button.data('button-type');
        var modal = $(this);
        //modal.find('.modal-title').text('New message to ' + recipient);
        //modal.find('.modal-body input').val(recipient);
        $("#modal-body").html("");
        if(buttonType == "delete"){
        	$.ajax({
                crossOrigin: true,
                type: "GET",
                url: "/deletedatasource?connId="+ connId,
                cache: false,
                xhrFields: {
                    withCredentials: true
                },
                success: function(data) {
					console.log(data);
                	var obj = JSON.parse(data);
                	
					status = obj.status;
					console.log(obj);
                	if(status == 14) {
                		$("#modal-body").html("Successfully Deleted!!");
                	}
                }
            });
        }
        else{
        	$.ajax({
	            crossOrigin: true,
	            type: "GET",
	            url: "/checkconnection?connId="+ connId + "&choice="+ buttonType,
	            cache: false,
	            xhrFields: {
	                withCredentials: true
	            },
	            success: function( data ) {
	                var obj = JSON.parse(data);
	                console.log(data);
	                status = obj.status;
	                if(status == 11) {
	                    $('#refresh-connection').show();
	                    $('#source-col').show();
	                    $("#selectedSource").html('<div class="d-inline-block border"><div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + logo.source[source].logo + '"><p class="text-center">' + logo.source[source].name + '</p></div></div>');
	                    var url = '/validate?type=source&srcdestId=' + source;
	                    $("#selectedSourceLinkAuthenticate").click(function(){
	                        window.open(url, "_blank", 'width=800, height=600, menubar=no, resizable=no, scrollbars=no, status=no, toolbar=no, location=no');
	                    });
	                    src = source;
	                    $("#selectedSourceLinkValidate").click(function(){
	                        $.ajax({
	                            crossOrigin: true,
	                            type: "GET",
	                            url: "/isvalid?type=source&srcdestId=" + src,
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
	                                if( $('#sourceValidatedIndicator').css('display') != 'none'){
	                                    $("#datasource").show(); 
	                                }
	                            }
	                        });
	                    });

	                }
	                else if(status == 12) {
	                    $('#refresh-connection').show();
	                    $('#destination-col').show();

	                    dst = destination;
	                    $("#selectedDestination").html('<div class="d-inline-block border"><div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + logo.destination[destination].logo + '"><p class="text-center">' + logo.destination[destination].name + '</p></div></div>');
	                    $("#selectedDestinationLinkAuthenticate").click(function(){
	                        $("#modal-body").hide();
	                        $("#refresh-connection").hide();
							$("#destination-form").show();
							$.ajax({
								crossOrigin: true,
								type: "GET",
								url: "/fetchdbs?destId=" + dst,
								cache: false,
								xhrFields: {
									withCredentials: true
								},
								success: function (data) {
									var obj = JSON.parse(data);
									var listhtml = "";
									
									if (obj.status == 200) {
										for (var i = 0; i < obj.data.length; i++) {
											conn = obj.data[i];
											listhtml += '<button type="button" class="list-group-item list-group-item-action prev-conn-button" data-db-name="' + conn.credentials[0].value + '" data-db-username="' + conn.credentials[1].value + '" data-server-host="' + conn.credentials[2].value + '" data-server-port="' + conn.credentials[3].value + '" data-db-password="' + conn.credentials[4].value + '">' + conn._id + '</button>'
										}
										$("#prev-conn").html(listhtml);
										$(".prev-conn-button").click(function (event) {
											var button = $(this); // Button that triggered the modal
											var dbname = button.data('db-name');
											var username = button.data('db-username');
											var host = button.data('server-host');
											var port = button.data('server-port');
											var password = button.data('db-password');
											$("#InputDatabaseName").val(dbname);
											$("#InputUsername").val(username);
											$("#InputPassword").val(password);
											$("#InputHostname").val(host);
											$("#InputPort").val(port);
										});
									}
								}

							});
	                    });
						
	                    $("#selectedDestinationLinkValidate").click(function(){
	                        $.ajax({
	                            crossOrigin: true,
	                            type: "GET",
	                            url: "/isvalid?type=destination&srcdestId=" + dst,
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
	                                if( $('#destinationValidatedIndicator').css('display') != 'none'){
	                                    $("#datasource").show();
	                                }
	                            }
	                        });
	                    });

	                }
	                else if(status == 13) {
	                    $('#refresh-connection').show();
	                    $('#source-col').show();
	                    $('#destination-col').show();

	                    $("#selectedSource").html('<div class="d-inline-block border"><div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + logo.source[source].logo + '"><p class="text-center">' + logo.source[source].name + '</p></div></div>');
	                    var url = '/validate?type=source&srcdestId=' + source;
	                    $("#selectedSourceLinkAuthenticate").click(function(){
	                        window.open(url, "_blank", 'width=800, height=600, menubar=no, resizable=no, scrollbars=no, status=no, toolbar=no, location=no');
	                    });
	                    src = source;
	                    $("#selectedSourceLinkValidate").click(function(){
	                        $.ajax({
	                            crossOrigin: true,
	                            type: "GET",
	                            url: "/isvalid?type=source&srcdestId=" + src,
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

						
	                    dst = destination;
						$("#selectedDestination").html('<div class="d-inline-block border"><div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + logo.destination[destination].logo + '"><p class="text-center">' + logo.destination[destination].name + '</p></div></div>');
	                    $("#selectedDestinationLinkAuthenticate").click(function(){
	                        $("#modal-body").hide();
	                        $("#refresh-connection").hide();
							$("#destination-form").show();
							$.ajax({
								crossOrigin: true,
								type: "GET",
								url: "/fetchdbs?destId=" + dst,
								cache: false,
								xhrFields: {
									withCredentials: true
								},
								success: function (data) {
									var obj = JSON.parse(data);
									var listhtml = "";
									
									if (obj.status == 200) {
										for (var i = 0; i < obj.data.length; i++) {
											conn = obj.data[i];
											listhtml += '<button type="button" class="list-group-item list-group-item-action prev-conn-button" data-db-name="' + conn.credentials[0].value + '" data-db-username="' + conn.credentials[1].value + '" data-server-host="' + conn.credentials[2].value + '" data-server-port="' + conn.credentials[3].value + '" data-db-password="' + conn.credentials[4].value + '">' + conn._id + '</button>'
										}
										$("#prev-conn").html(listhtml);
										$(".prev-conn-button").click(function (event) {
											var button = $(this); // Button that triggered the modal
											var dbname = button.data('db-name');
											var username = button.data('db-username');
											var host = button.data('server-host');
											var port = button.data('server-port');
											var password = button.data('db-password');
											$("#InputDatabaseName").val(dbname);
											$("#InputUsername").val(username);
											$("#InputPassword").val(password);
											$("#InputHostname").val(host);
											$("#InputPort").val(port);
										});
									}
								}

							});
	                    });

	                    $("#selectedDestinationLinkValidate").click(function(){
	                        $.ajax({
	                            crossOrigin: true,
	                            type: "GET",
	                            url: "/isvalid?type=destination&srcdestId=" + dst,
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
	                                if( $('#destinationValidatedIndicator').css('display') != 'none'){
	                                    $("#datasource").show();
	                                }
	                            }
	                        });
	                    });

	                }
	                else if (status == 21) {
	                    $("#modal-body").html(JSON.stringify(obj.data));
	                }
	                else if(status == 22) {
	                    $("#modal-body").html("Successfully Pushed!!");
	                }
	                $("#destination-form").submit(function(e){
	                    e.preventDefault();
	                    var url = '/validate?type=destination&srcdestId=' + destination + "&database_name=" + $('#InputDatabaseName').val() + "&db_username=" + $('#InputUsername').val() + "&db_password=" + $('#InputPassword').val() + "&server_host=" + $('#InputHostname').val() + "&server_port=" + $('#InputPort').val();
	                    window.open(url, "_blank", 'width=800, height=600, menubar=no, resizable=no, scrollbars=no, status=no, toolbar=no, location=no');
	                    
	                    $("#modal-body").show();
	                    $("#refresh-connection").show();
	                    $("#destination-form").hide();
	                });
	            }
        	});
        }
        $("#datasource").click(function() {
            $.ajax({
                crossOrigin: true,
                type: "POST",
                url: "/createdatasource",
                cache: false,
                xhrFields: {
                    withCredentials: true
                },
                success: function(data) {
                    $('#display-modal').modal('hide');
                }
            });
            
        });
    });

    $('#display-modal').on('hidden.bs.modal', function (event) {
        $("#refresh-connection").hide();
        $("#source-col").hide();
        $("#sourceValidatedIndicator").hide();
        $("#sourceNotValidatedIndicator").hide();
        $("#destination-col").hide();
        $("#destinationValidatedIndicator").hide();
        $("#destinationNotValidatedIndicator").hide();
        $("#datasource").hide();
        $("#selectedSource").html("");
        $("#selectedDestination").html("");
        $("#modal-body").html("");
        $("#selectedSourceLinkAuthenticate").off();
        $("#selectedSourceLinkValidate").off();        
        $("#selectedDestinationLinkAuthenticate").off();
        $("#selectedDestinationLinkValidate").off();
        
    });
    
});