package de.redstonecloud.bridge.platform.nukkit;

import cn.nukkit.plugin.PluginBase;
import de.redstonecloud.bridge.cloudinterface.CloudInterface;

public class NukkitPlugin extends PluginBase {
    public CloudInterface cloudInterface;

    @Override
    public void onLoad() {
        cloudInterface = CloudInterface.getInstance();
    }

    @Override
    public void onEnable() {
        cloudInterface.start(new NukkitExecutor());
        this.getServer().getPluginManager().registerEvents(new NukkitListener(), this);
    }

    @Override
    public void onDisable() {
        cloudInterface.shutdown();
    }
}