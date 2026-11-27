package com.liargame.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.liargame.dto.KeywordDto;
import com.liargame.messaging.GameMessenger;
import com.liargame.model.Game;
import com.liargame.model.GameMode;
import com.liargame.model.GamePhase;
import com.liargame.model.Player;
import com.liargame.model.PlayerStatus;
import com.liargame.model.Role;
import com.liargame.repository.GameRepository;

@Service
public class GameService {

	private GameRepository gameRepository;
	private RoleAssigner roleAssigner;
	private ChatService chatService;
	private PickRandom pickRandom;
	private GameMessenger messenger;
	private final Map<Integer, TurnTimer> timers = new ConcurrentHashMap<>();

	public GameService(GameRepository gameRepository, RoleAssigner roleAssigner, ChatService chatService,
			PickRandom pickRandom, GameMessenger messenger) {
		this.gameRepository = gameRepository;
		this.roleAssigner = roleAssigner;
		this.chatService = chatService;
		this.pickRandom = pickRandom;
		this.messenger = messenger;
	}

	public void setGame(Game game) throws SQLException {
		game.setMaxRound(GameConfig.getMaxRound(game.getPlayers().size()));
		game.setLiars(roleAssigner.assign(game.getPlayers()));

		KeywordDto selected = pickRandom.pick(game.getDifficulty().name());
		game.setCategory(selected.getCategory());
		game.setKeyword(selected.getKeyword());

		game.setFoolKeyword(null);
		if (game.getGameMode() == GameMode.FOOL) {
			game.setFoolKeyword(pickRandom.pickOtherKeyword(selected));
		}

		// TODO: LiarService 연결. 해당 게임 기록 초기화
	}

	private List<Player> getAlivePlayers(Game game) {
		List<Player> alive = new ArrayList<>();
		for (Player p : game.getPlayers()) {
			if (p.getStatus() == PlayerStatus.ALIVE)
				alive.add(p);
		}
		return alive;
	}

	private boolean isLiarWinByCount(Game game) {
		// TODO: LiarService에서 남은 라이어 수 가져오기

		int liarCount = 0;
		for (int i = 0; i < game.getLiars().size(); i++) {
			Player liar = game.getLiars().get(i);

			if (liar.getStatus() == PlayerStatus.ALIVE) {
				liarCount++;
			}

		}
		int citizenCount = getAlivePlayers(game).size() - liarCount;
		return liarCount > 0 && citizenCount == liarCount;
	}

	public boolean isMaxRoundover(int round, int maxRound) {
		if (round >= maxRound)
			return true;
		else
			return false;
	}

	private void changePhase(Game game, GamePhase nextPhase) {
		game.setPhase(nextPhase);

		messenger.broadcast(game.getRoomId(), "PHASE", Map.of("phase", nextPhase.name()));
	}

	private boolean isSpectator(Game game, Player player) {
		if (player.getStatus() == PlayerStatus.DIED) {
			messenger.sendTo(player, "NOTICE", game.getRoomId(),
					Map.of("message", "탈락하여 관전 중입니다. 게임이 끝날 때까지 채팅, 투표를 할 수 없습니다."));
			return true;
		}
		return false;
	}

	private void nextRound(Game game) {
		if (isLiarWinByCount(game)) {
			messenger.broadcast(game.getRoomId(), "NOTICE", Map.of("message", "남은 시민 수와 라이어 수가 같습니다."));
			finishGame(game, Role.LIAR);
			return;
		}

		if (isMaxRoundover(game.getRound(), game.getMaxRound())) {
			finishGame(game, Role.LIAR);
			return;
		}

		game.setRound(game.getRound() + 1);

		messenger.broadcast(game.getRoomId(), "ROUND_START", Map.of("round", game.getRound(), "maxRound",
				game.getMaxRound(), "message", game.getRound() + "라운드 시작!"));
		changePhase(game, GamePhase.CHATTING);
		chatService.startChat(game, getAlivePlayers(game));
		startTurn(game);
	}

	private void finishGame(Game game, Role winner) {
		stopTimer(game);
		changePhase(game, GamePhase.RESULT);

		String liarIds = "";
		for (int i = 0; i < game.getLiars().size(); i++) {
			Player liar = game.getLiars().get(i);
			liarIds += "Player" + liar.getId() + " ";
		}

		String winnerMessage;

		if (winner == Role.LIAR) {
			winnerMessage = "라이어 승리";
		} else {
			winnerMessage = "시민 승리";
		}

		String resultMessage = winnerMessage + " / 라이어: " + liarIds + "/제시어: " + game.getKeyword();

		messenger.broadcast(game.getRoomId(), "GAME_RESULT", Map.of("winner", winner.name(), "liarIds", liarIds,
				"keyword", game.getKeyword(), "message", "==== 게임 종료 ====\n" + resultMessage));
		// TODO: Roomservice 연결 후 방을 대기 상태로 변경

		for (int i = 0; i < game.getPlayers().size(); i++) {
			Player player = game.getPlayers().get(i);
			player.setChoosingRemain(true);

			messenger.sendTo(player, "REMAIN_CHOICE", game.getRoomId(), Map.of("message", "다시하기 또는 종료하기를 선택해주세요."));
		}
	}

	public void startTurn(Game game) {
		Player player = chatService.getTurnPlayer(game);

		startTimer(game,GameConfig.CHAT_TIME);

		String turnMessage = "Player" + player.getId() + "의 차례입니다.(" + GameConfig.CHAT_TIME + "초)";

		messenger.broadcast(game.getRoomId(), "TURN_START",
				Map.of("playerId", player.getId(), "seconds", GameConfig.CHAT_TIME, "message", turnMessage));
	}

	public void sendRoles(Game game) {
		for (Player p : game.getPlayers()) {
			p.setStatus(PlayerStatus.ALIVE);

			if (game.getGameMode() == GameMode.FOOL) {
				String playerKeyword;

				if (p.getRole() == Role.LIAR) {
					playerKeyword = game.getFoolKeyword();
				} else {
					playerKeyword = game.getKeyword();
				}

				messenger.sendTo(p, "ROLE_INFO", game.getRoomId(),
						Map.of("category", game.getCategory(), "keyword", playerKeyword));
			} else {
				if (p.getRole() == Role.LIAR) {
					messenger.sendTo(p, "ROLE_INFO", game.getRoomId(),
							Map.of("category", game.getCategory(), "role", p.getRole().name()));
				} else {
					messenger.sendTo(p, "ROLE_INFO", game.getRoomId(), Map.of("category", game.getCategory(), "role",
							p.getRole().name(), "keyword", game.getKeyword()));
				}
			}
		}
	}

	public void startGame(Game game) {
		synchronized (game) {
			if (game.getPhase() != GamePhase.WAITING) {
				return;
			}

			try {
				setGame(game);
			} catch (SQLException e) {
				e.printStackTrace();

				// TODO: RoomService를 통해 해당 방을 대기 상태로 변경

				changePhase(game, GamePhase.WAITING);

				messenger.broadcast(game.getRoomId(), "ERROR", Map.of("message", "제시어를 불러오지 못했습니다. 다시 시작해주세요."));
				return;
			}
			gameRepository.save(game);
			sendRoles(game);
			messenger.broadcast(game.getRoomId(), "NOTICE",
					Map.of("message", "이번 게임의 라이어는 " + game.getLiars().size() + "명입니다."));

			game.setRound(0);
			nextRound(game);
		}
	}

	public void handleChat(Integer roomId, Player player, String text) {
		if (roomId == null) {
			return;
		}

		Game game = gameRepository.findByRoomId(roomId);

		if (game == null) {
			return;
		}

		synchronized (game) {
			if (!game.getPlayers().contains(player)) {
				return;
			}

			if (isSpectator(game, player)) {
				return;
			}

			if (game.getPhase() != GamePhase.CHATTING) {
				return;
			}

			if (player.getStatus() != PlayerStatus.ALIVE) {
				return;
			}

			if (text == null || text.trim().isEmpty()) {
				return;
			}

			String errorMessage = chatService.handleChat(game, player, text);

			if (errorMessage != null) {
				messenger.sendTo(player, "ERROR", roomId, Map.of("message", errorMessage));

				return;
			}

			stopTimer(game);

			messenger.broadcast(roomId, "CHAT", Map.of("playerId", player.getId(), "text", text));
			boolean finished = chatService.nextTurn(game);

			if (finished) {
				chatEnd(game);
			} else {
				startTurn(game);
			}
		}
	}

	private void chatEnd(Game game) {
		startVote(game);
	}

	private void startVote(Game game) {
		List<Player> candidates = getAlivePlayers(game);
		List<Player> voters = new ArrayList<>();

		for (Player p : candidates) {
			if (!p.isBot()) {
				voters.add(p);
			}
		}

		// TODO: VoteService에 해당 게임의 투표자 후보 목록 전달 및 투표 기록 초기화
		changePhase(game, GamePhase.VOTING);

		List<Integer> candidateIds = new ArrayList<>();

		for (Player p : candidates) {
			candidateIds.add(p.getId());
		}

		messenger.broadcast(game.getRoomId(), "VOTE_START", Map.of("candidateIds", candidateIds, "seconds",
				GameConfig.VOTE_TIME, "message", "투표를 시작합니다! (" + GameConfig.VOTE_TIME + "초)"));
		startTimer(game, GameConfig.VOTE_TIME);
	}

	private void startLiarGuessing(Game game) {
		changePhase(game, GamePhase.LIAR_GUESSING);

		messenger.broadcast(game.getRoomId(), "LIAR_GUESS_START", Map.of("seconds", GameConfig.LIAR_GUESS_TIME,
				"message", "라이어가 모두 밝혀졌습니다! 라이어는 제시어를 맞혀주세요. (" + GameConfig.LIAR_GUESS_TIME + "초)"));
		startTimer(game, GameConfig.LIAR_GUESS_TIME);
	}

	public void handleVote(Integer roomId, Player voter, Integer targetId) {
		if (roomId == null) {
			return;
		}

		Game game = gameRepository.findByRoomId(roomId);

		if (game == null) {
			return;
		}

		synchronized (game) {
			if (!game.getPlayers().contains(voter)) {
				return;
			}

			if (isSpectator(game, voter)) {
				return;
			}

			if (game.getPhase() != GamePhase.VOTING) {
				return;
			}

			if (voter.getStatus() != PlayerStatus.ALIVE || voter.isBot()) {
				return;
			}

			if (targetId == null) {
				messenger.sendTo(voter, "ERROR", roomId, Map.of("message", "의심가는 플레이어 번호를 입력해주세요."));
				return;
			}

			// TODO: VoteService에서 중복 투표, 자기 투표, 후보 여부 검사 및 투표 저장
			// TODO: VoteService에서 해당 게임의 전체 투표 완료 여부 확인
			// TODO: 모두 투표했으면 해당 게임의 투표 타이머 중지
			// TODO: 해당 방에 전체 투표 완료 안내 전송
			// TODO: VoteService에서 최다 득표자 목록을 가져와 voteEnd로 전달
		}
	}

	public void handleLiarGuess(Integer roomId, Player player, String answer) {
		if (roomId == null) {
			return;
		}

		Game game = gameRepository.findByRoomId(roomId);

		if (game == null) {
			return;
		}

		synchronized (game) {
			if (!game.getPlayers().contains(player)) {
				return;
			}

			if (isSpectator(game, player)) {
				return;
			}

			if (game.getPhase() != GamePhase.LIAR_GUESSING) {
				return;
			}

			if (player.getRole() != Role.LIAR) {
				messenger.sendTo(player, "ERROR", roomId, Map.of("message", "라이어만 정답을 입력할 수 있습니다."));
				return;
			}

			if (answer == null || answer.trim().isEmpty()) {
				messenger.sendTo(player, "ERROR", roomId, Map.of("message", "정답을 입력해주새요."));
				return;
			}

			// TODO: LiarService에서 해당 플레이어의 정답 입력 여부 확인
			// TODO: 이미 입력했으면 개인 안내 전송 후 종료
			// TODO: 중복 입력이 아니면 해당 방에 추측 내용 전송
			// TODO: LiarService에서 입력 기록 저장 및 정답 판정
			// TODO: LIAR_WIN이면 finishGame(game, Role.LIAR) 호출
			// TODO: CITIZEN_WIN이면 finishGame(game, Role.CITIZEN) 호출
			// TODO: WAITING이면 다른 라이어의 답을 기다리는 안내 전송
		}
	}

	private void voteEnd(Game game, List<Player> topVoted) {
		stopTimer(game);
		startVoteResult(game, topVoted);
	}

	private void startVoteResult(Game game, List<Player> topVoted) {
		if (topVoted.size() != 1) {
			messenger.broadcast(game.getRoomId(), "NOTICE", Map.of("message", "동률. 다음 라운드 진행"));
			nextRound(game);
			return;
		}

		Player target = topVoted.get(0);
		messenger.broadcast(game.getRoomId(), "NOTICE", Map.of("message", "최다 득표자: Player" + target.getId()));

		// TODO: LiarService에서 지목된 플레이어의 라이어 여부 판정
		// TODO: 라이어면 해당 방에 라이어 발각 안내 전송
		// TODO: LiarService에서 발각 상태 변경 및 발각 기록 저장
		// TODO: 모든 라이어가 발각됐으면 startLiarGuessing(game) 호출
		// TODO: 발각되지 않은 라이어가 남았으면 nextRound(game) 호출
		// TODO: 시민이면 시민 안내 전송 후 DIED로 변경하고 nextRound(game) 호출

	}

	private void timeOut(Game game) {
		synchronized (game) {
			switch (game.getPhase()) {
			case CHATTING:
				messenger.broadcast(game.getRoomId(), "NOTICE", Map.of("message", "채팅 시간 종료!"));
				boolean finished = chatService.timeOut(game);

				if (finished) {
					chatEnd(game);
				} else {
					startTurn(game);
				}

				break;

			case VOTING:
				messenger.broadcast(game.getRoomId(), "NOTICE", Map.of("message", "투표 시간 종료!"));
				// TODO: VoteService에서 해당 게임의 최다 득표자 목록을 가져와 voteEnd(game, 최다 득표자 목록) 호출
				break;

			case LIAR_GUESSING:
				messenger.broadcast(game.getRoomId(), "NOTICE", Map.of("message", "정답 시간 종료!"));
				finishGame(game, Role.CITIZEN);
				break;

			default:
				break;
			}
		}
	}

	private void stopTimer(Game game) {
		synchronized (game) {
			TurnTimer timer = timers.remove(game.getRoomId());
			if (timer != null) {
				timer.stop();
			}
		}
	}

	private void startTimer(Game game, int seconds) {
		synchronized (game) {
			stopTimer(game);

			TurnTimer timer = new TurnTimer(new Runnable() {
				@Override
				public void run() {
					synchronized (game) {
						TurnTimer currentTimer = timers.get(game.getRoomId());
						if(currentTimer == null) {
							return;
						}
						if(!currentTimer.isCurrentThread()) {
							return;
						}
						
						timers.remove(game.getRoomId());
						timeOut(game);
					}
				}
			});
			
			timers.put(game.getRoomId(), timer);
			timer.start(seconds);
		}
	}

}
