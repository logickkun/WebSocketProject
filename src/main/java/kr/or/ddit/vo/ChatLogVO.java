package kr.or.ddit.vo;

import java.sql.Date;

import lombok.Data;

@Data
public class ChatLogVO {
	
	private int chatNo;
	private String userNick;
	private String msg;
	private Date chatDate;
	private String type;
	private String roomNumber;
	private String sessionId;
}
