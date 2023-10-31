package me.scattermc.Zombies;

import fr.mrmicky.fastboard.FastBoard;
import me.scattermc.Zombies.config.InitFiles;
import me.scattermc.Zombies.event.EventState;
import me.scattermc.Zombies.manager.Manager;
import me.scattermc.Zombies.player.Profile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Main extends JavaPlugin {
    public static Main instance;
    private InitFiles files;
    private Manager manager;
    public void onEnable(){
        instance = this;
        register();
    }
    public void onDisable(){
        ConsoleCommandSender receiver = Bukkit.getConsoleSender();
        receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Shutdown success. &3(*Zombies)"));

        if(manager == null){
            manager = new Manager(this);
        }

        if(manager.getEventManager() != null && manager.getEventManager().getEvent() != null){
            if(manager.getEventManager().getEvent().getEventState() == EventState.STARTED){
                manager.getEventManager().getEvent().forceStop(); //reset the eventGame on disable
            }
        }

        for(Profile profile : manager.getProfiles().getDataPlayers()){
            Player player = profile.player;
            String name = player.getName();

            for(Team teams : Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard().getTeams()){
                if(teams.hasEntry(name)){
                    teams.removeEntry(name);
                }
            }

            if(player.isOnline()){
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }

            for(Map.Entry<UUID, FastBoard> entry : profile.getScoreboard().getPlayerBoards().entrySet()){
                entry.getValue().delete();
                profile.getScoreboard().getPlayerBoards().clear(); //reset all scoreboards
            }
        }
    }
    private void register(){
        ConsoleCommandSender receiver = Bukkit.getConsoleSender();
        receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccessfully registered hooks for Zombies plugin" + "\n" +
                " &bMade by &6&lScatterMC Network &4(Fleekinq)"));

        files = new InitFiles(this);
        manager = new Manager(this);
    }
    public InitFiles files(){
        return files;
    }
    public Manager manager() {
        return manager;
    }
    public static Main getInstance() {
        return instance;
    }
}
