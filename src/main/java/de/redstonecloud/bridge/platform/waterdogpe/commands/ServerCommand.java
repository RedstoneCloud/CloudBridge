package de.redstonecloud.bridge.platform.waterdogpe.commands;

import de.redstonecloud.bridge.cloudinterface.CloudInterface;
import de.redstonecloud.bridge.cloudinterface.components.BridgeServer;
import dev.waterdog.waterdogpe.command.Command;
import dev.waterdog.waterdogpe.command.CommandSender;
import dev.waterdog.waterdogpe.command.CommandSettings;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import dev.waterdog.waterdogpe.utils.types.TextContainer;
import org.cloudburstmc.protocol.bedrock.data.command.CommandOverloadData;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParam;
import org.cloudburstmc.protocol.bedrock.data.command.CommandParamData;

public class ServerCommand extends Command {

    public ServerCommand() {
        super("server", CommandSettings.builder()
                .setDescription("waterdog.command.server.description")
                .setUsageMessage("waterdog.command.server.usage")
                .setPermission("waterdog.command.server.permission")
                .build());
    }

    @Override
    public boolean onExecute(CommandSender sender, String alias, String[] args) {
        if (args.length < 1 || !sender.isPlayer() && args.length < 2) {
            return false;
        }

        BridgeServer server = CloudInterface.getExecutor().determineServer(args[0]);
        if (server == null) {
            sender.sendMessage("§cServer not found!");
            return true;
        }

        ProxiedPlayer player;
        if (sender.isPlayer() && args.length < 2) {
            player = (ProxiedPlayer) sender;
        } else {
            if (!sender.hasPermission("waterdog.command.server.permission.other")) {
                sender.sendMessage(new TextContainer("§cYou don't have the permission to move other players."));
                return false;
            }

            player = sender.getProxy().getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage("§cPlayer not found!");
                return true;
            }
            sender.sendMessage(new TextContainer("§aPlayer {%0} was sent to {%1} server!", player.getName(), server.getName()));
        }

        player.connect(sender.getProxy().getServerInfo(server.getName()));
        return true;
    }

    @Override
    protected CommandOverloadData[] buildCommandOverloads() {
        CommandParamData server = new CommandParamData();
        server.setName("server");
        server.setOptional(false);
        server.setType(CommandParam.ID);

        CommandParamData player = new CommandParamData();
        player.setName("player");
        player.setOptional(true);
        player.setType(CommandParam.NAME_ARG);
        return new CommandOverloadData[]{new CommandOverloadData(false, new CommandParamData[]{server, player})};
    }
}