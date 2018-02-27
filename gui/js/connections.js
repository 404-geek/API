$(document).ready(function() {
    $.ajax({
		crossOrigin: true,
      	type: "GET",
		url: "http://localhost:8080/getconnectionids",
		cache: false,
		xhrFields: {
			withCredentials: true
		},
		success: function( data ) {
            console.log(data);
            var obj = JSON.parse(data);
            var srcdst = obj.data.srcdestId;
            var logo = obj.images;
            var srcdesthtml="";
            
            for(var i=0; i<srcdst.length; i++) {
				var srcdest = srcdst[i];
                srcdesthtml = srcdesthtml + '<div id="connections" class="bg-grey rounded border-colored p-2 m-2 d-inline-block"><div>' + srcdest.connectionId + '</div><hr><div class="d-inline-block text-center"><div class="d-inline-block border text-center">Source<div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + logo.source[srcdest.sourceName].logo + '"><p class="text-center">' + srcdest.sourceName + '</p></div></div></div><div class="d-inline-block text-center"><div class="d-inline-block border text-center">Destination<div class="m-2"><img class="rounded-circle" width="75" height="75" src="' + logo.destination[srcdest.destName].logo + '"><p class="text-center">' + srcdest.destName + '</p></div></div></div><div><button id="view" type="button" data-toggle="modal" data-target="#display-modal" class="btn btn-light" onclick="abc('+ "'" +  srcdest.connectionId + "'" +  ', ' + "'" + 'view' + "'" + ')">View</button><button onclick="abc('+ "'" +  srcdest.connectionId + "'" +  ', ' + "'" + 'export' + "'" + ')" data-toggle="modal" data-target="#display-modal" id="export" type="button" class="btn btn-light">Export</button></div></div>';
            }
            $("#connections").html(srcdesthtml);

        }
    });
});
var abc = function(conn, choice){
    $.ajax({
        crossOrigin: true,
        type: "GET",
        url: "http://localhost:8080/selectaction?conId="+ conn + "&choice="+ choice,
        cache: false,
        xhrFields: {
            withCredentials: true
        },
        success: function( data ) {
            $("#modal-body").html(data);
        }
    });
};