package kr.or.ddit.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.or.ddit.vo.ChatRoomVO;

@Controller
public class ChatController {
	
	
	List<ChatRoomVO> roomList = new ArrayList<ChatRoomVO>();
	private static int roomNumber = 0;
	
	@GetMapping("/chat")
	public String chat() {

		return "chat";
	
	}
	
	@GetMapping("/chatList")
	public String chatList() {
		
		return "list";
		
	}
	
	//비동기로 방 목록을 불러온다...!
	@PostMapping("/getListRoom")
	public ResponseEntity<List<ChatRoomVO>> getListRoom(HashMap<Object, Object> params) {
		
		
		
		return new ResponseEntity<List<ChatRoomVO>>(roomList, HttpStatus.OK);
		
	}
	
	//비동기로 방을 생성..!
	@PostMapping("/createRoom")
	public ResponseEntity<List<ChatRoomVO>> createRoom(@RequestParam HashMap<Object, Object> params) {
		
		//클라이언트에서 보낸 방 이름
		String rName = (String) params.get("roomName");
		
		if(rName != null && !rName.trim().equals("")) {
			
			
			ChatRoomVO chatRoomVO = new ChatRoomVO();
			chatRoomVO.setRoomNumber(++roomNumber);
			chatRoomVO.setRoomName(rName);
			roomList.add(chatRoomVO);
			
		}
		
		return new ResponseEntity<List<ChatRoomVO>>(roomList, HttpStatus.OK);
		
	}
	
	
	
	
	// 방입장...
	@GetMapping("/enterTheRoom")
	public String enterTheRoom(@RequestParam HashMap<Object, Object> params, Model model) {
		
		String goPage = "";
		
		int rNumber = Integer.parseInt((String) params.get("roomNumber"));
		
		List<ChatRoomVO> newList = roomList.stream().filter(o ->o.getRoomNumber() == rNumber).collect(Collectors.toList());
		
		if(newList != null && newList.size() > 0) {
			
			model.addAttribute("roomName", params.get("roomName"));
			model.addAttribute("roomNumber", params.get("roomNumber"));
			
			goPage =  "chat";
			
		}else {
			
			goPage =  "room";
			
		}
		
		return goPage;
		
	}
	
}
