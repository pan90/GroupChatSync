package cn.paper_card.groupchatsync;

import java.util.concurrent.ConcurrentHashMap;

// 存储一个周期内QQ群成员的消息数量
public class MemberMessageCountPeriod {
    private final ConcurrentHashMap<Long, Integer> counts;

    public MemberMessageCountPeriod() {
        this.counts = new ConcurrentHashMap<>();
    }

    public int getCount(long qq) {
        final Integer integer = this.counts.get(qq);
        if (integer != null) return integer;
        return 0;
    }

    public int addCount(long qq) {
        final int count = this.getCount(qq) + 1;
        this.counts.put(qq, count);
        return count;
    }

    public void clearAll() {
        this.counts.clear();
    }
}
