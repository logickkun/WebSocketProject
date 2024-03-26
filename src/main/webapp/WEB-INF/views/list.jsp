<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<div class="container">
		<h1>채팅방</h1>
		<div id="roomContainer" class="roomContainer">
			<table id="roomList" class="roomList"></table>
		</div>
		<div>
			<table class="inputTable">
				<tr>
					<th>방 제목</th>
					<th><input type="text" name="roomName" id="roomName"></th>
					<!-- Ver 2.0 -->
					<th><input type="text" name="roomId" id="roomId"></th>
					<th><button id="createRoomBtn">방 만들기</button></th>
				</tr>
			</table>
		</div>
	</div>
</body>
<script type="text/javascript">
$(function(){
	
	var WebSocketServer;
	
	var enter = $("#enter");
	var createRoomBtn = $("#createRoomBtn");
	
	//들어오자마자 비동기로 방 리스트를 조회...!
	getListRoom();
	createRoom();
	
	function getListRoom() {
		
		ajaxConnecter("/getListRoom", "" , "post", function(result) {
			
			createChatRoom(result);
			
		});
		
	}
	
	function createChatRoom(result) {
		
		if(result != null) {
			
			var chatTable = "<tr><th>순서</th><th>방이름</th><th class'enter'></th></tr>";
			
			result.forEach(function(info, index) {
				
				var rName = info.roomName.trim();
				var rNumber = info.roomNumber;
				
				chatTable += "<tr>" +
									"<td>" + (index + 1) + "</td>" +				
									"<td>" + rName + "</td>" +				
									"<td><button type='button' id='enter' value='" + rNumber + "," + rName + "' >입장</button></td>" +				
							 "</tr>"
			});
			
			//empty() 메서드는 기존에 존재하던 방 목록을 지우고 새로 불러온다 (초기화를 함)
			$("#roomList").empty().append(chatTable);
			
		}
		
	}
	
	function createRoom() {
		
		createRoomBtn.on("click", function(){
			
			var roomName = $("#roomName").val();
			
			//Ver 2.0
			var roomId = $("#roomId").val();
			
			var obj = {
					
					roomName : roomName

			};
			
			ajaxConnecter("/createRoom", obj,"post", function(result){
				
				createChatRoom(result);
				
			});
			
			$("#roomName").val("");
			
		});	
	}
	
	//비동기적으로 서버에 요청하는 AJAX 함수...콜백 파라미터로 으로 다시 되돌려받음..! 유용한기능...!
	function ajaxConnecter(url, param, type, callback, contentType) {
		
		$.ajax({
			
			url : url,
			data : param,
			type : type,
			contentType : contentType != null ? cotentType : 'application/x-www-form-urlencoded; charset=UTF-8',
			success : function(res) {
				
				callback(res);
				
			},
			error : function(res) {
				
				callback(res);
				
			}
			
		});
	}
	
	
	//방 입장버튼 이벤트..!
	$("#roomList").on("click", "#enter", function() {
		
	    var rInfo = $(this).val().trim();
	    
	    var rNumber = rInfo.split(",")[0];
	    
	    var rName = rInfo.split(",")[1];
	    
	    enterRoom(rNumber, rName);		
	    
	});

	
	
	function enterRoom(rNumber, rName) {
		
		location.href = "/enterTheRoom?roomName=" + rName + "&" + "roomNumber=" + rNumber;
	}
	
});
</script>
</html>