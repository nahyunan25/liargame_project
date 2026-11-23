package server.room;

import common.Player;

public class RoomChatManager {

    // 대기실 채팅 처리
    public void handleChat(Room room, Player sender, String message) {

        // 대기실 상태에서만 채팅 가능
        if (!room.isWaiting()) {
            return;
        }

        int number = room.getPlayerNumber(sender);

        String msg = "Player" + number + ": " + message;
        
        //서버 콘솔에서 방 별 챗 구분
        System.out.println("[방" + room.getRoomId() + "]" + msg);

        // 해당 방의 플레이어들에게 전송
        for (Player p : room.getPlayers()) {
            if (p.isConnected()) {
                p.send(msg);
            }
        }
    }
}