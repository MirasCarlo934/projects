/*
 * Static classes:
 * 	-drawerHandle : for buttons that open a drawer
 * 	-drawer : for drawers
 * 
 * Static IDs:
 * 	-[drawer_id]Content : for element containing contents of drawer
 * 	-
 */

var drawerHandles;
var drawers;
var drawerHeight = 0;

$(document).ready(function() {
	drawerHandles = document.getElementsByClassName("drawerHandle");
	drawers = document.getElementsByClassName("drawer");
	
	for(i = 0; i < drawers.length; i++) {
		if ($("#" + drawers[i].id + "Content").height() > drawerHeight) {
			drawerHeight = $("#" + drawers[i].id + "Content").height();
		}
	}
	
	$(".drawerHandle").click(function() {
		var elementClass = $(this).attr("class");
		if(elementClass == "boxBtn drawerHandle") {
			console.log("Activating drawer handle " + $(this).attr("id"));
			$(this).addClass("selected");
		} else {
			$(this).removeClass();
			$(this).addClass("boxBtn drawerHandle");
		}
		
		for(i = 0; i < drawerHandles.length; ++i) {
			var drawerHandle = drawerHandles[i];
			if(drawerHandle.id != $(this).attr("id") && 
					$("#"+drawerHandle.id).attr("class").indexOf('selected')) { 
					//closes other activated drawer handles
				console.log("Deactivating drawer handle " + drawerHandle.id);
				$("#"+drawerHandle.id).removeClass();
				$("#"+drawerHandle.id).addClass("boxBtn drawerHandle");
			}
		}
	});
});

function buksan(drawerID, top_padding, bottom_padding) {
	for(i = 0; i < drawers.length; ++i) {
		var drawer = drawers[i];
		if(drawer.id != drawerID && $("#"+drawer.id).height() != 0) { //closes other opened drawers
			openDrawer(drawer.id, top_padding, bottom_padding);
			/* if($("#"+drawer.id).height() == 0) {
				console.log("Opening drawer " + drawer.id);
				$("#" + drawer.id).animate({
			        height: $("#" + drawer.id + "Content").height(), 
			    		paddingTop: top_padding, 
			    		paddingBottom: bottom_padding
			    }, openDrawer(drawerID, top_padding, bottom_padding));
			} else {
				console.log("Closing drawer " + drawer.id);
				$("#" + drawer.id).animate('slow', {
			        height: '0px',
			        	paddingTop: '0px', 
			    		paddingBottom: '0px'
			    }, openDrawer(drawerID, top_padding, bottom_padding));
			} */
		}
	}
	openDrawer(drawerID, top_padding, bottom_padding);
}

function openDrawer(drawerID, top_padding, bottom_padding) {	
	if($("#"+drawerID).height() == 0) {
		console.log("Opening drawer " + drawerID);
		$("#" + drawerID).animate({
//	        height: $("#" + drawerID + "Content").height(),
			height: drawerHeight,
	    		paddingTop: top_padding, 
	    		paddingBottom: bottom_padding
	    });
	} else {
		console.log("Closing drawer " + drawerID);
		$("#" + drawerID).animate({
	        height: '0px',
	        	paddingTop: '0px', 
	    		paddingBottom: '0px'
	    });
	}
}