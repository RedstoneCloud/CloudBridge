package de.redstonecloud.bridge.cloudinterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.pierreschwang.nettypacket.event.EventRegistry;
import de.redstonecloud.api.components.ICloudPlayer;
import de.redstonecloud.api.components.ServerActions;
import de.redstonecloud.api.components.ServerStatus;
import de.redstonecloud.api.netty.NettyHelper;
import de.redstonecloud.api.netty.client.NettyClient;
import de.redstonecloud.api.netty.packet.communication.ClientAuthPacket;
import de.redstonecloud.api.netty.packet.player.PlayerConnectPacket;
import de.redstonecloud.api.netty.packet.player.PlayerDisconnectPacket;
import de.redstonecloud.api.netty.packet.server.ServerActionRequest;
import de.redstonecloud.api.netty.packet.server.ServerChangeStatusPacket;
import de.redstonecloud.api.redis.broker.Broker;
import de.redstonecloud.api.redis.cache.Cache;
import de.redstonecloud.bridge.cloudinterface.broker.BrokerHandler;
import de.redstonecloud.bridge.cloudinterface.components.BridgeExecutor;
import de.redstonecloud.bridge.cloudinterface.components.BridgePlayer;
import de.redstonecloud.bridge.cloudinterface.components.BridgeServer;
import de.redstonecloud.bridge.cloudinterface.netty.ActionHandler;
import de.redstonecloud.bridge.cloudinterface.netty.ProxyHandler;
import lombok.Getter;
import org.json.JSONObject;

import java.io.File;

@Getter
public class CloudInterface {
    private static CloudInterface INSTANCE;
    public static Gson GSON = new Gson();

    private String serverName;

    @Getter
    public static Cache cache;
    @Getter
    public static NettyClient netty;
    @Getter
    public static Broker broker;
    public static boolean proxy;

    @Getter public static JsonObject bridgeConfig;

    @Getter
    protected static BridgeExecutor executor;

    private BridgeServer currentServerStartup;

    public static CloudInterface getInstance() {
        if(INSTANCE == null) INSTANCE = new CloudInterface();

        return INSTANCE;
    }

    private CloudInterface() {
        String workingDir = new File(System.getProperty("user.dir")).getName();
        bridgeConfig = new Gson().fromJson(System.getenv("BRIDGE_CFG"), JsonObject.class);

        serverName = workingDir.toLowerCase();

        cache = new Cache();

        String[] parts = serverName.split("-");
        String[] result = new String[parts.length + 1];

        StringBuilder pattern = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                pattern.append("-");
            }
            pattern.append(parts[i]);
            result[i + 1] = pattern.toString() + "-*";
        }

        result[0] = "*";  // First element is always "*"
        result[result.length - 1] = serverName;

        broker = new Broker(serverName, result);

        currentServerStartup = BridgeServer.readFromCache(serverName.toUpperCase());
        netty = new NettyClient(currentServerStartup.getName().toUpperCase(), NettyHelper.constructRegistry(), new EventRegistry());
        netty.setPort(Integer.valueOf(System.getenv("NETTY_PORT"))).bind();

        proxy = currentServerStartup.isProxy();

    }

    public void start(BridgeExecutor executorPlugin) {
        executor = executorPlugin;
        broker.listen(serverName, BrokerHandler::handle);

        if(proxy) netty.getEventRegistry().registerEvents(new ProxyHandler(netty));
        netty.getEventRegistry().registerEvents(new ActionHandler(netty));

        executorPlugin.runDelayed(() -> {
            ClientAuthPacket packet = new ClientAuthPacket();
            packet.setClientId(currentServerStartup.getName().toUpperCase());

            netty.sendPacket(packet);
        }, 20);
    }

    public void shutdown() {
        broker.shutdown();
    }

    public void playerLogin(String name, String uuid, String ip) {
        PlayerConnectPacket pck = new PlayerConnectPacket()
                .setPlayerName(name)
                .setUuid(uuid)
                .setIpAddress(ip)
                .setServer(currentServerStartup.getName().toUpperCase());

        netty.sendPacket(pck);
    }

    public void playerDisconnect(String uuid) {
        PlayerDisconnectPacket pck = new PlayerDisconnectPacket()
                .setUuid(uuid)
                .setServer(currentServerStartup.getName().toUpperCase());

        netty.sendPacket(pck);
    }

    public void sendMessage(BridgePlayer pl, String message) {
        netty.sendPacket(new ServerActionRequest()
                .setAction(ServerActions.PLAYER_SEND_MESSAGE.name())
                .setServer(pl.getConnectedNetwork().getName())
                .setPlayerUuid(pl.getUUID())
                .setExtraData(new JSONObject().put("message", message)));
    }

    public void sendActionBar(BridgePlayer pl, String message) {
        netty.sendPacket(new ServerActionRequest()
                .setAction(ServerActions.PLAYER_ACTIONBAR.name())
                .setServer(pl.getConnectedNetwork().getName())
                .setPlayerUuid(pl.getUUID())
                .setExtraData(new JSONObject().put("message", message)));
    }

    public void sendTitle(BridgePlayer pl, String title) {
        netty.sendPacket(new ServerActionRequest()
                .setAction(ServerActions.PLAYER_SEND_TITLE.name())
                .setServer(pl.getConnectedNetwork().getName())
                .setPlayerUuid(pl.getUUID())
                .setExtraData(new JSONObject().put("title", title)));
    }

    public void sendToast(BridgePlayer pl, String title, String content) {
        netty.sendPacket(new ServerActionRequest()
                .setAction(ServerActions.PLAYER_TOAST.name())
                .setServer(pl.getConnectedNetwork().getName())
                .setPlayerUuid(pl.getUUID())
                .setExtraData(new JSONObject().put("title", title).put("content", content)));
    }

    public void connect(BridgePlayer pl, String server) {
        netty.sendPacket(new ServerActionRequest()
                .setAction(ServerActions.PLAYER_CONNECT.name())
                .setServer(pl.getConnectedNetwork().getName())
                .setPlayerUuid(pl.getUUID())
                .setExtraData(new JSONObject().put("server", server)));
    }

    public void kick(BridgePlayer pl, String reason) {
        netty.sendPacket(new ServerActionRequest()
                .setAction(ServerActions.PLAYER_KICK.name())
                .setServer(pl.getConnectedNetwork().getName())
                .setPlayerUuid(pl.getUUID())
                .setExtraData(new JSONObject().put("reason", reason)));
    }

    public void changeStatus(String server, ServerStatus newStatus) {
        netty.sendPacket(new ServerChangeStatusPacket()
                .setServer(server)
                .setNewStatus(newStatus.name())
        );
    }

    public void changeStatus(ServerStatus newStatus) {
        changeStatus(currentServerStartup.getName().toUpperCase(), newStatus);
    }
}
