package de.redstonecloud.bridge.platform.waterdogpe;

import de.redstonecloud.bridge.cloudinterface.CloudInterface;
import dev.waterdog.waterdogpe.event.defaults.PlayerDisconnectedEvent;
import dev.waterdog.waterdogpe.event.defaults.PlayerLoginEvent;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;

public class WDPEListener {
    public static void onLogin(PlayerLoginEvent ev) {
        ProxiedPlayer player = ev.getPlayer();

        if(!ev.isCancelled()) CloudInterface.getInstance().playerLogin(player.getName(), player.getUniqueId().toString(), player.getAddress().toString());
        if(ev.isCancelled()) CloudInterface.getInstance().playerDisconnect(player.getUniqueId().toString());
    }

    public static void onDisconnect(PlayerDisconnectedEvent ev) {
        ProxiedPlayer player = ev.getPlayer();
        CloudInterface.getInstance().playerDisconnect(player.getUniqueId().toString());
    }
}
