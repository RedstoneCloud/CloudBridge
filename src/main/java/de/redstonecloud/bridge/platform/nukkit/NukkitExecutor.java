package de.redstonecloud.bridge.platform.nukkit;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.Task;
import com.google.common.net.HostAndPort;
import de.redstonecloud.api.components.ICloudPlayer;
import de.redstonecloud.bridge.cloudinterface.components.BridgeExecutor;
import de.redstonecloud.bridge.cloudinterface.components.BridgeServer;

import java.util.Objects;
import java.util.UUID;

public class NukkitExecutor implements BridgeExecutor {
    private static Server server = Server.getInstance();

    public Player getPlayerByCloudPlayer(ICloudPlayer player) {
        return server.getPlayer(UUID.fromString(player.getUUID())).get();
    }

    public void sendMessage(ICloudPlayer cloudPlayer, String message) {
        Objects.requireNonNull(getPlayerByCloudPlayer(cloudPlayer)).sendMessage(message);
    }

    @Override
    public void sendTitle(ICloudPlayer cloudPlayer, String title) {
        getPlayerByCloudPlayer(cloudPlayer).sendTitle(title);
    }

    @Override
    public void kick(ICloudPlayer player) {
        Objects.requireNonNull(getPlayerByCloudPlayer(player)).kick();
    }

    @Override
    public void kick(ICloudPlayer player, String reason) {
        Objects.requireNonNull(getPlayerByCloudPlayer(player)).kick(reason, false);
    }

    @Override
    public void sendActionbar(ICloudPlayer player, String message) {
        getPlayerByCloudPlayer(player).sendActionBar(message);
    }

    @Override
    public void sendToast(ICloudPlayer player, String title, String message) {
        getPlayerByCloudPlayer(player).sendToast(title, message);
    }

    @Override
    public void runDelayed(Runnable code, int tickDelay) {
        server.getScheduler().scheduleDelayedTask(new Task() {
            @Override
            public void onRun(int i) {
                code.run();
            }
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
