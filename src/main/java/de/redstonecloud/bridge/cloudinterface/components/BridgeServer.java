package de.redstonecloud.bridge.cloudinterface.components;

import com.google.common.net.HostAndPort;
import com.google.gson.JsonObject;
import de.redstonecloud.api.components.ICloudServer;
import de.redstonecloud.api.components.ServerStatus;
import de.redstonecloud.api.components.cache.ServerData;
import de.redstonecloud.api.util.Keys;
import de.redstonecloud.bridge.cloudinterface.CloudInterface;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class BridgeServer implements ICloudServer {
    public static BridgeServer readFromCache(String serverName) {
        String cachedData = CloudInterface.getCache().get(Keys.CACHE_PREFIX_SERVER + serverName.toUpperCase());

        if(cachedData == null ||cachedData.isEmpty()) return null;

        JsonObject json = CloudInterface.GSON.fromJson(cachedData, JsonObject.class);

        ServerData data = ServerData.parse(json);

        BridgeServer server = BridgeServer.builder()
                .template(data.template())
                .name(data.name())
                .uuid(data.uuid())
                .port(data.port())
                .status(ServerStatus.valueOf(data.status()))
                .type(data.serverType())
                .isProxy(data.proxy())
                .extraData(data.extraData())
                .build();

        return server;
    }

    protected String template;
    protected String name;
    protected int port;
    protected ServerStatus status;
    protected String type;
    protected long createdAt;
    protected boolean isProxy;
    protected UUID uuid;
    protected JsonObject extraData;

    @Override
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public HostAndPort getAddress() {
        return HostAndPort.fromParts("0.0.0.0", port);
    }

    @Override
    public ServerStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(ServerStatus status){}

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
