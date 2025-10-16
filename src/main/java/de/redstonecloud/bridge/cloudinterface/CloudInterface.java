package de.redstonecloud.bridge.cloudinterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.redstonecloud.api.components.ServerActions;
import de.redstonecloud.api.components.ServerStatus;
import de.redstonecloud.api.redis.broker.Broker;
import de.redstonecloud.api.redis.broker.BrokerHelper;
import de.redstonecloud.api.redis.broker.packet.defaults.communication.ClientAuthPacket;
import de.redstonecloud.api.redis.broker.packet.defaults.player.PlayerConnectPacket;
import de.redstonecloud.api.redis.broker.packet.defaults.player.PlayerDisconnectPacket;
import de.redstonecloud.api.redis.broker.packet.defaults.server.ServerActionPacket;
import de.redstonecloud.api.redis.broker.packet.defaults.server.ServerChangeStatusPacket;
import de.redstonecloud.api.redis.cache.Cache;
import de.redstonecloud.bridge.cloudinterface.components.BridgeExecutor;
import de.redstonecloud.bridge.cloudinterface.components.BridgePlayer;
import de.redstonecloud.bridge.cloudinterface.components.BridgeServer;
import de.redstonecloud.bridge.cloudinterface.redis.broker.ActionHandler;
import de.redstonecloud.bridge.cloudinterface.redis.broker.ProxyHandler;
import lombok.Getter;

import java.io.File;

@Getter
public class CloudInterface {
    private static CloudInterface INSTANCE;
    public static Gson GSON = new Gson();

    private String serverName;

    @Getter
    public static Cache cache;
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

        broker = new Broker(serverName, BrokerHelper.constructRegistry(), result);

        currentServerStartup = BridgeServer.readFromCache(serverName.toUpperCase());

        proxy = currentServerStartup.isProxy();

    }

    public void start(BridgeExecutor executorPlugin) {
        executor = executorPlugin;
        // broker.listen(serverName, BrokerHandler::handle);

        if(proxy) broker.listen("", ProxyHandler::handle);
        broker.listen("", ActionHandler::handle);

        executorPlugin.runDelayed(() -> new ClientAuthPacket()
                .setClientId(currentServerStartup.getName().toUpperCase())
                .setTo("cloud")
                .send(), 20);
    }

    public void shutdown() {
        broker.shutdown();
    }

    public void playerLogin(String name, String uuid, String ip) {
        new PlayerConnectPacket()
                .setPlayerName(name)
                .setUuid(uuid)
                .setIpAddress(ip)
                .setServer(currentServerStartup.getName().toUpperCase())
                .setTo("cloud")
                .send();
    }

    public void playerDisconnect(String uuid) {
        new PlayerDisconnectPacket()
                .setUuid(uuid)
                .setServer(currentServerStartup.getName().toUpperCase())
                .setTo("cloud")
                .send();
    }

    public void sendMessage(BridgePlayer pl, String message) {
        JsonObject extraData = new JsonObject();
        extraData.addProperty("message", message);

        new ServerActionPacket()
                .setAction(ServerActions.PLAYER_SEND_MESSAGE.name())
                .setPlayerUuid(pl.getUUID().toString())
                .setExtraData(extraData)
                .setTo(pl.getConnectedNetwork().getName())
                .send();
    }

    public void sendActionBar(BridgePlayer pl, String message) {
        JsonObject extraData = new JsonObject();
        extraData.addProperty("message", message);

        new ServerActionPacket()
                .setAction(ServerActions.PLAYER_ACTIONBAR.name())
                .setPlayerUuid(pl.getUUID().toString())
                .setExtraData(extraData)
                .setTo(pl.getConnectedNetwork().getName())
                .send();
    }

    public void sendTitle(BridgePlayer pl, String title) {
        JsonObject extraData = new JsonObject();
        extraData.addProperty("title", title);

        new ServerActionPacket()
                .setAction(ServerActions.PLAYER_SEND_TITLE.name())
                .setPlayerUuid(pl.getUUID().toString())
                .setExtraData(extraData)
                .setTo(pl.getConnectedNetwork().getName())
                .send();
    }

    public void sendToast(BridgePlayer pl, String title, String content) {
        JsonObject extraData = new JsonObject();
        extraData.addProperty("title", title);
        extraData.addProperty("content", content);

        new ServerActionPacket()
                .setAction(ServerActions.PLAYER_TOAST.name())
                .setPlayerUuid(pl.getUUID().toString())
                .setExtraData(extraData)
                .setTo(pl.getConnectedNetwork().getName())
                .send();
    }

    public void connect(BridgePlayer pl, String server) {
        JsonObject extraData = new JsonObject();
        extraData.addProperty("server", server);

        new ServerActionPacket()
                .setAction(ServerActions.PLAYER_CONNECT.name())
                .setPlayerUuid(pl.getUUID().toString())
                .setExtraData(extraData)
                .setTo(pl.getConnectedNetwork().getName())
                .send();
    }

    public void kick(BridgePlayer pl, String reason) {
        JsonObject extraData = new JsonObject();
        extraData.addProperty("reason", reason);

        new ServerActionPacket()
                .setAction(ServerActions.PLAYER_KICK.name())
                .setPlayerUuid(pl.getUUID().toString())
                .setExtraData(extraData)
                .setTo(pl.getConnectedNetwork().getName())
                .send();
    }

    public void changeStatus(String server, ServerStatus newStatus) {
        new ServerChangeStatusPacket()
                .setServer(server)
                .setNewStatus(newStatus.name())
                .setTo("cloud")
                .send();
    }

    public void changeStatus(ServerStatus newStatus) {
        changeStatus(currentServerStartup.getName().toUpperCase(), newStatus);
    }
}
