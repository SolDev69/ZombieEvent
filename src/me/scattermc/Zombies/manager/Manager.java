package me.scattermc.Zombies.manager;

import me.scattermc.Zombies.Main;
import me.scattermc.Zombies.commands.EventCommand;
import me.scattermc.Zombies.commands.WorldName;
import me.scattermc.Zombies.event.EventManager;
import me.scattermc.Zombies.player.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;
public class Manager {
    private final Main main;
    private ProfileManager profileManager;
    private EventManager eventManager;
    public Manager(Main main){
        this.main = main;
        register();
    }
    private void register(){
        eventManager = new EventManager(main);
        profileManager = new ProfileManager();

        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(Main.getInstance(), eventManager), Main.getInstance());

        Objects.requireNonNull(main.getCommand("event")).setExecutor(new EventCommand(main, eventManager));
        Objects.requireNonNull(main.getCommand("worldname")).setExecutor(new WorldName());
    }
    public EventManager getEventManager() {
        return eventManager;
    }
    public ProfileManager getProfiles() {
        return profileManager;
    }
}
