package server.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import common.GameConfig;
import common.Player;
import common.PlayerStatus;
import common.Role;
import server.room.Room;
import server.room.RoomManager;

//역할지정, 채팅, 시간, 투표, 판정
public class GameManager {
	//게임 데이터 변수
	private List<Player> players;
	private List<Player> liars;
	private List<Player> foundLiars = new ArrayList<>(); // 투표로 밝혀진 라이어(LiarGuess)
	private GamePhase phase; //현재 단계
	private int round;
	private int maxRound;
	private String category;
	private String keyword;
	
	//담당 클래스
	private RoleAssigner roleAssigner = new RoleAssigner();
	private ChatManager chatManager = new ChatManager();
	private VoteManager voteManager = new VoteManager();
	private LiarManager liarManager = new LiarManager();
	private TurnTimer turnTimer = new TurnTimer(this);
	
	//외부 연결
	private RoomManager roomManager;
	private Room room;
	
	//생성자: RoomManager와 연동
	public GameManager(RoomManager roomManager, List<Player>players) {
		this.roomManager = roomManager;
		this.players = players;
		chatManager.setGameManager(this);
		liarManager.setGameManger(this);
	};
	
	//외부에서 호출 -> 담당자에게 넘기기만
	public synchronized void join(Player player) {
		players.add(player);
		player.send("당신은 Player" + player.getId() + "입니다.");
		broadcast("Player" + player.getId() + " 입장 (" + players.size() + "/" + room.getPlayerCount()   + ")");

		if (players.size() == room.getPlayerCount()) {
			startGame();
		}
	}
	

	//==============startGame==============
	
	public synchronized void startGame() {//게임 시작
		setGame();
		sendRoles();
		round = 0;
		nextRound();
	}
	
	public void setGame() { //게임 시작 전 기본 세팅
		maxRound = GameConfig.getMaxRound(players.size());
		liars = roleAssigner.assign(players); //라이어 선정 한번에
		category = PickRandom.pickCategory();
		keyword = PickRandom.pickKeyword(category);
	}
	
	public void sendRoles() {
		for (Player p : players) {
			p.setStatus(PlayerStatus.ALIVE); //상태 alive로 모두 초기화
			
			if (p.getRole() == Role.LIAR) {
				sendTo(p, "주제: " + category + " / 당신은 라이어입니다!");
			} 
			else {
				sendTo(p, "주제: " + category + " / 제시어: " + keyword);
			}
		}
	}
	
	//===============handle_=================
	
	public synchronized void handleChat(Player player, String text) { //-> ChatManager의 handleChat과 연결
		if (phase != GamePhase.CHATTING) return;
		if (player.getStatus() != PlayerStatus.ALIVE) return; //조건 2가지: CHATTING 상태, ALIVE상태
		
		chatManager.handleChat(player, text);
	}; 
	public synchronized void handleVote(Player voter, int targetID) { //-> VoteManager의 handleVote과 연결
		if (phase != GamePhase.VOTING) return;
		if (voter.getStatus() != PlayerStatus.ALIVE || voter.isBot()) return; //조건 3가지: VOTING 상태, ALIVE상태, not bot
		
		voteManager.handleVote(voter, targetID);
		
		if (voteManager.isAllVoted()) {
			turnTimer.stop();
			broadcast("모두 투표하여 투표가 종료되었습니다.");
			voteEnd(voteManager.getTopVoted());
		};
	}; 
	
	public synchronized void handleLiarGuess(Player player, String answer) {
		if (phase != GamePhase.LIAR_GUESSING) return;
		if(player.getRole() != Role.LIAR) {
			player.send("라이어만 정답을 입력할 수 있습니다.");
			return;
		}
		
		broadcast("라이어가 추측한 답: "+answer);
		finishGame(liarManager.handleLiarGuess(answer));
	}; 
	
	
	//==================ChatManager, VoteManager에서 호출하는 부분 -> 아래 내부 코드로 이동===========
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
				voteEnd(voteManager.getTopVoted());   // 투표 안 한 사람은 무효표
				break;
			case LIAR_GUESSING:
				broadcast("정답 시간 종료!");
				finishGame(Role.CITIZEN);
				break;
			default:
				break;}
	};	
	
	//===============내부코드=================
	private void startVote() {
		System.out.println("=============================");
		changePhase(GamePhase.VOTING);
		broadcast("투표를 시작합니다! (" + GameConfig.VOTE_TIME + "초)");
		
		List<Player> candidates = getAlivePlayers();
		List<Player> voters = new ArrayList<Player>();
		for(Player p : candidates) {
			if (!p.isBot()) {
				voters.add(p);
			}
		}
		voteManager.startVote(voters, candidates); //voters랑 candidates 생성해서 VoteManager에게 넘겨줌
		turnTimer.start(GameConfig.VOTE_TIME);
	}
	
	private void startVoteResult(List<Player> gettopVoted) {
		if (gettopVoted.size() !=1) {
			broadcast("동률. 다음 라운드 진행");
			nextRound();
			return;}

		Player target = gettopVoted.get(0);
		broadcast("최다 득표자: Player" + target.getId());
		
		// 라이어 지목 → LiarManager가 판정
		if (liarManager.isLiar(target)) { //1. 라이어 지목 성공
			broadcast("Player" + target.getId() + "은(는) 라이어였습니다!");
			
			if (liarManager.reveal(target)) {//1-1. 모두 밝혀졌을 때 -> LiarGuess
				changePhase(GamePhase.LIAR_GUESSING);
				broadcast("라이어가 모두 밝혀졌습니다! 라이어는 제시어를 맞혀주세요. ("+ GameConfig.LIAR_GUESS_TIME + "초)");
				turnTimer.start(GameConfig.LIAR_GUESS_TIME);
			}
			
			else {nextRound();} //1-2. 라이어 남아있을 때 -> 다음 라운드
		}
			
		else { //2. 라이어 지목 실패(Die)
			broadcast("Player" + target.getId() + "은(는) 시민이였습니다!");
			target.setStatus(PlayerStatus.DIED);
			nextRound();
			}
		}		

	//========================================
	public GamePhase getPhase() {
		return phase;
	};
	
	private void changePhase(GamePhase nextPhase) {
		phase = nextPhase;
		
		broadcast("PHASE:"+ nextPhase.name());
	}
	
	public void startChatTimer() {
		turnTimer.start(GameConfig.CHAT_TIME);
	}
	
	public void stopChatTimer() {
		turnTimer.stop();
	}

	//메시지 전송 — TODO: 통신 연결 후 실제 전송으로 교체(broadcast, sendTo)
	public void broadcast(String msg) { //참가자 모두에게
		if(!msg.startsWith("PHASE:")) {
			System.out.println(msg);
		}
		for (Player p : players) {
	        if (p.isConnected()) p.send(msg);
	    }
	}

	private void sendTo(Player player, String msg) { //한 사람한테만
		player.send(msg);
	}	
	
	//살아 있는 사람만
		private List<Player> getAlivePlayers() {
			List<Player> alive = new ArrayList<>();
			for (Player p : players) {
				if (p.getStatus() == PlayerStatus.ALIVE) alive.add(p);
			}
			return alive;
		}
	
	private void nextRound() {
		if (isMaxRoundover(round, maxRound)) { //최대라운드 끝났을때
			finishGame(Role.LIAR);   // 라이어가 아직 남아 있음 -> 라이어 승리
			return;
		}
		round++;
		broadcast("" + round + "라운드 시작!");
		changePhase(GamePhase.CHATTING);
		chatManager.startChat(getAlivePlayers());
	}
	
	//최대 라운드까지 진행되었을 때
	public boolean isMaxRoundover(int round, int maxRound) {
		if (round >= maxRound) return true;
		else return false;
	};
	
	private void finishGame(Role winner) {
		turnTimer.stop();
		changePhase(GamePhase.RESULT);

		String liarIds = "";
		for (Player liar : liars) {
			liarIds += "Player" + liar.getId() + " ";
		}
		broadcast("===== 게임 종료 =====");
		broadcast((winner == Role.LIAR ? "라이어 승리!" : "시민 승리!")
				+ " / 라이어: " + liarIds + "/ 제시어: " + keyword);
		// TODO: Room 상태를 WAITING으로 돌리기 (대기실로)	
	}
}
