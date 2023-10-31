package me.scattermc.Zombies.event.items;

import com.google.common.collect.ImmutableMap;
import me.scattermc.Zombies.Main;
import me.scattermc.Zombies.config.Message;
import me.scattermc.Zombies.event.EventGame;
import me.scattermc.Zombies.event.EventManager;
import me.scattermc.Zombies.event.EventState;
import me.scattermc.Zombies.player.Profile;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.UUID;

public class TrackCompass extends BukkitRunnable implements Listener {
    private final Main main;
    private ItemStack compass;
    private ItemMeta meta;
    private final EventManager eventManager;
    private final EventGame eventGame;
    public TrackCompass(Main main, EventManager eventManager){
        this.main = main;
        this.eventManager = main.manager().getEventManager();
        this.eventGame = eventManager.getEvent();

        this.compass = new ItemStack(Material.COMPASS);
        this.meta = compass.getItemMeta();

        if(meta != null) {
            meta.setDisplayName(Message.of("general.tracker-compass-name").toString());
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
                    super.runTaskTimer(main, 20L, 20L);
                } else {
                    if (!super.isCancelled()) {
                        super.cancel();
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        for (Player player : main.getServer().getOnlinePlayers()) {
            UUID closestPlayerUid = this.getClosestSurvivor(player.getLocation(), player.getUniqueId());

            if (closestPlayerUid != null) {
                Location closestPlayerLoc = getSurvivorFromUID(closestPlayerUid).getLocation();
                player.setCompassTarget(closestPlayerLoc);
            }
        }
    }

    private UUID getClosestSurvivor(Location loc, UUID exceptPlayerId) {
        if (Objects.requireNonNull(loc.getWorld()).getEnvironment() != World.Environment.NORMAL) {
            return null;
        }
        UUID closestSurvivor = null;
        double distanceToClosestSurvivor = 0.0D;
        double xLoc = loc.getX();
        double yLoc = loc.getY();

        for (Profile survivors : main.manager().getProfiles().getDataPlayers()) {
            if(!eventGame.isSurvivor(survivors)) break;
            Player survivor = survivors.player;

            if (survivor.getUniqueId() != exceptPlayerId && survivor.getGameMode() != GameMode.SPECTATOR) {
                double p2xLoc = survivor.getLocation().getX();
                double p2yLoc = survivor.getLocation().getY();
                double distance = Math.sqrt((p2yLoc - yLoc) * (p2yLoc - yLoc) + (p2xLoc - xLoc) * (p2xLoc - xLoc));

                if (closestSurvivor == null) {
                    distanceToClosestSurvivor = distance;
                    closestSurvivor = survivor.getUniqueId();
                } else {
                    if (distance < distanceToClosestSurvivor) {
                        distanceToClosestSurvivor = distance;
                        closestSurvivor = survivor.getUniqueId();
                    }
                }

                String updatedItemName = Message.of("general.tracker-compass-name").placeholders(
                        ImmutableMap.of("<distance>", String.valueOf(distanceToClosestSurvivor))).toString();

                meta.setDisplayName(updatedItemName);
                compass.setItemMeta(meta);
            }
        }

        return closestSurvivor;
    }

    private Player getSurvivorFromUID(UUID uid) {
        return main.getServer().getPlayer(uid);
    }
    public ItemStack getCompass() {
        return compass;
    }
}
