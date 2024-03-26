package kr.or.ddit.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.or.ddit.mapper.IChatMapper;
import kr.or.ddit.service.IChatService;
import kr.or.ddit.vo.ChatLogVO;

@Service
public class ChatServiceImpl implements IChatService{
	
	@Autowired
	private IChatMapper chatMapper;

	@Override
	public void insert(ChatLogVO clv) {
		
		chatMapper.insert(clv);
		
	}
	
}
