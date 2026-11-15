package common;

public enum PlayerStatus {
	ALIVE("참여 중"),
	DIED("탈락"),
	REVEALED_LIAR("라이어 발각");
	
	private String label;
	
	private PlayerStatus(String label) {
		this.label=label;
	}

	public String getLabel() { //화면(채팅창) 표시용 이름
		return label;
	}
}
