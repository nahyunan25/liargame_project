package server.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import common.GameConfig;
import common.Player;
import common.PlayerStatus;
import common.Role;
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
	private TurnTimer turnTimer = new TurnTimer(this);
	private GameJudge gameJudge = new GameJudge();
	
	//외부 연결
	private RoomManager roomManager;
	
	//생성자: RoomManager와 연동
	public GameManager(RoomManager roomManager, List<Player>players) {
		this.roomManager = roomManager;
		this.players = players;
		chatManager.setGameManager(this);
	};
	
	//외부에서 호출 -> 담당자에게 넘기기만
	public synchronized void startGame() {
		maxRound = GameConfig.getMaxRound(players.size());
		
		//1단계: role assign
		liars = roleAssigner.selectLiars(players);
		roleAssigner.assignRoles(players, liars);
		
		//2단계: category, keyword 선정
		category = roleAssigner.pickCategory();
		keyword = roleAssigner.pickKeyword(category);
		
		//3단계: 상태 초기화, 각자에게 역할 안내
		for (Player p : players) {
			p.setStatus(PlayerStatus.ALIVE); //상태 alive로 모두 초기화
			
			if (p.getRole() == Role.LIAR) {
				sendTo(p, "주제: " + category + " / 당신은 라이어입니다!");
			} 
			else {
				sendTo(p, "주제: " + category + " / 제시어: " + keyword);
			}
		};
		
		//4단계: 첫 라운드 시작
		round = 1;
		nextRound();
	}
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
			voteEnd(getTopVoted());
		};
		
	}; 
	public void handleLiarGuess(Player player, String answer) {
		if (phase != GamePhase.LIAR_GUESSING) return;
		
		if (gameJudge.checkLiarGuess(answer, keyword)) {
			finishGame(Role.LIAR);
		}
		else {
			finishGame(Role.CITIZEN);
		}
	}; 
	
	
	//GameManager에서 단계 관리
	//담당자들이 각 단계가 끝나면 GameManager에게 알림
	public synchronized void chatEnd() {
		phase = GamePhase.VOTING;
		broadcast("투표를 시작합니다! (" + GameConfig.VOTE_TIME + "초)");
		
		List<Player> voters = getAlivePlayers();
		List<Player> candidates = new ArrayList<Player>();
		for(Player p : voters) {
			if (!p.isBot()) {
				candidates.add(p);
			}
		}
		voteManager.startVote(voters, candidates); //voters랑 candidates 생성해서 VoteManager에게 넘겨줌
		turnTimer.start(GameConfig.VOTE_TIME);
	};
	
	public synchronized void voteEnd(List<Player> topVoted) {
		if (topVoted.size() !=1) {
			broadcast("동률. 다음 라운드 진행");
			nextRound();
			return;
		}
		
		Player target = topVoted.get(0);
		broadcast("최다 득표자: Player" + target.getId());
		
		// 라이어 지목 → GameJudge가 판정
		if (gameJudge.isLiar(target)) {
			target.setStatus(PlayerStatus.REVEALED_LIAR);
			liars.add(target);
			broadcast("Player" + target.getId() + "은(는) 라이어였습니다!");

			if (gameJudge.isAllLiarsFound(foundLiars, liars.size())) {
				startLiarGuess();   // 라이어가 모두 밝혀짐 → 정답 단계
				return;
			}
		}		
		// 3. 시민 지목 → 탈락
			else {
				target.setStatus(PlayerStatus.DIED);
				broadcast("Player" + target.getId() + "은(는) 시민이었습니다. 탈락!");
			}
			nextRound();
		};
	
	public synchronized void timeOut() {
		switch (phase) {
			case CHATTING:
				broadcast("채팅 시간 종료!");
				chatManager.timeOut();
				break;
			case VOTING:
				broadcast("투표 시간 종료!");
				voteEnd(getTopVoted());   // 투표 안 한 사람은 무효표
				break;
			case LIAR_GUESSING:
				broadcast("정답 시간 종료!");
				finishGame(Role.CITIZEN);
				break;
			default:
				break;}
	};
	
	public GamePhase getPhase() {
		return phase;
	};
	
	//=============내부 단계 진행
	//메시지 전송 — TODO: 통신 연결 후 실제 전송으로 교체(broadcast, sendTo)
	private void broadcast(String msg) {
		System.out.println("[전체] " + msg);
	}

	private void sendTo(Player player, String msg) {
		System.out.println("[Player" + player.getId() + "에게] " + msg);
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
		broadcast("===== " + round + "라운드 시작! =====");
		if (gameJudge.isMaxRoundover(round, maxRound)) {
			finishGame(Role.LIAR);   // 라이어가 아직 남아 있음 -> 라이어 승리
			return;
		}
		round++;
		phase = GamePhase.CHATTING;
		chatManager.startChat(getAlivePlayers());
	}
	
	//라이어 정답 단계
	private void startLiarGuess() {
		phase = GamePhase.LIAR_GUESSING;
		broadcast("라이어가 모두 밝혀졌습니다! 라이어는 제시어를 맞혀주세요. ("
				+ GameConfig.LIAR_GUESS_TIME + "초)");
		turnTimer.start(GameConfig.LIAR_GUESS_TIME);
	}	
		
	private void finishGame(Role winner) {
		turnTimer.stop();
		phase = GamePhase.RESULT;

		String liarIds = "";
		for (Player liar : liars) {
			liarIds += "Player" + liar.getId() + " ";
		}
		broadcast("===== 게임 종료 =====");
		broadcast((winner == Role.LIAR ? "라이어 승리!" : "시민 승리!")
				+ " / 라이어: " + liarIds + "/ 제시어: " + keyword);
		// TODO: Room 상태를 WAITING으로 돌리기 (대기실로)	
	}
	
	// TODO: VoteManager에 getTopVoted()가 생기면 그걸로 교체
	private List<Player> getTopVoted() {
		Map<Player, Integer> counts = voteManager.countVotes();
		List<Player> top = new ArrayList<>();
		int max = 0;
		for (Map.Entry<Player, Integer> e : counts.entrySet()) {
			if (e.getValue() > max) {
				max = e.getValue();
				top.clear();
				top.add(e.getKey());
			} else if (e.getValue() == max && max > 0) {
				top.add(e.getKey());
			}
		}
		if (max == 0) top.addAll(counts.keySet());   // 아무도 투표 안 함 → 전원 동률
		return top;
	}			
}
