/*
 * mainhtml.h
 * The main html used by the captive portal, main config and ota_setup.
 * This also contains the default template for the html pages that each device will display.
 *
 *  Created on: Mar 11, 2017
 *      Author: cels
 */

#ifndef MAINHTML_H_
#define MAINHTML_H_

const char DEFAULT_HTML1[] PROGMEM = R"=====(
<!DOCTYPE html>
<head>
  <title>Home Symphony</title>
<style>
.disable {
    position: absolute;
    width: 100%;
    height: 100%;
    z-index: 98;
    background-color: gray;
    opacity: 0.3;
}
.invisible {
    position: relative;
    z-index: 99;
    background-color: red;
}
.init {
  position: absolute;
  top: 100px;
  left: 50%;
  transform: translate(-50%, 0);
  background-color: white;
  color: blue;
  width: 70%;
  padding: 30px 30px;
  border: 3px solid green;
  text-align: center;
  z-index: 99;
  font-family: Arial;
  font-weight: bold; 
  font-size: 1.5em;
}
body {
  margin: 0;
  padding: 0;
  font-family: 'PT Sans', sans-serif;
  font-size: 1.3em;
  font-weight: bold;
  color: #fff;
  background-color: black;
}
.section {
  padding: 100px;
  padding-left: 150px;
}
.section input[type="radio"],
.section input[type="checkbox"]{
  display: none;
}
.container {
  margin-bottom: 10px;
}
.container label {
  position: relative;
}

/* Base styles for spans */
.container span::before,
.container span::after {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  margin: auto;
}

/* Radio buttons */
.container span.radio:hover {
  cursor: pointer;
}
.container span.radio::before,
.container span.checkbox::before {
  left: -52px;
  width: 45px;
  height: 25px;
  background-color: #A8AAC1;
  border-radius: 50px;
}
.container span.radio::after,
.container span.checkbox::after {
  left: -49px;
  width: 17px;
  height: 17px;
  border-radius: 10px;
  background-color: #6C788A;
  transition: left .25s, background-color .25s;
}
input[type="radio"]:checked + label span.radio::after,
input[type="checkbox"]:checked + label span.checkbox::after {
  left: -27px;
  background-color: #EBFF43;
}
input[type=range] {
  top: -40px;
  -webkit-appearance: none;
  width: 80%;
  background: none;
}
input[type=range]:focus {
  outline: none;
}
input[type=range]::-webkit-slider-thumb {
  box-shadow: 1px 1px 1px #000000, 0px 0px 1px #0d0d0d;
  border-radius: 34px;
  height: 17px;
  width: 17px;
  background: #6C788A;
  cursor: pointer;
  -webkit-appearance: none;
//  margin : 4px;
}
input[type=range]::-webkit-slider-runnable-track {
  width: 100%;
  height: 25px;
  border-radius: 25px;
  cursor: pointer;
  animate: 0.2s;
  box-shadow: 1px 1px 1px black, 0px 0px 1px #0d0d0d;
  background: #A8AAC1;
  padding: 4px;
}
input[type=range]:focus::-webkit-slider-runnable-track {
  background: #A8AAC1;
}
</style>
</head>
<script type="text/javascript">
  var wsUri = "ws://"+location.hostname+":81/";
  websocket = new WebSocket(wsUri, ['arduino']);
function sendOnOffWs() {
	var e = event.target || event.srcElement;
	if (e.checked) {
	  websocket.send(e.id+"=1");
	} else {
	  websocket.send(e.id + "=0");
	}    
}
function sendSelected() {
	var e = event.target || event.srcElement;
        websocket.send(e.id+"="+e.value);
}
function sendRangeWs() {
  var e = event.target || event.srcElement;
  websocket.send(e.id+ "=" +e.value);       
}
   function wsHandler() {
     websocket.onopen = function(evt) {
       for (i=1;i<=2;i++) {
         var hover = document.getElementById("tmp"+i);
         hover.parentNode.removeChild(hover);
       }
     };
     websocket.onclose = function(evt) {
       alert("DISCONNECTED");
     };
     websocket.onmessage = function(evt) {
         var txt = document.getElementById("txt1");
         txt.value=evt.data
         if (evt.data != "Connected") {
           var theItem = evt.data.split("=");
           var element = document.getElementById(theItem[0]);
           if (element.type == "checkbox") {
             if (theItem[1]==1)
               element.checked=true;
             else
               element.checked=false;
           }
           if (element.type == "range") {
             element.value=theItem[1];
           }
	   if (element.type == "radio") {
             element.checked=true;
           }
         }
     };
     websocket.onerror = function(evt) {
       console.log("ERROR: " + evt.data);
     };
   } 
   window.addEventListener("load", wsHandler, false); 
  </script>
  <div id="tmp1" class='invisible'><div class='init'>Connecting...</div></div>
  <div id="tmp2" class='disable'></div>
	<div data-role="header"  align="center">
	  <h3>Life in Harmony</h3>
<fieldset><legend><h3>$AAA</h3></legend>
      $VAR
</fieldset>
	</div>
<div align="center">
For Debug Only:<input type="text" id="txt1"><br>
</div>
</html>
)=====";

const char DEFAULT_HTML2[] PROGMEM = R"=====(
<fieldset><legend><h3>$AAA</h3></legend>
      $VAR
</fieldset>
	</div>
<div align="center">
For Debug Only:<input type="text" id="txt1"><br>
</div>
</html>
)=====";

const char ADMIN_HTML[] PROGMEM = R"=====(
<html>
<head>
<style>
.center {
    text-align: center;
}
a:link, a:visited {
    background-color: white;
    color: black;
    border: 2px solid green;
    padding: 10px 20px;
    text-align: center;
    text-decoration: none;
    display: inline-block;
}
a:hover, a:active {
    background-color: green;
    color: white;
}
input[type=text], select {
    width: 100%;
    padding: 2px 5px;
    margin: 8px 0;
    box-sizing: border-box;
    border: 3px solid #ccc;
    -webkit-transition: 0.5s;
    transition: 0.5s;
    outline: none;
}
input[type=text]:focus {
    border: 3px solid #555;
    background-color: lightblue;
}
input[type=submit]{
    background-color: #4CAF50;
    border: none;
    color: white;
    padding: 16px 32px;
    text-decoration: none;
    margin: 4px 2px;
    cursor: pointer;
}
div.tab {
    width: 80%;
    overflow: hidden;
    border: 1px solid #ccc;
    background-color: #f1f1f1;
}
div.tab button {
    background-color: inherit;
    float: left;
    border: 1px solid #ccc;
    outline: none;
    cursor: pointer;
    padding: 14px 16px;
    transition: 0.3s;
    font-size: 12px;
}
div.tab button:hover {
    background-color: #ddd;
}
div.tab button.active {
    background-color: #4CAF50;
}
.tabcontent {
    display: none;
    padding: 6px 12px;
    border: 1px solid #ccc;
    border-top: none;
}

fieldset {
  width: 70%;
}
</style>
</head>
<body>

<h2>Device Setup</h2>

<div class="tab">
  <button class="tablinks" onclick="openTab(event, 'wifi')" id="defaultOpen">Configure Wifi</button>
  <button class="tablinks" onclick="openTab(event, 'mqtt')">Configure MQTT</button>
  <button class="tablinks" onclick="openTab(event, 'device')">Manage Device</button>
</div>

<div id="wifi" class="tabcontent">
<h3>Configure Wifi Settings</h3>
<form action='/handleWifiConfig' method='get' id='form'>
<fieldset><legend><h3>Wifi Settings:</h3></legend>
SSID:<br><input type='text' name='ssid'><br>
Pass Phrase:<br><input type='text' name='pass'><br>
<select id='apmode' name='apmode'>
<option value='1'>Station/AP</option>
<option value='2'>Station+AP</option>
<option value='3'>Station</option>
</select><br>
<input type='submit' value='Submit'>
</form>
</div>

<div id="mqtt" class="tabcontent">
<h3>Configure Mqtt Settings</h3>
<form action='/handleMqttConfig' method='get' id='form'>
<fieldset><legend><h3>Mqtt Details:</h3></legend>
IP:<br><input type='text' name='ip'><br>
Port:<br><input type='text' name='port'><br>
<input type='submit' value='Submit'>
</fieldset>
</form>
</div>

<div id="device" class="tabcontent">
<h3>Manage Device</h3>
<form action='/handleRegister' method='get' id='form'>
<fieldset><legend><h3>Manual Registration</h3></legend>
Name:<br><input type='text' name='pName'><br>
Room:<br><input type='text' name='room'><br>
Product ID:<br>
<select name="pID">
<option value="0000">system</option>
<option value="0001">ZenLight</option>
<option value="0002">ZenLightRGB</option>
<option value="0003">ZenSocket</option>
<option value="0004">ZenTemp2</option>
<option value="0005">MediaPlayer</option>
<option value="0006">TFTSwitch</option>
<option value="0007">Test1</option>
<option value="0008">PirSensor</option>
</select>
<br>
<input type='submit' value='Register'>
</fieldset>
</form>
<form action='/doUnRegister' method='get' id='form2'>
<fieldset><legend><h3>Manually UnRegister</h3></legend>
<input type='submit' value='UnRegister'>
</fieldset>
</form>
</div>

<script>
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
}

// Get the element with id="defaultOpen" and click on it
document.getElementById("defaultOpen").click();
</script>
     
</body>
</html> 
)=====";
#endif /* MAINHTML_H_ */
