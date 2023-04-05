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


    @EventHandler
    public void onGroupMessage(MiraiGroupMessageEvent event) {

        // 同步消息功能被禁用了
        if (!this.plugin.getConfigManager().isGroupChatSyncEnable()) return;

        // 非特定群聊消息
        if (event.getGroupID() != this.plugin.getConfigManager().getQQGroupID()) return;

        final String message = event.getMessage();

        if (message.length() < 1 || message.length() > 64) return; // 空消息或长消息

        final UUID uuid = MiraiMC.getBind(event.getSenderID());
        if (uuid == null) return; // 没有绑定的QQ

        final OfflinePlayer offlinePlayer = this.plugin.getServer().getOfflinePlayer(uuid);

        final String name = offlinePlayer.getName();

        if (name == null) return; // 无法获取玩家ID

        this.plugin.sendMessageToGameLater("<" + name + "> " + message);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // todo : AsyncPlayerChatEvent 被废弃了，但是不知道应该用什么代替

        // 游戏内聊天同步功能被禁用。
        if (!this.plugin.getConfigManager().isGameChatSyncEnable()) return;

        final Player player = event.getPlayer();

        // 一个周期内为一个玩家同步的消息数量最大为2
        if (this.plugin.getPlayerMessageCountPeriod().getCount(player) > 2) return;

        // 周期内消息数量加一
        this.plugin.getPlayerMessageCountPeriod().addCount(player);

        final String message = "<" + event.getPlayer().getName() + "> " +event.getMessage();

        // 消息进入队列等待转发
        this.plugin.sendMessageToGroupLater(message);
    }
}
