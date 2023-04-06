package cn.paper_card.groupchatsync;

import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.api.bot.MiraiGroup;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class GroupChatSync extends JavaPlugin {

    // 等待转发到QQ群的游戏消息
    private final ConcurrentLinkedQueue<String> game_message;

    // 等待转发到游戏内的QQ群消息
    private final ConcurrentLinkedQueue<String> group_message;

    private final ConfigManager configManager;

    private final PlayerMessageCountPeriod playerMessageCountPeriod;

    private final MemberMessageCountPeriod memberMessageCountPeriod;

    private final PlayerChatMsgCountPeriod playerChatMsgCountPeriod;

    // 转发游戏内消息到QQ群的任务
    private BukkitTask task = null;

    private BukkitTask taskResetCount = null;

    private BukkitTask taskResetMemberMsgCount = null;

    private BukkitTask taskResetPlayerMsgCount = null;

    public GroupChatSync() {
        this.game_message = new ConcurrentLinkedQueue<>();
        this.group_message = new ConcurrentLinkedQueue<>();
        this.configManager = new ConfigManager(this);
        this.playerMessageCountPeriod = new PlayerMessageCountPeriod();
        this.memberMessageCountPeriod = new MemberMessageCountPeriod();
        this.playerChatMsgCountPeriod = new PlayerChatMsgCountPeriod();
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public PlayerMessageCountPeriod getPlayerMessageCountPeriod() {
        return this.playerMessageCountPeriod;
    }

    public MemberMessageCountPeriod getMemberMessageCountPeriod() {
        return this.memberMessageCountPeriod;
    }

    public PlayerChatMsgCountPeriod getPlayerChatMsgCountPeriod() {
        return this.playerChatMsgCountPeriod;
    }

    // 通过在线的QQ机器人获取QQ群
    @Nullable
    public MiraiGroup findGroup() {
        MiraiGroup group = null;
        final List<Long> onlineBots = MiraiBot.getOnlineBots();
        for (final Long id : onlineBots) {
            try {
                final MiraiBot bot = MiraiBot.getBot(id);
                group = bot.getGroup(this.configManager.getQQGroupID());
                if (group != null) break;
            }  catch (NoSuchElementException e) {
                this.getLogger().severe(e.getClass().getName() + ": " + e.getLocalizedMessage());
            }
        }
        return group;
    }

    public void sendMessageToGroupLater(@NotNull String message) {
        this.game_message.offer(message);
    }

    public void sendMessageToGameLater(@NotNull String message) {
        this.group_message.offer(message);
    }

    @Override
    public void onEnable() {

        this.saveDefaultConfig(); // 如果没有配置文件的话保存默认配置

        // 注册事件监听
        this.getServer().getPluginManager().registerEvents(new MessageListener(this), this);

        if (this.task == null) {
            // 每20个tick检查队列中有没有消息要发送的群聊中。
            this.task = this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {

                // 转发游戏消息到群里中
                final String game_msg = GroupChatSync.this.game_message.poll();
                final MiraiGroup group = this.findGroup();
                if (game_msg != null && group != null) {
                    group.sendMessage(game_msg);
                }

                // 转发群消息到游戏中
                final String group_msg = GroupChatSync.this.group_message.poll();
                if (group_msg != null) {
                    GroupChatSync.this.getServer().broadcast(Component.text(group_msg));
                }
            }, 40, 40);
        }

        if (this.taskResetCount == null) {
            this.taskResetCount = this.getServer().getScheduler().runTaskTimerAsynchronously(this,
                    GroupChatSync.this.playerMessageCountPeriod::clearAll,
                    2 * 20, 20 * 60 /* 一分钟重置一次 */ );
        }

        if (this.taskResetMemberMsgCount == null) {
            this.taskResetMemberMsgCount = this.getServer().getScheduler().runTaskTimerAsynchronously
                    (this, GroupChatSync.this.memberMessageCountPeriod::clearAll, 2*20, 10*20 /* 20秒重置一次 */);
        }

        if (this.taskResetPlayerMsgCount == null) {
            this.taskResetPlayerMsgCount = this.getServer().getScheduler().runTaskTimerAsynchronously
                    (this, GroupChatSync.this.playerChatMsgCountPeriod::clearAll, 2*20, 10*20);
        }
    }

    @Override
    public void onDisable() {
        // 保存配置
        this.saveConfig();

        // 取消任务
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
            this.task = null;
        }

        if (this.taskResetCount != null && !this.taskResetCount.isCancelled()) {
            this.taskResetCount.cancel();
            this.taskResetCount = null;
        }

        if (this.taskResetMemberMsgCount != null && !this.taskResetMemberMsgCount.isCancelled()) {
            this.taskResetMemberMsgCount.cancel();
            this.taskResetMemberMsgCount = null;
        }

        if (this.taskResetPlayerMsgCount != null && !this.taskResetPlayerMsgCount.isCancelled()) {
            this.taskResetPlayerMsgCount.cancel();
            this.taskResetPlayerMsgCount = null;
        }
    }
}
