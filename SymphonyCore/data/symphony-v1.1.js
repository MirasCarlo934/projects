var wsUri = "ws://"+location.hostname+":81/";
websocket = new WebSocket(wsUri);
var itm;

var updateDone;
var counter = 0;

function openTab(evt, tabName) {
    var i, tabcontent, tablinks;
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    document.getElementById(tabName).style.display = "block";
    evt.currentTarget.className += " active";
    if (tabName == "upload") {
    	fetchFiles();
    }
}
function initialize() {
  document.getElementById("defaultOpen").click(); 
}

function fetchFiles() {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState==4 && this.status==200) {
      var p = document.getElementById("files");
      p.innerHTML = this.responseText;
    }
  }
  xhttp.open('GET', '/getFiles', true);
  xhttp.send();
}
function uploadFile() {
  document.getElementById("msg").innerHTML = "";
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState==4 && this.status==200) {
      document.getElementById("msg").innerHTML = this.responseText;
    }
  }
  var form = document.getElementById('fileUpload');
  var formData = new FormData(form);
  xhttp.open('POST', '/uploadFile', true);
  xhttp.send(formData);
}

function getProgress() {
    counter++;
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
  	  if (this.readyState == 4 && this.status == 200) {
  		  clearInterval(updateDone);
  		  counter = 0;
  		  document.getElementById("msg").innerHTML = this.responseText;
  		  document.getElementById("btn").innerHTML = "Done";
      } else if (this.readyState == 4 && this.status == 203) {
          document.getElementById("msg").innerHTML = "HTTP_UPDATE_FAILED";
          document.getElementById("btn").disabled = false;
          document.getElementById("btn").innerHTML = "Submit";
      } else {
          //document.getElementById("debug").innerHTML = document.getElementById("debug").innerHTML+"<br>state="+this.readyState+ " status="+this.status;
      }
    };
    xhttp.open("GET", "/progress", true);
    xhttp.send();
    document.getElementById("btn").innerHTML = counter;
}
function updateFirmware() {
  var choice = confirm("Warning!\nThis will update the firmware.");
  if (choice == true) {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
	  if (this.readyState == 4 && this.status == 202) {
       document.getElementById("msg").innerHTML = this.responseText;
       document.getElementById("btn").disabled = true;
       updateDone = setInterval(getProgress, 1000);
      }
    };
    var form = document.getElementById("update");
    xhttp.open("GET", "/handleUpdate?update=1", true);
    xhttp.send();
  } 
}

function loadDoc() {
  var xhttp;
  xhttp=new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
    	myFunction(this);
    }
  };
  xhttp.open("GET", "/properties.html", true);
  xhttp.send();
}
function myFunction(xhttp) {
	var txt = document.getElementById("txt2");
    txt.value=txt.value+"\n"+xhttp.responseText
    jsonData = "["+xhttp.responseText.replace(/([a-zA-Z0-9]+?):/g, '"$1":').replace(/'/g,'"')+"]"
  //this is the temp array of object {typ:'Rad',lbl:'RED',val:'0007', grp:'g2'}
    itm = JSON.parse(jsonData);
    //we will create a new array within an array grouped according to temp[i].grp
    //[ 
    //    [{typ:'Rad',lbl:'RED',val:'0007'},{typ:'Rad',lbl:'GREEN',val:'0007'}],
    //    [{typ:'Rad',lbl:'HUE',val:'0007'},{typ:'Rad',lbl:'SATURATION',val:'0007'}],
    // ]
    //  grp['grp1']:[{typ:'Rad',lbl:'RED',val:'0007'},{typ:'Rad',lbl:'GREEN',val:'0007'}]
    //  grp['grp2']:[{typ:'Rad',lbl:'HUE',val:'0007'},{typ:'Rad',lbl:'SATURATION',val:'0007'}]
    var arrayLength = itm.length;
    var grp = new Object();   //a vector object where the key is the grpid.
    /** we need to create this DOM
    <label class="bar barchkbox">
    <div class="lbl">RED</div>
    <input lbl="RED" id="$NAME$:0001" name="$NAME$" value="0001" onclick="sendOnOffWs(this)" type="radio">
    <div class="btn btntxt"></div>
    </label><br>
    */
    for (var i = 0; i < arrayLength; i++) {
    	var thisGroup = grp[itm[i].grp];
    	if(thisGroup==undefined) {
        	thisGroup = new groups();
        	thisGroup.add(itm[i]);
        	grp[itm[i].grp] = thisGroup;
    	} else {
        	thisGroup.add(itm[i]);
    	}
    }
    var mainDiv = document.getElementById("main");
    for (var k in grp) {
    	var fs = document.createElement('fieldset');
    	fs.setAttribute('style', 'width:30em;text-align:center;')
    	var legend = document.createElement('legend');
    	legend.innerHTML = k;
    	fs.appendChild(legend);
    	mainDiv.appendChild(fs);
    	for (var j = 0; j < grp[k].ctr; j++) {
    		var p = grp[k].properties[j];
    		var lbl = document.createElement('label');
    		lbl.setAttribute('class', "bar barrng");
    		fs.appendChild(lbl);
    		var div1 = document.createElement('div');
    		div1.setAttribute('class', "lbl");
    		div1.innerHTML = p.lbl;
    		var div2 = document.createElement('div');
    		div2.setAttribute('class', "btn btntxt");
    		var input = document.createElement('input');
    		input.setAttribute('onclick',"sendOnOffWs(this)");
    		input.setAttribute('lbl',p.lbl);
    		input.setAttribute('id',name+":"+p.val);
    		input.setAttribute('name', k);
    		if (p.typ == "Rad") { 
    			input.setAttribute('type',"radio");
    		} else if (p.typ == "Rng") {
        		input.setAttribute('type',"text");
        		input.setAttribute('onclick',"getRange(this)");
        		div2.setAttribute('class', "txt btntxt");
        		div2.setAttribute('id', "lbl_"+name+":"+p.val);
    		} else {
    			lbl.setAttribute('class', "bar barchkbox");
    			input.setAttribute('type',"checkbox");
    		}
    		lbl.appendChild(div1);
    		lbl.appendChild(input);
    		lbl.appendChild(div2);
    		br = document.createElement('br');
    		fs.appendChild(br);
        } 
    }
    
}

function groups() {
	this.ctr = 0;
	this.properties = [];
	this.add = function(prop) {
		//alert("groups ctr"+this.ctr+" prop.lbl="+prop.lbl)
		this.properties[this.ctr] = prop;
		this.ctr++;
	}
}
function getRange(e) {
  var tdiv = document.getElementById("lbl_"+e.id);
  var rangeVal = 0;
  if (tdiv.textContent.length>0) {
    rangeVal = tdiv.textContent;
  }
  var popup = document.getElementById("popup");
  if (popup!=null) {
    popup.remove()
  }
  var divRange = document.createElement('div');
  divRange.setAttribute('class', "init popup");
  divRange.setAttribute('id', "popup");
  divRange.setAttribute('onclick','closeIt()');
  divRange.innerHTML = "<label class='rng barrng'><div class='lbl'>"+e.getAttribute("lbl")+
    "</div><div class='slider'><input type='range' id='"+ e.id + "_" + 
    "rng' parent='"+e.id+"' min='"+e.getAttribute("min")+"' max='"+e.getAttribute("max")+
    "' onchange='sendRangeWs(this)' value="+rangeVal+"></div></label><br>";
  document.body.appendChild(divRange);

}
function sendOnOffWs(e) {
    var txt = document.getElementById("txt1");
    txt.value="checked " + e.id
	if (e.checked) {
	  websocket.send(e.id+"=1");
	} else {
	  websocket.send(e.id + "=0");
	}    
}
function sendRangeWs(e) {
  var txt = document.getElementById("txt1");
  txt.value="range " +e.id+ "=" +e.value;
  document.getElementById("popup").remove();
  var tdiv = document.getElementById("lbl_"+e.getAttribute("parent"));
  tdiv.textContent = e.value;
  websocket.send(e.getAttribute("parent")+ "=" +e.value);       
}
   function wsHandler() {
     websocket.onopen = function(evt) {
       for (i=1;i<=2;i++) {
         var hover = document.getElementById("tmp"+i);
         hover.parentNode.removeChild(hover);
       }
     };
     websocket.onclose = function(evt) {
       //alert("DISCONNECTED");
       status.innerHTML="DISCONNECTED";
     };
     websocket.onmessage = function(evt) {
         var txt = document.getElementById("txt2");
         txt.value=txt.value+"\n"+evt.data
         if (evt.data != "Connected") {
//           if (evt.data == "Disconnected") {
//             var status = document.getElementById('status');
//             status.innerHTML="Disconnected";
//           } else{
             var theItem = evt.data.split("=");
             var element = document.getElementById(theItem[0]);
             txt.value=txt.value+"\n"+"theItem[0]="+theItem[0]
             if (element.type == "checkbox") {
               if (theItem[1]==1)
                 element.checked=true;
               else
                 element.checked=false;
             }
             if (element.type == "text") {
               element = document.getElementById("lbl_"+theItem[0]);
               element.textContent=theItem[1];
             }
	         if (element.type == "radio") {
               element.checked=true;
             }
//           }
         } else {
           var status = document.getElementById('status');
           status.innerHTML="Synchronized";
         }
     };
     websocket.onerror = function(evt) {
       console.log("ERROR: " + evt.data);
     };
   } 
   window.addEventListener("load", wsHandler, false); 
function show() {
  var mainDiv = document.getElementById("main");
  var txt = document.getElementById("txt1");
  txt.value=mainDiv.innerHTML;
}
function closeIt(){
  document.getElementById("popup").remove();
}

function test() {
	var o = new Object();
	o.responseText="{typ:'Rad',lbl:'RED',val:'0007', grp:'g2'},{typ:'Rad',lbl:'GREEN',val:'0007', grp:'g2'},{typ:'Rad',lbl:'BLUE',val:'0007', grp:'g2'},{typ:'Btn',lbl:'STOP',val:'0009', grp:'g1'},{typ:'Rng',lbl:'Hue',val:'0011',min:'0',max:'360',grp:'g1'}, {typ:'Rng',lbl:'Saturation',val:'0012',min:'0',max:'360',grp:'g1'},{typ:'Rad',lbl:'H',val:'0007', grp:'g3'},{typ:'Rad',lbl:'S',val:'0007', grp:'g3'},{typ:'Rad',lbl:'V',val:'0007', grp:'g2'}"
	myFunction(o)
}