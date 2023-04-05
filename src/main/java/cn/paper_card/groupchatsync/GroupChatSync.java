package cn.paper_card.groupchatsync;

import org.bukkit.plugin.java.JavaPlugin;

public final class GroupChatSync extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new MessageListener(this), this);
    }

    @Override
    public void onDisable() {

    }
}
