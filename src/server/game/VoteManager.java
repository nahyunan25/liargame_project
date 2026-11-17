package server.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Player;

public class VoteManager {
	private List<Player> voters; //투표할 수 있는 사람(살아있는 사람)
	private List<Player> candidates; //투표받을 수 있는 사람(살아있는 사람, 봇)
	private Map<Player, Player> votes = new HashMap<>(); //누가->누구에게
	private GameManager gameManager;
	private boolean voteEnded = false; //투표 종료 여부 저장
	
	//GameManager 연결
	public void setGameManager(GameManager gameManager) {
		this.gameManager = gameManager;
	}
	//투표 시작
	public void startVote(List<Player> voters, List<Player> candidates) {
		this.voters = voters;
		this.candidates = candidates;
		
		votes.clear(); //이전 투표 기록 초기화
		voteEnded=false; //새 투표 시작
		
		gameManager.sendAll("투표를 시작합니다.\n라이어라고 생각하는 player의 번호를 입력하세요.");
	}
	
	//투표 내역 저장
	public void handleVote(Player voter, Player target) {
		//투표 종료 됐다면 저장 x
		if(voteEnded == true) {
			return;
		}
		
		//투표 저장
		votes.put(voter, target);
		
		//모두 투표했는지 확인
		if(isAllVoted() == true) {
			voteEnded = true;
			gameManager.voteEnd();
		}
	}
	
	
	//투표 대상 찾기
	public void handleVote(Player voter, int targetId) {
		for(int i=0; i <candidates.size(); i++) {
			if(candidates.get(i).getId() == targetId) {
				handleVote(voter,candidates.get(i));
				break;
			}
		}
	}
	
	//시간 종료
	public void timeOut() {};
    
	//모두 투표 했는지 확인
    public boolean isAllVoted() {
    	return votes.size() == voters.size();
    } 
    
    //득표수 계산
    public Map<Player, Integer> countVotes() { 
    	Map<Player,Integer> result = new HashMap<>();
    	
    	//초기화
    	for(int i=0; i<candidates.size(); i++) {
    		Player candidate = candidates.get(i);
    		result.put(candidate, 0);
    	}
    	//득표수 증가
    	for(int i=0; i<voters.size(); i++) {
    		Player voter = voters.get(i);
    		Player target = votes.get(voter);
    		
    		if(target != null) {
    			int count = result.get(target);
    			result.put(target, count + 1);
    		}
    	}
    	
    	return result;
    }
    
    public Player mostVotedPlayer() {
    	Map<Player, Integer> result = countVotes();
    	
    	int max = 0;
    	int index = 0;
    	
    	for(int i=0; i<candidates.size(); i++) {
    		int count = result.get(candidates.get(i));
    		if(count > max) {
    			max = count;
    			index = i;
    		}
    	}
    	return candidates.get(index);
    }
}
