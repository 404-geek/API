$(document).ready(function () {

	$.ajax({
		crossOrigin: true,
		type: "GET",
		url: "http://localhost:8080/blackbox/getsrcdest",
		cache: false,
		xhrFields: {
			withCredentials: true
		},
		success: function (data) {
			var obj = JSON.parse(data);
			var sources = obj.source;
			var destinations = obj.destination;
			var sourcehtml = "";
			var destinationhtml = "";

			$.each(sources, function (key, source) {
				sourcehtml += '<div id="' + key + '" class="d-inline-block border selector"><div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + source.logo + '"><p class="text-center">' + source.name + '</p></div></div>';
			});

			$.each(destinations, function (key, destination) {
				destinationhtml += '<div id="' + key + '" class="d-inline-block border selector"><div class="m-2"><img width="75" height="75" class="rounded-circle" src="' + destination.logo + '"><p class="text-center">' + destination.name + '</p></div></div>';
			});

			/*
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
			*/

			$("#sources").html(sourcehtml);
			$("#destinations").html(destinationhtml);


			$("#sources > .selector").click(function () {
				$("#sources div").removeClass("selected");
				$("#selectedSourceLinkValidate").off();
				$("#selectedSourceLinkAuthenticate").off();
				$("#datasource").hide();

				$("#sourceValidatedIndicator").hide();
				$("#sourceNotValidatedIndicator").hide();
				console.log("source: " + this.id);
				$("#sources > #" + this.id).addClass("selected");
				var id = this.id;
				$.each(sources, function (key, source) {
					if (id == key) {

						$("#selectedSource").html('<div class="d-inline-block border"><div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + source.logo + '"><p class="text-center">' + source.name + '</p></div></div>');
						var url = 'http://localhost:8080/blackbox/validate?type=source&srcdestId=' + key;
						console.log(url);
						$("#selectedSourceLinkAuthenticate").click(function () {
							window.open(url, "_blank", 'width=800, height=600, menubar=no, resizable=no, scrollbars=no, status=no, toolbar=no, location=no');
						});
						src = key;
						console.log(url);

						$("#selectedSourceLinkValidate").click(function () {
							$.ajax({
								crossOrigin: true,
								type: "GET",
								url: "http://localhost:8080/blackbox/isvalid?type=source&srcdestId=" + src,
								cache: false,
								xhrFields: {
									withCredentials: true
								},
								success: function (data) {
									var obj = JSON.parse(data);
									$("#datasource").hide();
									if (obj.isvalid == true) {
										console.log("source validated");
										$("#sourceValidatedIndicator").show();
										$("#sourceNotValidatedIndicator").hide();

									}
									else {
										console.log("source not validated");
										$("#sourceValidatedIndicator").hide();
										$("#sourceNotValidatedIndicator").show();

									}

									if (($('#sourceValidatedIndicator').css('display') != 'none') && ($('#destinationValidatedIndicator').css('display') != 'none')) {
										$("#datasource").show();
									}

								}
							});
						});

						$("#selectRow").show();
						$("#selectedSourceLinkValidate").show();
						$("#selectedSourceLinkAuthenticate").show();
					}
				});

			});

			$("#destinations > .selector").click(function () {
				$("#destinations div").removeClass("selected");
				$("#selectedDestinationFormSubmit").off();
				$("#selectedDestinationLinkValidate").off();
				$("#selectedDestinationLinkAuthenticate").off();

				$("#destinationValidatedIndicator").hide();
				$("#destinationNotValidatedIndicator").hide();
				console.log("destination: " + this.id);
				$("#destinations > #" + this.id).addClass("selected");
				var id = this.id;
				$.each(destinations, function (key, destination) {
					console.log(id + " " + key);
					if (id == key) {
						$("#selectedDestination").html('<div class="d-inline-block border"><div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + destination.logo + '"><p class="text-center">' + destination.name + '</p></div></div>');
						var srcdestid = key;
						console.log("uytrytf");
						$("#destination-form").submit(function (e) {
							e.preventDefault();
							console.log("jhgvgif");
							$('#destinationFormModal').modal('hide');
							var url = 'http://localhost:8080/blackbox/validate?type=destination&srcdestId=' + srcdestid + "&database_name=" + $('#InputDatabaseName').val() + "&db_username=" + $('#InputUsername').val() + "&db_password=" + $('#InputPassword').val() + "&server_host=" + $('#InputHostname').val() + "&server_port=" + $('#InputPort').val();
							window.open(url, "_blank", 'width=800, height=600, menubar=no, resizable=no, scrollbars=no, status=no, toolbar=no, location=no');
						});
						var dst = key;
						$("#selectedDestinationLinkValidate").click(function () {
							$.ajax({
								crossOrigin: true,
								type: "GET",
								url: "http://localhost:8080/blackbox/isvalid?type=destination&srcdestId=" + dst,
								cache: false,
								xhrFields: {
									withCredentials: true
								},
								success: function (data) {
									$("#datasource").hide();
									var obj = JSON.parse(data);
									if (obj.isvalid == true) {
										console.log("destination validated");
										$("#destinationNotValidatedIndicator").hide();
										$("#destinationValidatedIndicator").show();
									}
									else {
										console.log("destination not validated");
										$("#destinationValidatedIndicator").hide();
										$("#destinationNotValidatedIndicator").show();
									}
									if (($('#sourceValidatedIndicator').css('display') != 'none') && ($('#destinationValidatedIndicator').css('display') != 'none')) {
										$("#datasource").show();
									}
								}

							});
						});

						$('#destinationFormModal').on('show.bs.modal', function (e) {
							$.ajax({
								crossOrigin: true,
								type: "GET",
								url: "http://localhost:8080/blackbox/fetchdbs?destId=" + dst,
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

						$("#selectRow").show();
						$("#selectedDestinationLinkValidate").show();
						$("#selectedDestinationLinkAuthenticate").show();
					}
				});
			});



			$('#filterModal').on('show.bs.modal', function (e) {
				var endpointhtml = "";
				$.ajax({
					crossOrigin: true,
					type: "GET",
					url: "http://localhost:8080/blackbox/filterendpoints",
					cache: false,
					xhrFields: {
						withCredentials: true
					},
					success: function (data) {
						var obj = JSON.parse(data);
						for (var i = 0; i < obj.endpoints.length; i++) {
							endpointhtml += '<div class="form-check"><input class="form-check-input" type="checkbox" value="' + obj.endpoints[i] + '" id="' + obj.endpoints[i] + '"><label class="form-check-label" for="' + obj.endpoints[i] + '">' + obj.endpoints[i] + '</label></div>';
						}
						$("#filter-modal-body").html(endpointhtml);
					}
				});
			});
			$("#submitFilters").click(function (event) {
				event.preventDefault();
				var filters = $("#filter-modal-body input:checkbox:checked").map(function () {
					return $(this).val();
				}).get();
				var obj = {
					"endpoints": filters
				}
				console.log(JSON.stringify(obj));
				$.ajax({
					crossOrigin: true,
					data: { "filteredendpoints": JSON.stringify(obj) },
					type: "POST",
					url: "http://localhost:8080/blackbox/createdatasource",
					cache: false,
					xhrFields: {
						withCredentials: true
					},
					success: function (data) {
						window.location.href = "connections.html"
					}
				});
			});
		}

	});

});
