package de.redstonecloud.bridge.cloudinterface.components;

import com.google.common.net.HostAndPort;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.redstonecloud.api.components.ICloudPlayer;
import de.redstonecloud.api.components.ICloudServer;
import de.redstonecloud.api.components.cache.PlayerData;
import de.redstonecloud.api.util.Keys;
import de.redstonecloud.bridge.cloudinterface.CloudInterface;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class BridgePlayer implements ICloudPlayer {
    private HostAndPort address;
    private BridgeServer network;
    private BridgeServer server;
    private UUID uuid;
    private String name;
    private JsonElement extraData;

    public static BridgePlayer readFromCache(String uuid) {
        String cachedData = CloudInterface.getCache().get(Keys.CACHE_PREFIX_PLAYER + uuid);

        if(cachedData == null ||cachedData.isEmpty()) return null;

        JsonObject json = CloudInterface.GSON.fromJson(cachedData, JsonObject.class);

        PlayerData data = PlayerData.parse(json);

        BridgePlayer server = BridgePlayer.builder()
                .uuid(data.uuid())
                .name(data.name())
                .address(HostAndPort.fromString(data.address()))
                .network(BridgeServer.readFromCache(data.network()))
                .server(BridgeServer.readFromCache(data.server()))
                .extraData(data.extraData())
                .build();

        return server;
    }

    @Override
    public HostAndPort getAddress() {
        return address;
    }

    @Override
    public ICloudServer getConnectedNetwork() {
        return network;
    }

    @Override
    public ICloudServer getConnectedServer() {
        return server;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public void sendMessage(String message) {
        CloudInterface.getInstance().sendMessage(this, message);
    }

    @Override
    public void connect(String server) {
        CloudInterface.getInstance().connect(this, server);
    }

    @Override
    public void disconnect(String reason) {

    }

    @Override
    public String getName() {
        return name;
    }
}
