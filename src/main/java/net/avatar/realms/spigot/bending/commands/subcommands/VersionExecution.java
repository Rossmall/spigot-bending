package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.command.CommandSender;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.commands.BendingCommand;

public class VersionExecution extends BendingCommand {

	public VersionExecution() {
		super();
		this.command = "version";
		this.aliases.add("v");
		this.aliases.add("ver");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (sender.hasPermission("bending.command.version")) {
			sender.sendMessage("Bending v" + Bending.plugin.getDescription().getVersion());
			sender.sendMessage("Authors : Koudja & Noko");
		}
		return true;
	}

	@Override
	public void printUsage(CommandSender sender) {
		if (sender.hasPermission("bending.command.version")) {
			sender.sendMessage("/bending version");
		}
	}

}