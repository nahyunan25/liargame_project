package server.room;

import common.Player;

public class Test_4Players {
    public static void main(String[] args) {

        Player host = new Player(1, false);

        Room room = new Room(host, 1, "테스트방",
                             "LIAR", "EASY", 4);

        room.printPlayers();
    }
}