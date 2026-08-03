package de.redstonecloud.bridge.platform.pnx;

import org.powernukkitx.plugin.PluginBase;
import de.redstonecloud.bridge.cloudinterface.CloudInterface;

public class PNXPlugin extends PluginBase {
    public CloudInterface cloudInterface;

    @Override
    public void onLoad() {
        cloudInterface = CloudInterface.getInstance();
    }

    @Override
    public void onEnable() {
        cloudInterface.start(new PNXExecutor());
        this.getServer().getPluginManager().registerEvents(new PNXListener(), this);
    }

    @Override
    public void onDisable() {
        cloudInterface.shutdown();
    }
}