package kr.or.ddit.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import kr.or.ddit.handler.WebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer{
	
	
	@Autowired
	private WebSocketHandler webSocketHandler;
	
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		
		/*	
		 *	WebSocketConfigurer 인터페이스를 implements해서 오버라이드를 하고
		 *	addHandler메서드를 불러와서 핸들러를 파라미터로 설정하고 WS주소를 설정한다.
		 *  처음에 시도했던 xml방식의 config로 하면 웹소켓 주소를 단 하나밖에 못쓰는 상황이 발생하여
		 *  Java Config로 시도
		 */ 
		
		
		registry.addHandler(webSocketHandler, "/chatting/{roomNumber}");
		
	}
	
	//파일 전송 용량 설정...바이너리.. 이미지나 파일을 받으려면 필수적으로 용량을 설정해두어야함..! 아니면 소켓이 닫힘..!
	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
	ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
	container.setMaxTextMessageBufferSize(500000);
	container.setMaxBinaryMessageBufferSize(500000);
	return container;
	}

}
