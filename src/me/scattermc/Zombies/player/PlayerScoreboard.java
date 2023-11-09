package me.scattermc.Zombies.player;

import com.google.common.collect.ImmutableMap;
import fr.mrmicky.fastboard.FastBoard;
import me.scattermc.Zombies.Main;
import me.scattermc.Zombies.config.Message;
import me.scattermc.Zombies.event.EventGame;
import me.scattermc.Zombies.event.EventManager;
import me.scattermc.Zombies.event.EventState;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerScoreboard {
    private final Main main;
    private final Profile profile;
    private final EventManager eventManager;
    private final EventGame eventGame;
    private FastBoard board;
    private final Map<UUID, FastBoard> playerBoard;
    public PlayerScoreboard(Main main, Profile profile, EventManager eventManager) {
        this.main = main;
        this.eventManager = eventManager;
        this.eventGame = eventManager.getEvent();
        this.profile = profile;
        this.playerBoard = new HashMap<>();

        startBoardTask();
    }
    private void startBoardTask(){
        Bukkit.getScheduler().runTaskTimer(main, this::updateBoard, 20L, 20L);
    }
    private void updateBoard(){
        if(board == null || !hasBoard(profile.player.getUniqueId())){
            board = new FastBoard(profile.player);
            playerBoard.put(profile.player.getUniqueId(), board);
        }
        if(board.isDeleted()) return;

        switch (profile.getPlayerState()) {
            case INMAINLOBBY -> {
                board.updateTitle(Message.of("scoreboard.main.title").toString());

                if(eventGame.getEventState() == EventState.WAITING || eventGame.getEventState() == EventState.STOPPED) {
                    String formattedTime = eventGame.getCountdownFormatted();
                    int countdownTime = eventGame.getCountdownTime();
                    int configTime = main.files().getConfig().get().getInt("general.game-countdown");

                    List<String> lines = Message.of("scoreboard.main.lines")
                            .placeholders(ImmutableMap.of(
                                    "<playerCount>", String.valueOf(Bukkit.getOnlinePlayers().size()),
                                    "<countdownTime>", (countdownTime == configTime) ? Message.color("&dWaiting") : Message.color(formattedTime))).toList();

                    for (int i = 0; i < lines.size(); ++i) {
                        board.removeLine(i);

                        String line = lines.get(i);
                        board.updateLine(i, line);
                    }
                }
            }
            case ISSURVIVOR -> {
                board.updateTitle(
                        Message.of("scoreboard.survivor.title")
                                .placeholders(ImmutableMap.of("<survivorPrefix>", Message.of("general.survivor-prefix.tag").toString()))
                                .toString());

                if(eventGame.getEventState() == EventState.STARTED) {
                    int timeLeft = eventGame.getTimeRemaining();
                    int survivorCount = eventGame.getSurvivorCount();
                    int zombieCount = eventGame.getZombieCount();

                    List<String> lines = Message.of("scoreboard.survivor.lines")
                            .placeholders(ImmutableMap.of(
                                    "<playerCount>", String.valueOf(Bukkit.getOnlinePlayers().size()),
                                    "<survivorPrefix>", Message.of("general.survivor-prefix.tag").toString(),
                                    "<timeLeft>", String.valueOf(timeLeft),
                                    "<survivorCount>", String.valueOf(survivorCount),
                                    "<zombieCount>", String.valueOf(zombieCount))).toList();

                    for (int i = 0; i < lines.size(); ++i) {
                        String line = lines.get(i);
                        board.updateLine(i, line);
                    }
                }
            }
            case ISZOMBIE -> {
                board.updateTitle(
                        Message.of("scoreboard.zombie.title")
                                .placeholders(ImmutableMap.of("<zombiePrefix>", Message.of("general.zombie-prefix.tag").toString()))
                                .toString());

                if(eventGame.getEventState() == EventState.STARTED) {
                    int timeLeft = eventGame.getTimeRemaining();
                    int survivorCount = eventGame.getSurvivorCount();
                    int zombieCount = eventGame.getZombieCount();

                    List<String> lines = Message.of("scoreboard.zombie.lines")
                            .placeholders(ImmutableMap.of(
                                    "<playerCount>", String.valueOf(Bukkit.getOnlinePlayers().size()),
                                    "<zombiePrefix>", Message.of("general.zombie-prefix.tag").toString(),
                                    "<timeLeft>", String.valueOf(timeLeft),
                                    "<survivorCount>", String.valueOf(survivorCount),
                                    "<zombieCount>", String.valueOf(zombieCount))).toList();

                    for (int i = 0; i < lines.size(); ++i) {
                        String line = lines.get(i);
                        board.updateLine(i, line);
                    }
                }
            }
        }
    }
    public boolean hasBoard(UUID uuid){
        return playerBoard.containsKey(uuid);
    }
    public Map<UUID, FastBoard> getPlayerBoards() {
        return playerBoard;
    }
}
