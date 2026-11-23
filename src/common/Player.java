package common;
import java.io.PrintWriter;

import common.Role;

//플레이어 정보
public class Player {
    private int id;                  // 플레이어 번호 (발언 순서 기준)       
    private boolean bot;             
    private Role role;             
    private PlayerStatus status;     // ALIVE, DIED, REVEALED_LIAR
    private PrintWriter writer;
    
    
    public Player(int id, boolean bot) {
        this.id = id;
        this.bot = bot;
        this.status = PlayerStatus.ALIVE;
    }

    //===== 기본 정보 =====
    public int getId() {
        return id;
    }

    public void setId(int id) {
    	this.id = id;
    }
    public boolean isBot() {
        return bot;
    }

    //===== 역할 =====
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    //===== 상태 =====
    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }
    
    //===== 메시지 전송 =====
    public void setWriter(PrintWriter writer) {
    	this.writer = writer;
    }
    
    public void send(String msg) {
    	if (writer != null) {
			writer.println(msg);
		}
    	else {
			System.out.println("[Player" + id + "에게] " + msg);
		}
    }
    
    public boolean isConnected() {
        return writer != null;
    }

    //===== 새 게임 준비 (대기실로 돌아갈 때) =====
    private boolean choosingRemain = false;
    public boolean isChoosingRemain() {
    	return choosingRemain;
    }
    
    public void setChoosingRemain(boolean choosingRemain) {
    	this.choosingRemain = choosingRemain;
    }
    
    public void resetForNewGame() {
        this.role = null;
        this.status = PlayerStatus.ALIVE;
    }
}
