package de.redstonecloud.bridge.platform.pnx;

import  org.powernukkitx.Player;
import  org.powernukkitx.event.EventHandler;
import  org.powernukkitx.event.Listener;
import  org.powernukkitx.event.player.PlayerLoginEvent;
import  org.powernukkitx.event.player.PlayerQuitEvent;
import de.redstonecloud.bridge.cloudinterface.CloudInterface;

public class PNXListener implements Listener {
    @EventHandler
    public void onLogin(PlayerLoginEvent ev) {
        Player player = ev.getPlayer();

        CloudInterface.getInstance().playerLogin(player.getName(), player.getUniqueId().toString(), player.getRawSocketAddress().toString());
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent ev) {
        Player player = ev.getPlayer();

        CloudInterface.getInstance().playerDisconnect(player.getUniqueId().toString());
    }
}
