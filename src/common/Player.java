package common;
import common.Role;

//플레이어 정보
public class Player {
    private int id;                  // 플레이어 번호 (발언 순서 기준)
    private String nickname;        
    private boolean bot;             
    private Role role;             
    private PlayerStatus status;     // ALIVE, DIED, REVEALED_LIAR
    // TODO: 통신 담당의 ClientHandler 연결 (메시지 전송용)

    public Player(int id, String nickname, boolean bot) {
        this.id = id;
        this.nickname = nickname;
        this.bot = bot;
        this.status = PlayerStatus.ALIVE;
    }

    //===== 기본 정보 =====
    public int getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
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

    //===== 새 게임 준비 (대기실로 돌아갈 때) =====
    public void resetForNewGame() {
        this.role = null;
        this.status = PlayerStatus.ALIVE;
    }
}