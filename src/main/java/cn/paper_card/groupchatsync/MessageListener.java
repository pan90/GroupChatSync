package cn.paper_card.groupchatsync;

import me.dreamvoid.miraimc.api.MiraiMC;
import me.dreamvoid.miraimc.bukkit.event.message.passive.MiraiGroupMessageEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class MessageListener implements Listener {

    private final GroupChatSync plugin;


    public MessageListener(GroupChatSync plugin) {
        this.plugin = plugin;
    }

    // 判断是否为群管理员的消息
    private boolean isGroupAdmin(MiraiGroupMessageEvent event) {
        return event.getSenderPermission() >= 1;
    }

    private boolean handleAdminCommand(MiraiGroupMessageEvent event) {
        if (!this.isGroupAdmin(event)) return false;

        if ("游戏消息同步开启".equals(event.getMessage())) {
            this.plugin.getConfigManager().setGameChatSyncEnable(true);
            this.plugin.sendMessageToGroupLater("游戏消息同步已开启");
            // todo
//            this.plugin.sendMessageToGameLater("游戏消息同步已开启，您的聊天消息可能会被同步到QQ群聊。");
            return true;
        }

        if ("游戏消息同步关闭".equals(event.getMessage())) {
            this.plugin.getConfigManager().setGameChatSyncEnable(false);
            this.plugin.sendMessageToGroupLater("游戏消息同步已关闭");
            // todo
//            this.plugin.sendMessageToGameLater("游戏消息同步已关闭，您的聊天消息将不会被同步到QQ群聊。");
            return true;
        }

        return false;
    }

    @EventHandler
    public void onGroupMessage(MiraiGroupMessageEvent event) {

        // 屏蔽其它群聊的消息
        if (event.getGroupID() != this.plugin.getConfigManager().getQQGroupID()) return;

        // 处理管理员指令
        if (this.handleAdminCommand(event)) {
            return;
        }

        // 自动禁言QQ群成员消息
        final MemberMessageCountPeriod memberMessageCountPeriod = this.plugin.getMemberMessageCountPeriod();
        if (memberMessageCountPeriod.addCount(event.getSenderID()) > 3) {
            // 机器人可能没有禁言权限
            if (event.getGroup().getBotPermission() > 0) {
                // 消息太过频繁，禁言1分钟
                event.getGroup().getMember(event.getSenderID()).setMute(60);
            } else {
                this.plugin.getLogger().severe
                        ("机器人[" + event.getBotID() + "]在群聊[" + event.getGroupID() + "]中没有管理员权限，无法自动禁言！");
            }
            return;
        }

        // 同步消息功能被禁用了
        if (!this.plugin.getConfigManager().isGroupChatSyncEnable()) return;

        final String message = event.getMessage();

        if (message.length() < 1 || message.length() > 64) return; // 空消息或长消息

        final UUID uuid = MiraiMC.getBind(event.getSenderID());
        if (uuid == null) return; // 没有绑定的QQ

        final OfflinePlayer offlinePlayer = this.plugin.getServer().getOfflinePlayer(uuid);

        final String name = offlinePlayer.getName();

        if (name == null) return; // 无法获取玩家ID

        this.plugin.sendMessageToGameLater(new GroupMessage(name, event.getSenderID(), message));
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // todo : AsyncPlayerChatEvent 被废弃了，但是不知道应该用什么代替

        // 游戏内自动禁言
        if (this.plugin.getPlayerChatMsgCountPeriod().addCount(event.getPlayer()) > 3) {

            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                // 使用ESS的禁言命令禁言
                this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(),
                        "mute " + event.getPlayer().getName() + " 1m 刷屏自动禁言");
            });
            return;
        }

        // 由机器人转发消息到QQ
        final String prefix = "#";
        String msg = event.getMessage();
        if (!msg.startsWith(prefix)) return;

        // 截取真正的消息
        msg = msg.substring(prefix.length());


        // 游戏内聊天同步功能被禁用。
        if (!this.plugin.getConfigManager().isGameChatSyncEnable()) return;

        final Player player = event.getPlayer();

        // 一个周期内为一个玩家同步的消息数量最大为2
        if (this.plugin.getPlayerMessageCountPeriod().getCount(player) > 2) return;

        // 周期内消息数量加一
        this.plugin.getPlayerMessageCountPeriod().addCount(player);


        // 消息进入队列等待转发
        this.plugin.sendMessageToGroupLater("<" + player.getName() + "> " + msg);
    }
}
