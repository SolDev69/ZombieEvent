package me.scattermc.Zombies.event.items;

import com.google.common.collect.ImmutableMap;
import me.scattermc.Zombies.Main;
import me.scattermc.Zombies.config.Message;
import me.scattermc.Zombies.event.EventGame;
import me.scattermc.Zombies.event.EventManager;
import me.scattermc.Zombies.event.EventState;
import me.scattermc.Zombies.player.Profile;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrackCompass extends BukkitRunnable implements Listener {
    private final Main main;
    private ItemStack compass;
    private ItemMeta meta;
    private BukkitTask task;
    private final EventManager eventManager;
    private final EventGame eventGame;
    private Map<UUID, Double> distanceMap;
    public TrackCompass(Main main, EventManager eventManager){
        this.main = main;
        this.eventManager = main.manager().getEventManager();
        this.eventGame = eventManager.getEvent();
        this.distanceMap = new HashMap<>();

        this.compass = new ItemStack(Material.COMPASS);
        this.meta = compass.getItemMeta();

        if(meta != null) {
            String updatedItemName = Message.of("general.tracker-compass-name").placeholders(
                    ImmutableMap.of("<distance>", String.valueOf(0D))).toString();

            meta.setDisplayName(updatedItemName);
        }

        compass.setItemMeta(meta);
    }
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent e){
        Player player = e.getPlayer();
        Profile zombie = main.manager().getProfiles().getDataPlayer(player);

        PlayerInventory inv = player.getInventory();

        ItemStack mainHand = inv.getItemInMainHand();
        ItemStack offHand = inv.getItemInOffHand();

        if(eventGame.getEventState() == EventState.STARTED){
            if(eventGame.isZombie(zombie)) {
                if (mainHand.isSimilar(compass) || offHand.isSimilar(compass)) {
                    if(task == null) {
                        task = super.runTaskTimer(main, 20L, 20L);
                    }
                }
            }
        }
    }
    @Override
    public void run() {
        for (Player player : main.getServer().getOnlinePlayers()) {
            double maxRange = main.files().getConfig().get().getDouble("general.track-maxrange");
            Player nearest = getNearestPlayer(player, maxRange);
            Profile nearestPro = main.manager().getProfiles().getDataPlayer(nearest);

            if (nearest != null && eventGame.isSurvivor(nearestPro)) { //check only survivors
                double distance = distanceMap.get(player.getUniqueId());
                String updatedItemName = Message.of("general.tracker-compass-name").placeholders(
                        ImmutableMap.of("<distance>", Double.toString(distance))).toString();

                meta.setDisplayName(updatedItemName);
                compass.setItemMeta(meta);
                player.setCompassTarget(nearest.getLocation());
            }
        }
    }
    public Player getNearestPlayer(Player player, double range) {
        double distance = Double.POSITIVE_INFINITY;
        Player target = null;

        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (!(entity instanceof Player)) continue;
            if (entity == player) continue;

            Profile self = main.manager().getProfiles().getDataPlayer(player);
            Profile nearest = main.manager().getProfiles().getDataPlayer((Player) entity);
            if(eventGame.isZombie(nearest) || eventGame.isZombie(self)) continue; //skip zombies
            double distanceTo = player.getLocation().distance(entity.getLocation());

            if (distanceTo < distance) {
                distance = distanceTo;
                target = (Player) entity;
            }

            distanceMap.put(player.getUniqueId(), distance);
        }
        return target;
    }
    public ItemStack getCompass() {
        return compass;
    }
}
