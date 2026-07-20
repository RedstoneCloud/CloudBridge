package de.redstonecloud.bridge.platform.waterdogpe.commands;

import de.redstonecloud.bridge.cloudinterface.CloudInterface;
import de.redstonecloud.bridge.cloudinterface.components.BridgeServer;
import dev.waterdog.waterdogpe.command.Command;
import dev.waterdog.waterdogpe.command.CommandSender;
import dev.waterdog.waterdogpe.command.CommandSettings;
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import dev.waterdog.waterdogpe.utils.types.TextContainer;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOverloadData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParam;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamData;

public class SendCommand extends Command {

    public SendCommand() {
        super("wdsend", CommandSettings.builder()
                .setDescription("waterdog.command.send.description")
                .setUsageMessage("waterdog.command.send.usage")
                .setPermission("waterdog.command.send.permission")
                .build());
    }

    @Override
    public boolean onExecute(CommandSender sender, String alias, String[] args) {
        if (args.length < 1 || !sender.isPlayer() && args.length < 2) {
            return false;
        }

        BridgeServer srv = CloudInterface.getExecutor().determineServer(args[0]);
        if (srv == null) {
            sender.sendMessage("§cServer not found!");
            return true;
        }

        ServerInfo server = sender.getProxy().getServerInfo(srv.getName());
        if (server == null) {
            sender.sendMessage(new TextContainer("§cServer {%0} was not found!", args[0]));
            return true;
        }

        ServerInfo targetServer;
        if (sender.isPlayer() && args.length < 2) {
            targetServer = ((ProxiedPlayer) sender).getServerInfo();
        } else {
            BridgeServer tSrv = CloudInterface.getExecutor().determineServer(args[1]);
            if (tSrv == null) {
                sender.sendMessage("§cServer not found!");
                return true;
            }

            targetServer = sender.getProxy().getServerInfo(tSrv.getName());
        }

        if (targetServer == null) {
            sender.sendMessage(new TextContainer("§cCould not find target server {%0}!", args[1]));
            return true;
        }

        for (ProxiedPlayer player : targetServer.getPlayers()) {
            player.connect(server);
        }
        return true;
    }

    @Override
    protected CommandOverloadData[] buildCommandOverloads() {
        CommandParamData destination = new CommandParamData();
        destination.setName("destination");
        destination.setOptional(false);
        destination.setType(CommandParam.ID);

        CommandParamData target = new CommandParamData();
        target.setName("target");
        target.setOptional(false);
        target.setType(CommandParam.ID);

        return new CommandOverloadData[]{new CommandOverloadData(false, new CommandParamData[]{destination, target})};
    }
}