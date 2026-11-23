package server.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Player;

public class VoteManager {
	private List<Player> voters; //투표할 수 있는 사람(살아있는 사람)
	private List<Player> candidates; //투표받을 수 있는 사람(살아있는 사람, 봇)
	private Map<Player, Player> votes = new HashMap<>(); //누가->누구에게
	
	//투표 시작
	public void startVote(List<Player> voters, List<Player> candidates) {
		
		this.voters = voters;
		this.candidates = candidates;
		
		votes.clear(); //이전 투표 기록 초기화
	}
	
	//투표 내역 저장
	public void handleVote(Player voter, Player target) {
		
		//이미 투표했으면
		if(votes.containsKey(voter)) {
			voter.send("이미 투표했습니다.");
			return;
		}
		
		//자기 자신에게 투표했으면
		if(voter.getId() == target.getId()) {
			voter.send("자기 자신에게 투표할 수 없습니다.");
			return;
		}
		
		votes.put(voter, target);
		voter.send("투표를 완료하였습니다.");
		
		
		//콘솔에서만 누가 누구 투표했는지 보이게하기
		System.out.println("Player" +  voter.getId() + "-> Player" + target.getId()+ "에게 투표");
	}
	
	
	//투표 대상 찾기
	public void handleVote(Player voter, int targetId) {
		for(int i=0; i <candidates.size(); i++) {
			if(candidates.get(i).getId() == targetId) {
				handleVote(voter,candidates.get(i));
				return;
			}
		}
		//없는 플레이어라면
		if(votes.containsKey(voter)) {
			voter.send("이미 투표했습니다.");
			return;
		}
		else {
			voter.send("없는 플레이어입니다. 다시 투표해주세요.");
			return;
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
    
    //GameManager 내 코드 이동
	public List<Player> getTopVoted() {
		Map<Player, Integer> counts = countVotes();
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
