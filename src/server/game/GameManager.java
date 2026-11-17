package server.game;

import java.util.List;

import common.Player;
import server.room.RoomManager;
import java.io.PrintWriter;

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
	
	//클라이언트 메세지 전송용
	private List<PrintWriter> writers;
	
	//생성자: RoomManager와 연동
	public GameManager(RoomManager roomManager, List<Player>players) {
		this.players = players; //Server에서 전달 받음
		this.roomManager = roomManager;
		this.phase = GamePhase.WAITING;
		chatManager.setGameManager(this);
		voteManager.setGameManager(this);
	}

	//Server에서 클라이언트 목록 받기
	public void setWriters(List<PrintWriter> writers) {
		this.writers = writers;
	}
	//Server와 클라이언트에 메세지 출력
	public void sendAll(String message) {
		//서버 콘솔 출력
		System.out.println(message);
		
		//모든 클라이언트 전송
		synchronized(writers) {			
			for(int i = 0; i<writers.size(); i++) {
				writers.get(i).println(message);
			}
		}
	}
	//외부에서 호출 -> 담당자에게 넘기기만
	public void startGame() {
		phase = GamePhase.CHATTING;
		chatManager.startChat(players);
	}; 
	
	public void handleChat(Player player, String text) { //-> ChatManager의 handleChat과 연결
		if(phase == GamePhase.CHATTING) {
			chatManager.handleChat(player, text);
		}
	}; 
	public void handleVote(Player voter, int targetId) { //-> VoteManager의 handleVote과 연결
		if (phase == GamePhase.VOTING) {
			voteManager.handleVote(voter, targetId);
		}
	}; 
	public void handleLiarGuess(Player player, String answer) {}; 
	
	//GameManager에서 단계 관리
	//담당자들이 각 단계가 끝나면 GameManager에게 알림
	
	//chatEnd 메세지 출력 -> VOTTING으로 phase 변경
	public void chatEnd() {
		sendAll("모든 플레이어의 채팅이 종료되었습니다.");
		phase = GamePhase.VOTING;
		voteManager.startVote(players, players);
		}
	
	//VoteEnd 메세지 출력
	public void voteEnd() {
		sendAll("투표가 종료되었습니다.");
		
		Player player = voteManager.mostVotedPlayer();
		sendAll("최다 득표자는 Player" + player.getId()+ "입니다.");
	};
	public void timeOut() {};
}
