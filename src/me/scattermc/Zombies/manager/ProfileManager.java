package me.scattermc.Zombies.manager;

import me.scattermc.Zombies.player.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class ProfileManager {
    private Set<Profile> dataSet = new HashSet<>();
    public ProfileManager() {
        Bukkit.getOnlinePlayers().forEach(this::add);
    }
    public Profile getDataPlayer(Player player) {
        return dataSet.stream().filter(dataPlayer -> dataPlayer.player == player).findFirst().orElse(null);
    }
    public Set<Profile> getDataPlayers() {
        return dataSet;
    }
    public void add(Player player) {
        dataSet.add(new Profile(player));
    }
    public void remove(Player player) {
        dataSet.removeIf(dataPlayer -> dataPlayer.player == player);
    }
}
