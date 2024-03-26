package kr.or.ddit.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.or.ddit.service.IChatService;
import kr.or.ddit.vo.ChatLogVO;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler{
	
	
	@Autowired
	private IChatService chatService;
	
	
	// 웹소켓 세션을 담아둘 맵
//	HashMap<String, WebSocketSession> sessionMap = new HashMap<>();
	
	//방 리스트 소켓. 리스트의 의미는 방의 수를 의미...! 
	List<HashMap<String, Object>> listSeesionMap = new ArrayList<HashMap<String,Object>>();
	static int fileUploadIdx = 0;
	static String fileUploadSession = "";

	@SuppressWarnings("unchecked")
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		
		boolean flag = false;
		
		//세션의 주소(각 방마다 고유한 Url을 꺼냄)
		String url = session.getUri().toString();
		
		//확인...
		log.info(url);
		
		
		//방번호를 꺼낸다...!
		String rNumber = url.split("/chatting/")[1];
		
		
		//방번호에 관계없이 소켓에 접속한 유저들의 수..!
		int index = listSeesionMap.size();
		
		//그 유저들의 다 검사해서...
		if(listSeesionMap.size() > 0) {
			
			for(int i = 0; i < listSeesionMap.size(); i++) {
				
				//주소에 꺼낸 방번호와 각 세션의 방번호가 같은 사람이있으면.. 멈추고 플래그를 true로 설정..
				String roomNumber = (String) listSeesionMap.get(i).get("roomNumber");
				
				if(roomNumber.equals(rNumber)) {
					
					flag = true;
					index = i;
					break;
					
				}
				
			}
			
		}
		
		
		// true 설정했다면 ..방이 존재한다는 의미... 세션만 추가...!(방안에 사람을 입장시킴)
		if(flag) {
			
			HashMap<String, Object> map = listSeesionMap.get(index);
			map.put(session.getId(), session);
			listSeesionMap.add(map);
			
		}else { // 여전히 일치하는 방번호가 없고 false라면 방번호와 세션을 추가...!(방 생성..)
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("roomNumber", rNumber);
			map.put(session.getId(), session);
			listSeesionMap.add(map);
			
		}
		
		
		//타입을 설정하여 클라이언트로 세션아이디를 보냄...!
		JSONObject obj = new JSONObject();
		obj.put("type", "enter");
		obj.put("sessionId", session.getId());
		session.sendMessage(new TextMessage(obj.toJSONString()));
		
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		
		//jackson-databind 라이브러리를 사용하여 VO를 통해 바로 json으로 파싱해준다...!!!
		ObjectMapper objectMapper = new ObjectMapper();
		ChatLogVO chatSendInfo = objectMapper.readValue(message.getPayload(), ChatLogVO.class);
		
//		payload를 통해서 받은 메세지를 JSON으로 파싱 해준다.
//		String msg = message.getPayload();
//		JSONParser jsonParser = new JSONParser();
//		JSONObject jsonMsg = (JSONObject) jsonParser.parse(msg);
		
		String json = objectMapper.writeValueAsString(chatSendInfo); // vo를 json 형식으로 파싱..!
		
//		log.info(jsonMsg.toJSONString());
		log.info(json);
		
		
		
		
//		//채팅로그저장...
//		String userId = (String) jsonMsg.get("userNick");
//		String userContent = (String) jsonMsg.get("msg");
//		
//		log.info(userId);
//		log.info(userContent);
//		
//		ChatLogVO clv = new ChatLogVO();
//		clv.setUserId(userId);
//		clv.setUserContent(userContent);
//		
//		
		chatService.insert(chatSendInfo);
		
		
		
		//클라이언트에서  인풋 히든에 담아서 보낸 방번호와 타입을 페이로드에서 꺼냄.
		String rNumber = (String) chatSendInfo.getRoomNumber();
//		String msgType = (String) jsonMsg.get("type");
		
		//listSessionMap에 넣어줄 temp 선언...!
		HashMap<String, Object> temp = new HashMap<String, Object>();
		
		if(listSeesionMap.size() > 0) {
			
			for(int i = 0; i < listSeesionMap.size(); i++) {
				
				String roomNumber = (String) listSeesionMap.get(i).get("roomNumber");
				
				
				// 일치 하는 방번호가있다면 그 방의 세션을 temp에 담고..(ex)2번방을 세션에 담음..
				if(roomNumber.equals(rNumber)) {
					
					temp = listSeesionMap.get(i);
					fileUploadIdx = i;
//					fileUploadSession = (String) jsonMsg.get("sessionId");
					break;
					
				}
				
			}
			
				// 여기엔 2번방안에 있는 사람들이 담김..
				for(String k : temp.keySet()) {
					
					//새롭게 생성된 방에 들어온 사람이 [0]번째의 key값이 "roomNumber"인 경우가 있다 그걸 건너뛰고. 필요한건 key값의 1..2...3..의 방의 번호)
					if(k.equals("roomNumber")) {
						
						continue;
						
					}				
					WebSocketSession webSocketSession = (WebSocketSession) temp.get(k);
					
					webSocketSession.sendMessage(new TextMessage(json));
					
				}
			
		}
		
	}
	//파일 관련...
	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
		
		//파일 저장해서.. 다운로드 하는 기능도 추가할수있다...!..현재는 출력만..!
		
		ByteBuffer byteBuffer = message.getPayload();
		
		HashMap<String, Object> temp = listSeesionMap.get(fileUploadIdx);
		
		for(String k : temp.keySet()) {
			
			if(k.equals("roomNumber")) {
				continue;
			}
			
			WebSocketSession webSocketSession = (WebSocketSession) temp.get(k);
			try {
				webSocketSession.sendMessage(new BinaryMessage(byteBuffer));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		
		//소켓이 종료되면 해당 세션값들을 찾아서 지운다.
		if(listSeesionMap.size() > 0) { 
			for(int i=0; i<listSeesionMap.size(); i++) {
				listSeesionMap.get(i).remove(session.getId());
			}
		}
	}

}
