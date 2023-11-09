package me.scattermc.Zombies.commands;

import me.scattermc.Zombies.config.Message;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldName implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)){
            Message.of("general.not-player").send(sender);
            return true;
        }

        World world = player.getWorld();
        String worldName = world.getName();

        Message.of("&eYou are in world &d" + Message.capitalize(worldName)).send(player);
        return true;
    }
}
