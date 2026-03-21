package de.redstonecloud.bridge.platform.waterdogpe;

import com.google.gson.JsonObject;
import de.redstonecloud.api.redis.broker.packet.defaults.template.BestTemplateResultPacket;
import de.redstonecloud.api.redis.broker.packet.defaults.template.GetBestTemplatePacket;
import de.redstonecloud.bridge.cloudinterface.CloudInterface;
import de.redstonecloud.bridge.cloudinterface.components.BridgeServer;
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.network.connection.handler.IForcedHostHandler;
import dev.waterdog.waterdogpe.network.connection.handler.IJoinHandler;
import dev.waterdog.waterdogpe.network.connection.handler.IReconnectHandler;
import dev.waterdog.waterdogpe.network.connection.handler.ReconnectReason;
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import jline.internal.Nullable;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WDPEHandler implements IForcedHostHandler, IReconnectHandler, IJoinHandler {

    private final ProxyServer proxyServer;

    public WDPEHandler(
            @NonNull ProxyServer proxyServer
    ) {
        this.proxyServer = proxyServer;
    }

    @Override
    public ServerInfo resolveForcedHost(@Nullable String domain, @NonNull ProxiedPlayer player) {
        return fetchServer(player);
    }

    @Override
    public ServerInfo getFallbackServer(ProxiedPlayer player, ServerInfo oldServer, ReconnectReason reason, String kickMessage) {
        return fetchServer(player);
    }

    @Override
    public ServerInfo getFallbackServer(
            @NonNull ProxiedPlayer player,
            @NonNull ServerInfo oldServer,
            @NonNull String kickMessage
    ) {
        return fetchServer(player);
    }

    @Override
    public ServerInfo determineServer(ProxiedPlayer player) {
        return fetchServer(player);
    }

    public static ServerInfo fetchServer(ProxiedPlayer player) {
        JsonObject cfg = CloudInterface.getBridgeConfig();
        ServerInfo fallback = fetchFallback();
        if(!cfg.has("hub_template")) return fallback;
        boolean fallbackOnJoin = !cfg.has("fallback_on_join") || cfg.get("fallback_on_join").getAsBoolean();

        if(!cfg.has("hub_template"))
            return fallback;

        ServerInfo hub = fetchHub();
        if(hub != null) return hub;

        if(player.getDownstreamConnection() != null && fallbackOnJoin)
            return fallback;

        return null;
    }

    public static ServerInfo fetchHub() {
        CompletableFuture<String> name = new CompletableFuture<>();

        new GetBestTemplatePacket(CloudInterface.getBridgeConfig().get("hub_template").getAsString())
                .setTo("cloud")
                .send(BestTemplateResultPacket.class, response -> name.complete(response.getServer()));

        try {
            BridgeServer srv = CloudInterface.getExecutor().determineServer(name.completeOnTimeout("", 3, TimeUnit.SECONDS).get().toUpperCase());
            if(srv == null) return null;

            return ProxyServer.getInstance().getServerInfo(srv.getName());
        } catch (Exception e) {
            return null;
        }
    }

    public static ServerInfo fetchFallback() {
        JsonObject cfg = CloudInterface.getBridgeConfig();
        if(!cfg.has("fallback_name")) return null;
        return ProxyServer.getInstance().getServerInfo(cfg.get("fallback_name").getAsString());
    }
}
