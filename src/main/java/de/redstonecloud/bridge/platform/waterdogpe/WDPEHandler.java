package de.redstonecloud.bridge.platform.waterdogpe;

import de.redstonecloud.api.redis.broker.message.Message;
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
import java.util.concurrent.ExecutionException;
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
        return fetchServer();
    }

    @Override
    public ServerInfo getFallbackServer(ProxiedPlayer player, ServerInfo oldServer, ReconnectReason reason, String kickMessage) {
        return fetchServer();
    }

    @Override
    public ServerInfo getFallbackServer(
            @NonNull ProxiedPlayer player,
            @NonNull ServerInfo oldServer,
            @NonNull String kickMessage
    ) {
        return fetchServer();
    }

    @Override
    public ServerInfo determineServer(ProxiedPlayer player) {
        return fetchServer();
    }

    public static ServerInfo fetchServer() {
        if(!CloudInterface.getBridgeConfig().has("hub_template")) return null;
        CompletableFuture<String> name = new CompletableFuture<>();

        new Message.Builder()
                .setTo("cloud")
                .append("template:getbest")
                .append(CloudInterface.getBridgeConfig().get("hub_template").getAsString())
                .build().send((Message resp) -> {
                    name.complete(resp.getArguments()[0]);
                });

        try {
            BridgeServer srv = CloudInterface.getExecutor().determineServer(name.completeOnTimeout("", 2, TimeUnit.SECONDS).get().toUpperCase());
            return ProxyServer.getInstance().getServerInfo(srv.getName());
        } catch (Exception e) {
            return null;
        }
    }
}
