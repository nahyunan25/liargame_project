package server.game;

import java.util.List;

import common.Player;
import server.room.RoomManager;

//역할지정, 채팅, 시간, 투표, 판정
public class GameManager {
	//게임 데이터 변수
	private List<Player> players;
	private List<Player> liars;
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
	public GameManager(RoomManager roomManager, List<Player>players) {};
	
	//외부에서 호출 -> 담당자에게 넘기기만
	public void startGame() {}; 
	public void handleChat(Player player, String text) { //-> ChatManager의 handleChat과 연결
		if(phase == GamePhase.CHATTING) {
			chatManager.handleChat(player, text);
		}
	}; 
	public void handleVote(Player voter, Player target) { //-> VoteManager의 handleVote과 연결
		if (phase == GamePhase.VOTING) {
			voteManager.handleVote(voter, target);
		}
	}; 
	public void handleLiarGuess(Player player, String answer) {}; 
	
	//GameManager에서 단계 관리
	//담당자들이 각 단계가 끝나면 GameManager에게 알림
	public void chatEnd() {};
	public void voteEnd() {};
	public void timeOut() {};
}
