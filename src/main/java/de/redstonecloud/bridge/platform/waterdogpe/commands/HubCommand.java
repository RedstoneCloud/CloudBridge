package de.redstonecloud.bridge.platform.waterdogpe.commands;

import de.redstonecloud.bridge.cloudinterface.CloudInterface;
import de.redstonecloud.bridge.platform.waterdogpe.WDPEHandler;
import dev.waterdog.waterdogpe.command.Command;
import dev.waterdog.waterdogpe.command.CommandSender;
import dev.waterdog.waterdogpe.command.CommandSettings;
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;

public class HubCommand extends Command {
    public HubCommand() {
        super("hub", CommandSettings.builder()
                .setDescription(CloudInterface.getBridgeConfig().has("hubcommand_desc") ? CloudInterface.getBridgeConfig().get("hubcommand_desc").getAsString() : "No desc provided")
                .setAliases("lobby")
                .build());
    }

    @Override
    public boolean onExecute(CommandSender sender, String alias, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) return false;
        ServerInfo hub = WDPEHandler.fetchServer();
        if(hub == null) {
            sender.sendMessage(CloudInterface.getBridgeConfig().has("hubcommand_no_hub_available") ? CloudInterface.getBridgeConfig().get("hubcommand_no_hub_available").getAsString() : "No hub available");
            return true;
        }

        ((ProxiedPlayer) sender).connect(hub);
        return true;
    }
}
