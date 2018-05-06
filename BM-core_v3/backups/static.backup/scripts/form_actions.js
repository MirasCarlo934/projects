function submitForm_POST(formID, responseContainerID) {
	var url = $("#" + formID).attr("action");
	var data = $("#" + formID).serialize();
	console.log("Submitting form " + formID + " to " + url + " on POST method"); 
	$.post(url, data, function(data, status) {
		$("#" + responseContainerID).html(data);
	});
}

function submitForm_GET(formID, responseContainerID) {
	var url = $("#" + formID).attr("action");
	var data = $("#" + formID).serialize();
	console.log("Submitting form " + formID + " to " + url + " on GET method"); 
	$.get(url, data, function(data, status) {
		$("#" + responseContainerID).html(data);
	});
}