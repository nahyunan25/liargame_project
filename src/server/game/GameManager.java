package server.game;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import common.GameConfig;
import common.GamePhase;
import common.Player;
import common.PlayerStatus;
import common.Role;
import server.room.Room;
import server.room.RoomManager;
import common.dto.KeywordDto;
import server.dao.KeywordDao;
import server.dao.DBConnector;
import common.GameMode;

//역할지정, 채팅, 시간, 투표, 판정
public class GameManager {
	// 게임 데이터 변수
	private List<Player> players;
	private List<Player> liars;
	private GamePhase phase; // 현재 단계
	private int round;
	private int maxRound;
	private String category;
	private String keyword;
	private String foolKeyword;
	private static final int START_PLAYERS = 4; // TODO: 방 연결 후 room의 인원으로 교체

	// 담당 클래스
	private RoleAssigner roleAssigner = new RoleAssigner();
	private ChatManager chatManager = new ChatManager();
	private VoteManager voteManager = new VoteManager();
	private LiarManager liarManager = new LiarManager();
	private TurnTimer turnTimer = new TurnTimer(this);
	private GameMessenger gameMessenger = new GameMessenger();
	private PickRandom pickRandom = new PickRandom(new KeywordDao(new DBConnector()));

	// 외부 연결
	private RoomManager roomManager;
	private Room room;

	/*
	 * 기존 코드 => 생성자 수정
	 * 
	 * // 생성자: RoomManager와 연동 public GameManager(RoomManager
	 * roomManager,List<Player> players) { this.roomManager = roomManager;
	 * this.players = players; chatManager.setGameManager(this);
	 * liarManager.setGameManager(this); }
	 */

	// 수정: Room 객체를 전달받음
	public GameManager(Room room, RoomManager roomManager) {
		this.room = room;
		this.roomManager = roomManager;
		chatManager.setGameManager(this);
		liarManager.setGameManager(this);
	}

	/*
	 * 11/22 삭제 //외부에서 호출 -> 담당자에게 넘기기만 public synchronized void join(Player player)
	 * { players.add(player); player.send("당신은 Player" + player.getId() + "입니다.");
	 * broadcast("Player" + player.getId() + " 입장 (" + START_PLAYERS + "/" +
	 * room.getPlayerCount() + ")");
	 * 
	 * if (players.size() == START_PLAYERS) { startGame(); } }
	 */
	/*
	 * 기존 코드 => 일부 기능을 다른 클래스로 이동
	 * 
	 * public synchronized void join(Player player) { players.add(player);
	 * 
	 * // 개인 메시지 => ClientHandler player.send("당신은 Player" + player.getId() +
	 * "입니다.");
	 * 
	 * // 입장 메시지 => RoomManager broadcast("Player" + player.getId() + " 입장 (" +
	 * players.size() + "/" + START_PLAYERS + ")");
	 * 
	 * // 게임 시작 => 방장 요청 시 시작 if (players.size() == START_PLAYERS) { startGame(); }
	 * }
	 */

	// ==============startGame==============

	public synchronized void startGame() {// 게임 시작
		// 수정: Room에 있는 플레이어 목록 복사
		this.players = new ArrayList<>(room.getPlayers());

		try {
			setGame();
		} catch (SQLException e) {
			e.printStackTrace();

			roomManager.returnToWaiting(room);
			changePhase(GamePhase.WAITING);
			broadcast("제시어를 불러오지 못했습니다. 다시 시작해주세요.");

			return;
		}
		sendRoles();
		liarManager.announceLiarCount();

		round = 0;
		nextRound();
	}

	public void setGame() throws SQLException { // 게임 시작 전 기본 세팅
		maxRound = GameConfig.getMaxRound(players.size());
		liars = roleAssigner.assign(players); // 라이어 선정 한번에

		KeywordDto selected = pickRandom.pick(room.getDifficulty().name());
		category = selected.getCategory();
		keyword = selected.getKeyword();

		foolKeyword = null;
		if (room.getGameMode() == GameMode.FOOL) {
			foolKeyword = pickRandom.pickOtherKeyword(selected);
		}

		liarManager.setLiarManager(liars, keyword);
		chatManager.setKeyword(keyword, foolKeyword);
	}

	public void sendRoles() {
		for (Player p : players) {
			p.setStatus(PlayerStatus.ALIVE); // 상태 alive로 모두 초기화

			if (room.getGameMode() == GameMode.FOOL) {
				String playerKeyword;
				if (p.getRole() == Role.LIAR) {
					playerKeyword = foolKeyword;
				} else {
					playerKeyword = keyword;
				}

				sendTo(p, "주제: " + category + " / 제시어: " + playerKeyword);
			} else {
				if (p.getRole() == Role.LIAR) {
					sendTo(p, "주제: " + category + " / 당신은 라이어입니다!");
				} else {
					sendTo(p, "주제: " + category + " / 제시어: " + keyword);
				}
			}
		}
	}

	// ===============handle_=================

	public synchronized void handleChat(Player player, String text) { // -> ChatManager의 handleChat과 연결
		if (isSpectator(player))
			return;
		if (phase != GamePhase.CHATTING)
			return;
		if (player.getStatus() != PlayerStatus.ALIVE)
			return; // 조건 2가지: CHATTING 상태, ALIVE상태

		chatManager.handleChat(player, text);
	};

	public synchronized void handleVote(Player voter, int targetID) { // -> VoteManager의 handleVote과 연결
		if (isSpectator(voter))
			return;
		if (phase != GamePhase.VOTING)
			return;
		if (voter.getStatus() != PlayerStatus.ALIVE || voter.isBot())
			return; // 조건 3가지: VOTING 상태, ALIVE상태, not bot

		voteManager.handleVote(voter, targetID);

		if (voteManager.isAllVoted()) {
			turnTimer.stop();
			broadcast("모두 투표하여 투표가 종료되었습니다.");
			voteEnd(voteManager.getTopVoted());
		}
		;
	};

	public synchronized void handleLiarGuess(Player player, String answer) {
		if (isSpectator(player))
			return;
		if (phase != GamePhase.LIAR_GUESSING)
			return;
		if (player.getRole() != Role.LIAR) {
			sendTo(player, "라이어만 정답을 입력할 수 있습니다.");
			return;
		}
		
		if(liarManager.hasGuessed(player)) {
			sendTo(player, "이미 정답을 입력했습니다.");
			return;
		}

		broadcast("Player" + player.getId() + "의 추측: " + answer);

		GuessResult result = liarManager.handleLiarGuess(player, answer);
		switch (result) {
		case LIAR_WIN:
			finishGame(Role.LIAR);
			break;

		case CITIZEN_WIN:
			finishGame(Role.CITIZEN);
			break;

		case WAITING:
			broadcast("오답입니다. 다른 라이어의 답을 기다리는 중입니다.");
			break;
		}
	};

	// ==================ChatManager, VoteManager에서 호출하는 부분
	// ==========================-> 아래 내부 코드로 이동===========
	public synchronized void chatEnd() {
		startVote();
	};

	public synchronized void voteEnd(List<Player> gettopVoted) {
		turnTimer.stop();
		startVoteResult(gettopVoted);
	}

	public synchronized void timeOut() {
		switch (phase) {
		case CHATTING:
			broadcast("채팅 시간 종료!");
			chatManager.timeOut();
			break;
		case VOTING:
			broadcast("투표 시간 종료!");
			voteEnd(voteManager.getTopVoted()); // 투표 안 한 사람은 무효표
			break;
		case LIAR_GUESSING:
			broadcast("정답 시간 종료!");
			finishGame(Role.CITIZEN);
			break;
		default:
			break;
		}
	};

	// ===============내부코드=================
	private void startVote() {
		System.out.println("=============================");
		changePhase(GamePhase.VOTING);
		broadcast("투표를 시작합니다! (" + GameConfig.VOTE_TIME + "초)");

		List<Player> candidates = getAlivePlayers();
		List<Player> voters = new ArrayList<Player>();
		for (Player p : candidates) {
			if (!p.isBot()) {
				voters.add(p);
			}
		}
		voteManager.startVote(voters, candidates); // voters랑 candidates 생성해서 VoteManager에게 넘겨줌
		turnTimer.start(GameConfig.VOTE_TIME);
	}

	private void startVoteResult(List<Player> gettopVoted) {
		if (gettopVoted.size() != 1) {
			broadcast("동률. 다음 라운드 진행");
			nextRound();
			return;
		}

		Player target = gettopVoted.get(0);
		broadcast("최다 득표자: Player" + target.getId());

		// 라이어 지목 → LiarManager가 판정
		if (liarManager.isLiar(target)) { // 1. 라이어 지목 성공
			broadcast("Player" + target.getId() + "은(는) 라이어였습니다!");

			if (liarManager.reveal(target)) {// 1-1. 모두 밝혀졌을 때 -> LiarGuess
				startLiarGuessing();
			} else {
				nextRound();
			} // 1-2. 라이어 남아있을 때 -> 다음 라운드
		}

		else { // 2. 라이어 지목 실패(Die)
			broadcast("Player" + target.getId() + "은(는) 시민이였습니다!");
			target.setStatus(PlayerStatus.DIED);
			nextRound();
		}
	}

	private void startLiarGuessing() {
		changePhase(GamePhase.LIAR_GUESSING);
		broadcast("라이어가 모두 밝혀졌습니다! 라이어는 제시어를 맞혀주세요. (" + GameConfig.LIAR_GUESS_TIME + "초)");
		turnTimer.start(GameConfig.LIAR_GUESS_TIME);
	}

	// ============부차적 내부코드==============
	// 채팅창 자체를 잠그는게 좀 복잡해서 일단 해당 참가자한데 알리는 식으로만 진행
	private boolean isSpectator(Player player) {
		if (player.getStatus() == PlayerStatus.DIED) {
			sendTo(player, "탈락하여 관전 중입니다. 게임이 끝날 때까지 채팅, 투표를 할 수 없습니다");
			return true;
		}
		return false;
	}

	private List<Player> getAlivePlayers() {
		List<Player> alive = new ArrayList<>();
		for (Player p : players) {
			if (p.getStatus() == PlayerStatus.ALIVE)
				alive.add(p);
		}
		return alive;
	}

	private void nextRound() {
		
		if(isLiarWinByCount()) {
			broadcast("남은 시민 수와 라이어 수가 같습니다.");
			finishGame(Role.LIAR);
			return;
		}
		
		if (isMaxRoundover(round, maxRound)) { // 최대라운드 끝났을때
			finishGame(Role.LIAR); // 라이어가 아직 남아 있음 -> 라이어 승리
			return;
		}
		round++;
		broadcast("" + round + "라운드 시작!");
		changePhase(GamePhase.CHATTING);
		chatManager.startChat(getAlivePlayers());
	}

	public boolean isMaxRoundover(int round, int maxRound) {
		if (round >= maxRound)
			return true;
		else
			return false;
	};

	private void finishGame(Role winner) {
		turnTimer.stop();
		changePhase(GamePhase.RESULT);

		String liarIds = "";
		for (Player liar : liars) {
			liarIds += "Player" + liar.getId() + " ";
		}
		broadcast("===== 게임 종료 =====");
		broadcast((winner == Role.LIAR ? "라이어 승리!" : "시민 승리!") + " / 라이어: " + liarIds + "/ 제시어: " + keyword);
		roomManager.returnToWaiting(room);

		for (int i = 0; i < players.size(); i++) {
			Player player = players.get(i);

			player.setChoosingRemain(true);
			sendTo(player, "1.다시하기  2.종료하기");
		}
	}

	public GamePhase getPhase() {
		return phase;
	};

	private void changePhase(GamePhase nextPhase) {
		phase = nextPhase;

		broadcast("PHASE:" + nextPhase.name());
	}

	public void startChatTimer() {
		turnTimer.start(GameConfig.CHAT_TIME);
	}

	public void stopChatTimer() {
		turnTimer.stop();
	}

	public void broadcast(String msg) {
		gameMessenger.broadcast(players, msg);
	}

	public void sendTo(Player player, String msg) {
		gameMessenger.sendTo(player, msg);
	}
	
	private boolean isLiarWinByCount() {
		int liarCount = liarManager.remainingLiarCount();
		int citizenCount = getAlivePlayers().size() - liarCount;
		
		return liarCount > 0 && citizenCount == liarCount;
	}
}
