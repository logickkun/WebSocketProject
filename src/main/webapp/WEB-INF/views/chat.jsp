<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<meta charset="UTF-8">
	<title>chatting</title>
	<style>
		.chatting{
			background-color: #ccc;
			width: 500px;
			height: 500px;
			overflow: auto;
		}
		.chatting .me{
			text-align: right;
		}
		.chatting .others{
			text-align: left;
		}
		input{
			width: 330px;
			height: 25px;
		}
		#inputMsg{
		
			display: none;
		}
		.msgImg{
			width: 200px;
			height: 125px;
		}
		.youmsgImg{
			width: 200px;
			height: 125px;
		    display: block;
   		    margin-right: 30vh;
		}
		.clearBoth{
			clear: both;
		}
		.img{
			float: right;
		}
	</style>
</head>
<body>
	<div id="container" class="container">
	
		<h1>${roomName }의 채팅</h1>
		<input type="hidden" id="sessionId" value="">
		<input type="hidden" id="roomNumber" value="${roomNumber}">
		
		<div id="chatting" class="chatting">
		</div>
		
		<div id="inputNick">
			<table class="inputTable">
				<tr>
					<th>닉네임</th>
					<th><input type="text" name="userNick" id="userNick"></th>
					<th><button id="regiBtn">닉네임등록</button></th>
				</tr>
			</table>
		</div>
		<div id="inputMsg">
			<table class="inputTable">
				<tr>
					<th>메시지</th>
					<th><input id="chatMsg" placeholder="보내실 메시지를 입력하세요."></th>
					<th><button id="sendBtn">보내기</button></th>
				</tr>
				<tr>
					<th>파일업로드</th>
					<th><input type="file" id="fileUpload" multiple="multiple"></th>
					<th><button id="sendFileBtn">파일올리기</button></th>
				</tr>
			</table>
		</div>
	</div>
</body>
<script type="text/javascript">
$(function(){
	
	var WebSocketServer;
	
	var regiBtn = $("#regiBtn");
	var sendBtn = $("#sendBtn");
	var sendFileBtn = $("#sendFileBtn");
	var fileMetaData = {};
	
	//닉네임 등록 버튼 이벤트 구현.
	regiBtn.on("click", function(){
		
		var userNick = $("#userNick").val();
		
		if(userNick == null){
			
			alert("닉네임을 입력하라구...!");
			$("#userNick").focus();
			
		}else{
			
			WebSocketConnected();
			$("#inputNick").hide();
			$("#inputMsg").show();
		}
		
	});
	
	
	function WebSocketConnected(){
		
		WebSocketServer = new WebSocket("ws://" + location.host + "/chatting/" + $("#roomNumber").val());
		WebSocketRun();
	}
	
	function WebSocketRun(){
		
		WebSocketServer.onopen = function(data) {
			
			//소켓이 열렸는지 확인...
			console.log(data);
			
		}
		
		//핸들러에서 보내는 메세지 수신 로직...
		WebSocketServer.onmessage = function(data) {
			
			//값이 들어오는지 확인...
// 			console.log("####" + data);
			
			var msg = data.data;
			
// 			console.log("####" + msg);
			
			if(msg != null && msg.type != '') {
				
				var jsonMsg = JSON.parse(msg);
				
				//입장시..인풋 히든에 자신의 고유한 세션아이디를 저장...!
				if(jsonMsg.type == "enter") {
					
					var sessionId = jsonMsg.sessionId != null ? jsonMsg.sessionId : "";
					
					if(sessionId != "") {
						
						$("#sessionId").val(sessionId);
					}
					
				}else if(jsonMsg.type == "message") { // 메세지일때...
					
					
					//인풋 히든에 저장된 세션아이디와 비교해서 상대방인지 나인지 구분한다...
					if(jsonMsg.sessionId == $("#sessionId").val()) {
						
						$("#chatting").append("<p class='me'> My : " + jsonMsg.msg + "</p>");
						
					}else{
						
						$("#chatting").append("<p class='others'>" + jsonMsg.userNick + " : "  + jsonMsg.msg + "</p>");
						
					}
				
				}else if(jsonMsg.type == "fileUpload"){ // 파일 업로드일때...
					
					// 파일과 같이 보낸 정보를 전역 변수로 파일 메타데이터에 저장...! 
					fileMetaDate = jsonMsg;
					
				}
				
				//msg안에 Blob 타입으로 들어오니... if문 밖에다 선언...
			}else{
				
				//그 메타데이터를 이용해서 누군지 확인..!
				var url = URL.createObjectURL(new Blob([msg]));
				
				if(fileMetaDate.sessionId == $("#sessionId").val()){

					$("#chatting").append("<p class='me'>나 :<div class='img'><img class='msgImg' src="+url+"></div><div class='clearBoth'></div></p>");
					
				}else{
					
					$("#chatting").append("<p class='others'>"+ fileMetaDate.userNick +"<div class='img'><img class='youmsgImg' src="+url+"></div><div class='clearBoth'></div></p>");
					
				}				
				
			}
			
		}
		
		//sendBtn 클릭 이벤트...
		sendBtn.on("click", function(){
			
	        sendMsg();
	        
	    });
		
		//DOM 객체에서 ENTER를 감지하면 sendMsg() 메서드 호출...!
		$(document).on("keypress", function(e) {
			
			if(e.which == 13) {
				
				sendMsg();
				
			}			
			
		});
		
	}
	
	function sendMsg() {
		
		var sendObj = {
				
				type : "message",
				roomNumber: $("#roomNumber").val(),
				sessionId : $("#sessionId").val(),
				userNick : $("#userNick").val(),
				msg : $("#chatMsg").val()
				
		}
		
		WebSocketServer.send(JSON.stringify(sendObj));
		$("#chatMsg").val("");
		
	}
	
	
	sendFileBtn.on("click", function(){
		
		var files = $("#fileUpload")[0].files;
		
		if(files.length > 0) {
			
			$.each(files, function(idx, file) {
				
				var fileReader = new FileReader();
				
				fileReader.onload = function(e) {
					
					var arrayBuffer = e.target.result;
					var param = {
							
						type : "fileUpload",
						file : file,
						roomNumber : $("#roomNumber").val(),
						sessionId : $("#sessionId").val(),
						userNick : $("#userNick").val()
							
					};
					
					WebSocketServer.send(JSON.stringify(param));
					WebSocketServer.send(arrayBuffer);
					
				};
				
				fileReader.readAsArrayBuffer(file);
				
			});
			
		}else{
			
			alert("왜 대체 파일을 선택안하고 누르는거지..?");
			
		}
	});
});
</script>
</html>