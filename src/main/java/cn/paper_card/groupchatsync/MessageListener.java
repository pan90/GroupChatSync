package cn.paper_card.groupchatsync;

import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.api.MiraiMC;
import me.dreamvoid.miraimc.api.bot.MiraiGroup;
import me.dreamvoid.miraimc.bukkit.event.message.passive.MiraiGroupMessageEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class MessageListener implements Listener {

    private final static long QQ_GROUP = 706926037L;

    private final GroupChatSync plugin;

    public MessageListener(GroupChatSync plugin) {
        this.plugin = plugin;
    }

    // 通过在线的QQ机器人获取QQ群
    private MiraiGroup getGroup() {
        MiraiGroup group = null;
        final List<Long> onlineBots = MiraiBot.getOnlineBots();
        for (final Long id : onlineBots) {
            try {
                final MiraiBot bot = MiraiBot.getBot(id);
                group = bot.getGroup(QQ_GROUP);
                if (group != null) break;
            }  catch (NoSuchElementException e) {
                this.plugin.getLogger().severe(e.getLocalizedMessage());
            }
        }
        return group;
    }

    @EventHandler
    public void onGroupMessage(MiraiGroupMessageEvent event) {
        if (event.getGroupID() != QQ_GROUP) return;

        final String message = event.getMessage();

        if (message.length() < 1 || message.length() > 64) return; // 空消息或长消息

        final UUID uuid = MiraiMC.getBind(event.getSenderID());
        if (uuid == null) return; // 没有绑定的QQ

        final OfflinePlayer offlinePlayer = this.plugin.getServer().getOfflinePlayer(uuid);

        final String name = offlinePlayer.getName();

        if (name == null) return; // 无法获取玩家ID

        // 广播消息
        this.plugin.getServer().getScheduler().runTask(this.plugin,
                () -> MessageListener.this.plugin.getServer().broadcast(Component.text
                        ("<" + name + "> " + message)
                )
        );
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // todo : AsyncPlayerChatEvent 被废弃了，但是不知道应该用什么代替

        final MiraiGroup group = this.getGroup();
        if (group == null) return;

        group.sendMessage( "<" + event.getPlayer().getName() + "> " +event.getMessage());
    }
}
