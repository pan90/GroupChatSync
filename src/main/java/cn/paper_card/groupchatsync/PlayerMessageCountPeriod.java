package cn.paper_card.groupchatsync;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

// 保存一个周期内玩家发消息的次数。
public class PlayerMessageCountPeriod {
    private final ConcurrentHashMap<OfflinePlayer, Integer> counts;

    public PlayerMessageCountPeriod() {
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

    public void addCount(@NotNull OfflinePlayer player) {
        final int c = this.getCount(player) + 1;
        this.counts.put(player, c);
    }
}
