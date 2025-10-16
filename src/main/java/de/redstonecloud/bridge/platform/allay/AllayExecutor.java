package de.redstonecloud.bridge.platform.allay;

import com.google.common.net.HostAndPort;
import de.redstonecloud.api.components.ICloudPlayer;
import de.redstonecloud.bridge.cloudinterface.components.BridgeExecutor;
import de.redstonecloud.bridge.cloudinterface.components.BridgeServer;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.scheduler.Task;
import org.allaymc.api.scheduler.TaskCreator;
import org.allaymc.api.server.Server;

import java.util.Objects;
import java.util.UUID;

public class AllayExecutor implements BridgeExecutor {
    private static Server server = Server.getInstance();

    public EntityPlayer getPlayerByCloudPlayer(ICloudPlayer player) {
        return server.getPlayerManager().getPlayers().get(player.getUUID());
    }

    public void sendMessage(ICloudPlayer cloudPlayer, String message) {
        Objects.requireNonNull(getPlayerByCloudPlayer(cloudPlayer)).sendMessage(message);
    }

    @Override
    public void sendTitle(ICloudPlayer cloudPlayer, String title) {
        Objects.requireNonNull(getPlayerByCloudPlayer(cloudPlayer)).sendTitle(title);
    }

    @Override
    public void kick(ICloudPlayer player) {
        Objects.requireNonNull(getPlayerByCloudPlayer(player)).disconnect();
    }

    @Override
    public void kick(ICloudPlayer player, String reason) {
        Objects.requireNonNull(getPlayerByCloudPlayer(player)).disconnect(reason);
    }

    @Override
    public void sendActionbar(ICloudPlayer player, String message) {
        Objects.requireNonNull(getPlayerByCloudPlayer(player)).sendActionBar(message);
    }

    @Override
    public void sendToast(ICloudPlayer player, String title, String message) {
        Objects.requireNonNull(getPlayerByCloudPlayer(player)).sendToast(title, message);
    }

    @Override
    public void runDelayed(Runnable code, int tickDelay) {
        server.getScheduler().scheduleDelayed(Server.getInstance(), () -> {
            code.run();
            return true;
        }, tickDelay);
    }


    public void addServer(String name, HostAndPort address) {}

    public void removeServer(String name) {}

    public boolean hasServer(String name) {
        return false;
    }

    public BridgeServer determineServer(String serverName) {
        return null;
    }

    public void connect(ICloudPlayer player, String name) {}
}
