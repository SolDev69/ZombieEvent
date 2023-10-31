package me.scattermc.Zombies.player;

import com.google.common.collect.ImmutableMap;
import fr.mrmicky.fastboard.FastBoard;
import me.scattermc.Zombies.Main;
import me.scattermc.Zombies.config.Message;
import me.scattermc.Zombies.event.EventGame;
import me.scattermc.Zombies.event.EventManager;
import me.scattermc.Zombies.event.EventState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final Main main;
    private final EventManager eventManager;
    private final EventGame eventGame;
    public PlayerListener(Main main, EventManager eventManager){
        this.main = main;
        this.eventManager = eventManager;
        this.eventGame = eventManager.getEvent();
    }
    @EventHandler
    public void onPlayerFirstJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        main.manager().getProfiles().add(player);
        Profile profile = main.manager().getProfiles().getDataPlayer(player);

        profile.setPlayerState(PlayerState.INMAINLOBBY);

        Bukkit.getScheduler().runTaskLater(main, () -> {
            if (eventGame.getEventState() == EventState.STARTED) {
                eventGame.getAllPlayers().add(profile);
                eventGame.sendPlayerToGame(profile);
                eventGame.setZombie(profile);

                String teamPrefix = (eventGame.isSurvivor(profile)) ? Message.of("general.survivor-prefix.tag").toString() : Message.of("general.zombie-prefix.tag").toString();

                Message.of("player.joined-late")
                        .placeholders(ImmutableMap.of("<role>", teamPrefix))
                        .send(profile.player);

                for (Profile profiles : eventGame.getAllPlayers()) {
                    Message.of("general.player-joined-latealert")
                            .placeholders(ImmutableMap.of(
                                    "<role>", teamPrefix,
                                    "<player>", profile.player.getName()))
                            .send(profiles.player);
                }
            }
        }, 50L);
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        Profile profile = main.manager().getProfiles().getDataPlayer(player);

        if (eventGame.getEventState() == EventState.STARTED) {
            eventGame.removePlayer(profile);
        }

        FastBoard board = profile.getScoreboard().getPlayerBoards().remove(profile.player.getUniqueId());
        if(board != null){
            board.delete();
        }
        main.manager().getProfiles().remove(player);
    }
}
