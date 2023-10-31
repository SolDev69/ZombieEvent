package me.scattermc.Zombies.utils;

import me.scattermc.Zombies.Main;
import me.scattermc.Zombies.config.Message;
import me.scattermc.Zombies.map.LobbyType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PlayerUtils {
    public static void teleportPlayer(Player player, LobbyType lobbyType){
        if(player != null && lobbyType != null){
            switch(lobbyType){
                case MAIN -> {
                    String mainWorldName = Main.getInstance().files().getConfig().get().getString("general.main-world");

                    if(mainWorldName != null) {
                        World world = Bukkit.getWorld(mainWorldName);
                        if (world != null) {
                            player.teleport(world.getSpawnLocation());
                            Message.of("player.teleport-to-mainlobby").send(player);
                            Bukkit.getLogger().info("DEBUG ++: "+"should have teleported player");
                        }
                    }
                }
                case ZOMBIE -> {
                    String zombieLobby = Main.getInstance().files().getConfig().get().getString("general.zombie-lobby");

                    if(zombieLobby != null) {
                        World world = Bukkit.getWorld(zombieLobby);
                        if (world != null) {
                            player.teleport(world.getSpawnLocation());
                            Message.of("player.teleport-to-zombielobby").send(player);
                        }
                    }
                }
                case GAME -> {
                    String cityMap = Main.getInstance().files().getConfig().get().getString("general.game-world");

                    if(cityMap != null) {
                        World world = Bukkit.getWorld(cityMap);
                        if (world != null) {
                            player.teleport(world.getSpawnLocation());
                            Message.of("player.teleport-to-zombiegameworld").send(player);
                        }
                    }
                }
            }
        }else{
            Message.of("&cError occurred trying to teleport player to &b" + lobbyType + " world").send(Bukkit.getConsoleSender());
        }
    }
}
