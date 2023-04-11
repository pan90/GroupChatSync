package cn.paper_card.groupchatsync;

public class GroupMessage {

    private final String playerName;
    private final String message;

    private final long qq;


    public GroupMessage(String playerName, long qq, String message) {
        this.playerName = playerName;
        this.qq = qq;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public long getQQ() {
        return this.qq;
    }

    public String getPlayerName() {
        return playerName;
    }
}
