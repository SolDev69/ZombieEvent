package me.scattermc.Zombies.map;

import me.scattermc.Zombies.Main;
import me.scattermc.Zombies.config.Message;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class GameMap {
    private final Main main;
    private String worldName;
    private WorldCreator god;
    private boolean loaded = false;
    public GameMap(Main main, String worldName){
        this.main = main;
        this.worldName = worldName;
        load();
    }
    private void load(){
        try {
            god = new WorldCreator(worldName);
            World world = god.createWorld();

            if(world != null) {
                world.setAutoSave(false);
                Message.of("&aLoaded Map with name= &d" + worldName).send(Bukkit.getConsoleSender());
                loaded = true;
            } else {
                loaded = false;
                Message.of("&cFailed to load &d" + worldName + " &ccheck the world directory and make sure it's valid.").send(Bukkit.getConsoleSender());
            }
        }catch(Exception e){
            e.printStackTrace();
            loaded = false;
            Message.of("&cTrouble loading &d" + worldName + " &ccheck the world directory and make sure its valid.").send(Bukkit.getConsoleSender());
        }
    }
    public void unload(){
        try {
            if(loaded) {
                Bukkit.unloadWorld(worldName, false);
                loaded = false;

                Message.of("&d" + worldName + " &bsuccessfully recycled, &aready to be used again at your desire.").send(Bukkit.getConsoleSender());
            }
        }catch(Exception e){
            e.printStackTrace();
            loaded = false;
            Message.of("&cFailure resetting &bMap with worldName being: &d" + worldName).send(Bukkit.getConsoleSender());
        }
    }
    public World getBukkitWorld(){
        return Bukkit.getWorld(worldName);
    }
}
