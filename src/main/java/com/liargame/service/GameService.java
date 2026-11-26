package com.liargame.service;

import org.springframework.stereotype.Service;

import com.liargame.repository.GameRepository;

@Service
public class GameService {

	private GameRepository gameRepository;
	private RoleAssigner roleAssigner;
	private ChatService chatService;
	private PickRandom pickRandom;
	
	public GameService(GameRepository gameRepository, RoleAssigner roleAssigner, ChatService chatService, PickRandom pickRandom) {
		this.gameRepository = gameRepository;
		this.roleAssigner = roleAssigner;
		this.chatService = chatService;
		this.pickRandom = pickRandom;	
	}
	
	public void setGame(Game game) throws SQLException{
		
	}
}
