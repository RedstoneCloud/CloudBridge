package de.redstonecloud.bridge.platform.waterdogpe;

import com.google.common.net.HostAndPort;
import de.redstonecloud.api.components.ICloudPlayer;
import de.redstonecloud.bridge.cloudinterface.components.BridgeExecutor;
import de.redstonecloud.bridge.cloudinterface.components.BridgeServer;
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.network.serverinfo.BedrockServerInfo;
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import dev.waterdog.waterdogpe.scheduler.Task;
import dev.waterdog.waterdogpe.utils.types.TextContainer;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;

public class WDPEExecutor implements BridgeExecutor {
    private static ProxyServer server = ProxyServer.getInstance();

    public void addServer(String name, HostAndPort address) {
        server.registerServerInfo(new BedrockServerInfo(name, new InetSocketAddress(address.getHost(), address.getPort()), new InetSocketAddress(address.getHost(), address.getPort())));
    }

    public void removeServer(String name) {
        server.removeServerInfo(name);
    }

    public boolean hasServer(String name) {
        return server.getServerInfo(name) != null;
    }

    public boolean hasSameServer(BridgeServer srv) {
        ServerInfo si = server.getServerInfo(srv.getName());
        if(si == null) return false;
        return si.getAddress().getPort() == srv.getPort();
    }

    @Override
    public BridgeServer determineServer(String serverName) {
        BridgeServer server = BridgeServer.readFromCache(serverName.toUpperCase());
        if(server == null) {
            if(hasServer(serverName)) removeServer(serverName);
            return null;
        }

        if(server.isProxy()) return null;

        if(!hasServer(server.getName()) || !hasSameServer(server)) addServer(server.getName(), server.getAddress());

        return server;
    }

    @Override
    public void connect(ICloudPlayer player, String serverName) {
        determineServer(serverName);
        getPlayerByCloudPlayer(player).connect(server.getServerInfo(serverName));
    }

    public ProxiedPlayer getPlayerByCloudPlayer(ICloudPlayer player) {
        return server.getPlayer(player.getUUID());
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
        Objects.requireNonNull(getPlayerByCloudPlayer(player)).disconnect();
    }

    @Override
    public void kick(ICloudPlayer player, String reason) {
        Objects.requireNonNull(getPlayerByCloudPlayer(player)).disconnect(new TextContainer(reason));
    }

    @Override
    public void sendActionbar(ICloudPlayer player, String message) {
        //WDPE can't do that :c
    }

    @Override
    public void sendToast(ICloudPlayer player, String title, String message) {
        getPlayerByCloudPlayer(player).sendToastMessage(title, message);
    }

    @Override
    public void runDelayed(Runnable code, int tickDelay) {
        server.getScheduler().scheduleDelayed(new Task() {
            @Override
            public void onRun(int i) {
                code.run();
            }

            @Override
            public void onCancel() {

            }
        }, tickDelay);
    }
}
