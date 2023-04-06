package cn.paper_card.groupchatsync;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

// 存储玩家在一个周期内公屏消息数量
public class PlayerChatMsgCountPeriod {
    private final ConcurrentHashMap<OfflinePlayer, Integer> counts;

    public PlayerChatMsgCountPeriod() {
        this.counts = new ConcurrentHashMap<>();
    }

    public void clearAll() {
        this.counts.clear();
    }

    public int getCount(@NotNull OfflinePlayer player) {
        final Integer integer = this.counts.get(player);
        if (integer == null) return 0;
        return integer;
    }

    public int addCount(@NotNull OfflinePlayer player) {
        final int c = this.getCount(player) + 1;
        this.counts.put(player, c);
        return c;
    }
}
