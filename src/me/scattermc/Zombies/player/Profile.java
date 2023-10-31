package me.scattermc.Zombies.player;

import me.scattermc.Zombies.Main;
import me.scattermc.Zombies.event.EventGame;
import me.scattermc.Zombies.event.EventManager;
import me.scattermc.Zombies.event.EventState;
import me.scattermc.Zombies.manager.Manager;
import me.scattermc.Zombies.map.LobbyType;
import me.scattermc.Zombies.utils.PlayerUtils;
import me.scattermc.Zombies.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Profile {
    public Player player;
    private final Map<UUID, PlayerState> playerStates;
    private PlayerScoreboard scoreboard;
    private final Manager manager;
    private final EventManager eventManager;
    private final EventGame eventGame;
    public Profile(Player player){
        this.player = player;
        this.playerStates = new HashMap<>();
        this.manager = (Main.getInstance().manager()==null)? new Manager(Main.getInstance()) : Main.getInstance().manager();
        this.eventManager = manager.getEventManager();
        this.eventGame = eventManager.getEvent();

        if(eventGame == null){
            eventManager.queueEvent();
        }

        scoreboard = new PlayerScoreboard(Main.getInstance(), this, eventManager);
    }
    public boolean isPlaying(){
        if(playerStates.get(player.getUniqueId()) == null) return false;
        return playerStates.get(player.getUniqueId()) != PlayerState.INMAINLOBBY;
    }
    public void setPlayerState(PlayerState playerState){
        playerStates.put(player.getUniqueId(), playerState);

        switch (playerState){
            case INMAINLOBBY -> {
                Bukkit.getLogger().info("#setPlayerState: Set player state to " + playerState);
                PlayerUtils.teleportPlayer(player, LobbyType.MAIN);
            }
            case ISSURVIVOR -> {
                if (eventGame.getEventState() == EventState.STARTED) {
                    PlayerUtils.teleportPlayer(player, LobbyType.GAME);
                }
            }
            case ISZOMBIE -> {
                if (eventGame.getEventState() == EventState.STARTED) {
                    if (WorldUtils.isDay()) {
                        PlayerUtils.teleportPlayer(player, LobbyType.ZOMBIE);
                    } else {
                        PlayerUtils.teleportPlayer(player, LobbyType.GAME);
                    }
                }
            }
        }
    }
    public PlayerState getPlayerState(){
        return playerStates.get(player.getUniqueId());
    }
    public PlayerScoreboard getScoreboard() {
        return scoreboard;
    }
}
