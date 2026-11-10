package server.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import server.room.Player;

public class VoteManager {
	private List<Player> voters; //투표할 수 있는 사람(살아있는 사람)
	private List<Player> candidates; //투표받을 수 있는 사람(살아있는 사람, 봇)
	private Map<Player, Player> votes = new HashMap<>(); //누가->누구에게
	
	public VoteManager() {};
	
	public void startVote(List<Player> voters, List<Player> candidates) {};
    
	public boolean hasVoted(Player voter) { return false; } //이미 투표했는지 확인
    public boolean isAllVoted() { return false; } //모두 투표했는지 확인
    
    public Map<Player, Integer> countVotes() { return null; } //후보별 득표수 집계
    public List<Player> getTopVoted() {return null;}; //최다 득표자 목록

    public void reset() {};
}
