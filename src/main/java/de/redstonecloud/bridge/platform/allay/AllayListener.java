package de.redstonecloud.bridge.platform.allay;

import de.redstonecloud.bridge.cloudinterface.CloudInterface;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.eventbus.event.player.PlayerLoginEvent;
import org.allaymc.api.eventbus.event.player.PlayerQuitEvent;

public class AllayListener {
    @EventHandler
    public void onLogin(PlayerLoginEvent ev) {
        EntityPlayer player = ev.getPlayer();

        CloudInterface.getInstance().playerLogin(player.getOriginName(), player.getLoginData().getUuid().toString(), player.getSocketAddress().toString());
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent ev) {
        EntityPlayer player = ev.getPlayer();

        CloudInterface.getInstance().playerDisconnect(player.getLoginData().getUuid().toString());
    }
}
