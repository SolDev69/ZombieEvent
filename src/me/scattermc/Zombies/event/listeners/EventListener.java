package me.scattermc.Zombies.event.listeners;

import me.scattermc.Zombies.Main;
import me.scattermc.Zombies.config.Message;
import me.scattermc.Zombies.event.EventGame;
import me.scattermc.Zombies.event.EventManager;
import me.scattermc.Zombies.event.EventState;
import me.scattermc.Zombies.map.LobbyType;
import me.scattermc.Zombies.player.Profile;
import me.scattermc.Zombies.utils.PlayerUtils;
import me.scattermc.Zombies.utils.WorldUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class EventListener implements Listener {
    private final Main main;
    private final EventManager eventManager;
    private final EventGame eventGame;
    public static Map<Profile, Block> survivorBlockMap = new HashMap<>();
    public static Map<Profile, Block> zombieBlockMap = new HashMap<>();
    public EventListener(Main main, EventGame eventGame, EventManager eventManager){
        this.main = main;
        this.eventManager = eventManager;
        this.eventGame = eventGame;
    }
    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Profile profile = main.manager().getProfiles().getDataPlayer(player);

        if (eventGame.getEventState() == EventState.STARTED) {
            if (eventGame.isSurvivor(profile)) {
                survivorBlockMap.put(profile, e.getBlockPlaced());
            } else if (eventGame.isZombie(profile)) {
                zombieBlockMap.put(profile, e.getBlockPlaced());
            }
        }
    }
    @EventHandler
    public void onPlayerBreak(BlockBreakEvent e){
        Player player = e.getPlayer();
        Profile profile = main.manager().getProfiles().getDataPlayer(player);

        if (eventGame.getEventState() == EventState.STARTED) {
            Block survivorPlacedBlock = survivorBlockMap.get(profile);

            if (!e.getBlock().equals(survivorPlacedBlock)) {
                if (eventGame.isZombie(profile)) {
                    e.setCancelled(true);
                    Message.of("player.cannot-break-zombieblocks").send(profile.player);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        Player player = e.getEntity();
        Profile profile = main.manager().getProfiles().getDataPlayer(player);

        if (eventGame.getEventState() == EventState.STARTED) {
            if (eventGame.isSurvivor(profile) && WorldUtils.isDay()) {
                eventGame.setZombie(profile);
                PlayerUtils.teleportPlayer(profile.player, LobbyType.ZOMBIE);
            } else if (eventGame.isSurvivor(profile) && !WorldUtils.isDay()) {
                eventGame.setZombie(profile);
                PlayerUtils.teleportPlayer(profile.player, LobbyType.GAME);
            }
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        Player player = e.getPlayer();
        Profile profile = main.manager().getProfiles().getDataPlayer(player);

        if (eventGame.getEventState() == EventState.STARTED) {
            if (!WorldUtils.isDay()) {
                if(e.getClickedBlock() != null) {
                    if (e.getClickedBlock().getType() == Material.CHEST) {
                        e.setCancelled(true);
                        Message.of("player.cannot-openchest").send(profile.player);
                    }
                }
            } else {
                e.setCancelled(false);
            }
        }
    }
}
