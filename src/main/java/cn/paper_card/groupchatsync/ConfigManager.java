package cn.paper_card.groupchatsync;

public class ConfigManager {
    private final static long QQ_GROUP = 706926037L;

    private final static String GAME_CHAT_SYNC_ENABLE = "game_chat_sync_enable";
    private final static String GROUP_CHAT_SYNC_ENABLE = "group_chat_sync_enable";

    private final GroupChatSync plugin;

    public ConfigManager(GroupChatSync plugin) {
        this.plugin = plugin;
    }

    public long getQQGroupID() {
        return QQ_GROUP;
    }

    // 全局开关，游戏内公屏消失是否同步到QQ群
    public boolean isGameChatSyncEnable() {
        return this.plugin.getConfig().getBoolean(GAME_CHAT_SYNC_ENABLE, true);
    }

    public void setGameChatSyncEnable(boolean enable) {
        this.plugin.getConfig().set(GAME_CHAT_SYNC_ENABLE, enable);
    }

    // 全局开关，QQ群消失是否同步到游戏内
    public boolean isGroupChatSyncEnable() {
        return this.plugin.getConfig().getBoolean(GROUP_CHAT_SYNC_ENABLE, true);
    }

    public void setGroupChatSyncEnable(boolean enable) {
        this.plugin.getConfig().set(GROUP_CHAT_SYNC_ENABLE, enable);
    }
}
