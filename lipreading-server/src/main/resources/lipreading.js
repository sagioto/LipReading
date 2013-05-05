
var isRecording = false;
var sample;
var sampleId;

var classifierUrl = "/samples";


var recordButton = document.querySelector("#rec"); 
var output = document.querySelector('#output');
var video = document.querySelector("#vid"); 
var canvas = document.querySelector('#canvas');
var ctx = canvas.getContext('2d');
var pointsCanvas = document.querySelector('#points');
var pointsCtx = pointsCanvas.getContext('2d');
var audio = document.querySelector('#audio');
var localMediaStream = null;
var ws = new WebSocket("ws://192.168.0.101:9999");

var width = 320;
var height = 240;
canvas.width = width;
canvas.height = height;
points.width = width;
points.height = height;

ws.onopen = function () {
	console.log("Openened connection to websocket");
}
ws.onmessage = function (msg) {
		pointsCtx.clearRect(0, 0, points.width, points.height);
		pointsCtx.fillStyle="#00FF00";
		if(msg.data !== "null"){
			var coordinates = msg.data.split(",");
			coordinates.pop();
			if(isRecording){
                sample.matrix.push({"item" : coordinates});
            }
            var coords = [];
            coords.length = 8;
			for(var i = 0; i < 8; i++) { coords[i] = parseInt(coordinates[i]); }
			for(var i = 0; i < 8; i +=2){
				var x = coords[i];
				var y = coords[i + 1];
				pointsCtx.fillRect(x - 2, y - 2, 4, 4);
			}
		}
		
}


//flip the video
ctx.translate(canvas.width, 0);
ctx.scale(-1, 1);

var onCameraFail = function (e) {
    console.log('Camera did not work.', e);
};

function snapshot() {
    if (localMediaStream) {
        ctx.drawImage(video, 0, 0);
		data = canvas.toDataURL('image/jpeg', 1.0);
        newblob = dataURItoBlob(data);
		ws.send(newblob);
    }
}

navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia;
window.URL = window.URL || window.webkitURL;
navigator.getUserMedia({video:true}, function (stream) {
    video.src = window.URL.createObjectURL(stream);
    localMediaStream = stream;
}, onCameraFail);

//start video capture	
setInterval(snapshot, 60);

function record() {
	isRecording = !isRecording;
	if(isRecording){
		recordButton.innerHTML = '<img src="img/stop.png" />';
		recordButton.title = "Stop";
		output.style.display = 'none';
		sample = {
			id:"web-" + new Date().getTime(),
			matrix: [],
			width: width,
			height: height
		}
	} else {
		recordButton.innerHTML = '<img src="img/record.png" />';
		recordButton.title = "Record";
		sample.originalMatrixSize = sample.matrix.length;
		var xmlhttp = new XMLHttpRequest();
		xmlhttp.onreadystatechange = function() {
			if (xmlhttp.readyState==4 && xmlhttp.status==200) {
				var ans = xmlhttp.responseText.split(",");
				output.innerHTML = ans[0];
				sampleId = parseInt(ans[1]);
				output.style.display = 'inline';
				audio.src = "http://translate.google.com/translate_tts?tl=en&q=" + 
					encodeURIComponent(ans[0].toLowerCase());
				audio .play();
			}
		};
		xmlhttp.open("POST", classifierUrl, true);
		xmlhttp.setRequestHeader("Content-type","application/json");
		xmlhttp.setRequestHeader("training","false");
		xmlhttp.send(JSON.stringify(sample));
	}
}

function dataURItoBlob(dataURI) {
    var binary = atob(dataURI.split(',')[1]);
    var array = [];
    for(var i = 0; i < binary.length; i++){
        array.push(binary.charCodeAt(i));
    }
    return new Blob([new Uint8Array(array)], {type: 'image/jpeg'});
}