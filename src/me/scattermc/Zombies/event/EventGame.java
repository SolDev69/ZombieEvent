package me.scattermc.Zombies.event;

import com.google.common.collect.ImmutableMap;
import me.scattermc.Zombies.Main;
import me.scattermc.Zombies.config.Message;
import me.scattermc.Zombies.event.items.TrackCompass;
import me.scattermc.Zombies.map.GameMap;
import me.scattermc.Zombies.map.LobbyType;
import me.scattermc.Zombies.player.PlayerState;
import me.scattermc.Zombies.player.Profile;
import me.scattermc.Zombies.utils.PlayerUtils;
import me.scattermc.Zombies.utils.TimeShade;
import me.scattermc.Zombies.utils.WorldUtils;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

import static me.scattermc.Zombies.event.listeners.EventListener.survivorBlockMap;
import static me.scattermc.Zombies.event.listeners.EventListener.zombieBlockMap;

public class EventGame extends BukkitRunnable {
    private final Main main;
    private EventState eventState;
    private GameMap map;
    private Collection<Profile> allPlayers;
    private Scoreboard eventBoard;
    private Team survivors;
    private Team zombies;
    private BukkitTask task;
    private BukkitTask countdownTask;
    private int countdown;
    public static Map<EventGame, Integer> countdownTime = new HashMap<>();
    private int interval = 24000; //20 minute Days
    public static final int MAX_DAYS = Main.getInstance().files().getConfig().get().getInt("general.max-days"); //7 days
    private int timeRemaining = MAX_DAYS;
    public static final String gameMapName = Main.getInstance().files().getConfig().get().getString("general.game-world");
    private int survivorCount;
    private int zombieCount;
    public EventGame(Main main){
        this.main = main;
        this.allPlayers = new ArrayList<>();
        this.eventBoard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        this.survivors = registerTeam("survivors");
        this.zombies = registerTeam("zombies");
        countdown = main.files().getConfig().get().getInt("general.game-countdown");

        survivors.setDisplayName(Message.of("general.survivor-prefix.tag").toString());
        survivors.setPrefix(Message.of("general.survivor-prefix.tag").toString());
        survivors.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        survivors.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        survivors.setAllowFriendlyFire(false);
        survivors.setCanSeeFriendlyInvisibles(true);
        zombies.setColor(ChatColor.valueOf(Message.of("general.survivor-prefix.color").toString()));

        zombies.setDisplayName(Message.of("general.zombie-prefix.tag").toString());
        zombies.setPrefix(Message.of("general.zombie-prefix.tag").toString());
        zombies.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        zombies.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        zombies.setAllowFriendlyFire(false);
        zombies.setCanSeeFriendlyInvisibles(true);
        zombies.setColor(ChatColor.valueOf(Message.of("general.zombie-prefix.color").toString()));

        setEventState(EventState.WAITING);
        map = new GameMap(main, gameMapName);
    }
    @Override
    public void run() {
        if(task != null && timeRemaining > 0){
            WorldUtils.setTime(TimeShade.DAY);

            Bukkit.getScheduler().runTaskLater(main, ()->{
                timeRemaining-=1;

                for(Profile profiles : allPlayers){
                    Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
                        WorldUtils.setTime(TimeShade.DAY);

                        if(isZombie(profiles)){
                            buffZombie(profiles);
                            PlayerUtils.teleportPlayer(profiles.player, LobbyType.ZOMBIE);
                        }

                        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                            WorldUtils.setTime(TimeShade.NIGHT); // 5 minutes has passed let's set it too Night

                            if (isZombie(profiles)) {
                                PlayerUtils.teleportPlayer(profiles.player, LobbyType.GAME);
                            }
                        }, WorldUtils.DAY_DURATION);
                    }, 0, WorldUtils.DAY_DURATION + WorldUtils.NIGHT_DURATION);

                    Message.of("general.event-timeupdate")
                            .placeholders(ImmutableMap.of(
                                    "<currentTime>", ((WorldUtils.isDay()) ? "&6Day Time" : "&9Night Time"),
                                    "<timeLeft>", String.valueOf(timeRemaining)))
                            .send(profiles.player);

                    String title = Message.of("general.event-title-screen-timeupdate")
                            .placeholders(ImmutableMap.of("<currentTime>", ((WorldUtils.isDay()) ? "&6Day Time" : "&9Night Time"))).toString();
                    String subtitle = Message.of("general.event-subtitle-timeupdate")
                            .placeholders(ImmutableMap.of("<currentTime>", ((WorldUtils.isDay()) ? "&6Day Time" : "&9Night Time"))).toString();
                    Message.sendTitle(profiles.player, title, subtitle);

                    String updateSound = main.files().getConfig().get().getString("general.event-timeupdate-soundeffect");
                    profiles.player.playSound(profiles.player.getLocation(), Sound.valueOf(updateSound), 2f, 2f);
                }
            }, interval);
        }

        if(timeRemaining == 0){
            forceStop();
        }
    }

    public void start() {
        if (countdownTask == null) {
            countdownTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (countdown > 0) {
                        countdown -= 1;
                        countdownTime.put(EventGame.this, countdown);

                        for (Profile profiles : main.manager().getProfiles().getDataPlayers()) {
                            if (profiles.getPlayerState() == PlayerState.INMAINLOBBY) {
                                if (countdown <= 3) {
                                    if (countdown == 0) return;
                                    Message.of("general.broadcast-event-countdown")
                                            .placeholders(ImmutableMap.of("<countdownTime>", String.valueOf(countdown)))
                                            .send(profiles.player);
                                }
                            }
                        }
                    } else {
                        if (countdownTask != null && !countdownTask.isCancelled()) {
                            countdownTask.cancel();
                        }

                        setEventState(EventState.STARTED);

                        for (Profile profiles : main.manager().getProfiles().getDataPlayers()) {
                            if (profiles.getPlayerState() == PlayerState.INMAINLOBBY) {
                                allPlayers.add(profiles);

                                List<Profile> allPlayersList = new ArrayList<>(allPlayers);
                                Collections.shuffle(allPlayersList);

                                List<Profile> survivorsList = new ArrayList<>(allPlayersList.subList(1, allPlayersList.size()));
                                survivorsList.forEach(EventGame.this::setSurvivor);

                                List<Profile> zombiesList = new ArrayList<>();
                                zombiesList.add(allPlayersList.get(0));
                                zombiesList.forEach(EventGame.this::setZombie);

                                Message.of("general.event-game-begun-broadcast")
                                        .placeholders(ImmutableMap.of(
                                                "<zombieCount>", String.valueOf(zombieCount),
                                                "<survivorCount>", String.valueOf(survivorCount),
                                                "<timeLeft>", String.valueOf(timeRemaining)))
                                        .send(profiles.player);

                                sendPlayerToGame(profiles);
                            }
                        }
                    }
                }
            }.runTaskTimer(main, 20L, 20L);
        }

        if (task == null && countdownTask.isCancelled()) {
            super.runTaskTimer(main, 20L, 20L);
        }
    }
    public void forceStop(){
        setEventState(EventState.STOPPED);

        if(task != null && !isCancelled()){
            super.cancel();
            task = null;
        }

        for(Profile profiles : allPlayers){
            profiles.player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard());
            profiles.setPlayerState(PlayerState.INMAINLOBBY);

            Message.of("general.game-over").send(profiles.player);
        }

        reset();
    }
    private Team registerTeam(String name) {
        Team team = eventBoard.getTeam(name);
        if (team == null) {
            team = eventBoard.registerNewTeam(name);
        }
        return team;
    }
    public void setEventState(EventState eventState) {
        this.eventState = eventState;
    }
    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
    public void setSurvivor(Profile profile) {
        String name = profile.player.getName();

        if(survivors.hasEntry(name)) return;
        if(zombies.hasEntry(name)){
            zombies.removeEntry(name);
            survivors.addEntry(name);
        }else{
            survivors.addEntry(name);
        }

        if(zombieCount >= 1){
            zombieCount-=1;
        }

        survivorCount+=1;
        Message.of("player.set-to-survivor").send(profile.player);
        profile.setPlayerState(PlayerState.ISSURVIVOR);
    }
    public void setZombie(Profile profile) {
        String name = profile.player.getName();
        World world = profile.player.getWorld();
//        Zombie npc = (Zombie) world.spawnEntity(profile.player.getLocation(), EntityType.ZOMBIE);

        if(zombies.hasEntry(name)) return;
        if(survivors.hasEntry(name)){
            survivors.removeEntry(name);
            zombies.addEntry(name);
        }else{
            zombies.addEntry(name);
        }

        if(survivorCount >= 1){
            survivorCount-=1;
        }

        zombieCount+=1;
        Message.of("player.set-to-zombie").send(profile.player);
        profile.setPlayerState(PlayerState.ISZOMBIE);

        for(Profile profiles : allPlayers){
            Message.of("general.event-broadcast-zombify")
                    .placeholders(ImmutableMap.of("<player>", profile.player.getName()))
                    .send(profiles.player);
        }
        TrackCompass trackCompass = new TrackCompass(main, main.manager().getEventManager());
        main.getServer().getPluginManager().registerEvents(trackCompass, main);

        profile.player.getInventory().addItem(trackCompass.getCompass());
    }
    public void removePlayer(Profile profile){
        String name = profile.player.getName();
        String teamPrefix = (isSurvivor(profile)) ? Message.of("general.survivor-prefix.tag").toString() : Message.of("general.zombie-prefix.tag").toString();
        allPlayers.remove(profile);

        for(Profile profiles : allPlayers) {
            Message.of(teamPrefix + "general.player-removed")
                    .placeholders(ImmutableMap.of("<player>", name))
                    .send(profiles.player);
        }

        if(isSurvivor(profile)){
            survivors.removeEntry(name);
            survivorCount-=1;
        }else if(isZombie(profile)){
            zombies.removeEntry(name);
            zombieCount-=1;
        }

        profile.player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard());
        profile.setPlayerState(PlayerState.INMAINLOBBY);
    }
    public void sendPlayerToGame(Profile profile){
        if(profile.isPlaying() && allPlayers.contains(profile)) {
            PlayerState state = (isSurvivor(profile)) ? PlayerState.ISSURVIVOR : PlayerState.ISZOMBIE;
            String teamPrefix = (isSurvivor(profile)) ? Message.of("general.survivor-prefix.tag").toString() : Message.of("general.zombie-prefix.tag").toString();
            profile.setPlayerState(state);

            Message.of("player.added-to-game")
                    .placeholders(ImmutableMap.of("<role>", teamPrefix))
                    .send(profile.player);
        }
    }
    public void buffZombie(Profile profile){
        if(isSurvivor(profile)) return; //buffing for zombies only

        int day = timeRemaining;
        Player player = profile.player;
        PlayerInventory inv = player.getInventory();

        switch(day){
            case 1->{
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
            }
            case 2->{
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                inv.addItem(new ItemStack(Material.WOODEN_SWORD));
            }
            case 3->{
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
            }
            case 4->{
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                inv.addItem(new ItemStack(Material.GOLDEN_SWORD));
            }
            case 5->{
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));
            }
            case 6->{
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
                inv.addItem(new ItemStack(Material.STONE_SWORD));
            }
            case 7->{
                for(String name : getSurvivors().getEntries()){
                    Player survivorPlayer = Bukkit.getPlayer(name);
                    Profile survivor = main.manager().getProfiles().getDataPlayer(survivorPlayer);

                    survivor.player.setGlowing(true);
                }
            }
        }
    }
    public void reset(){
        for(String name : survivors.getEntries()){
            Player survivorPlayer = Bukkit.getPlayer(name);
            Profile survivor = main.manager().getProfiles().getDataPlayer(survivorPlayer);

            survivor.player.setGlowing(false);
            survivors.removeEntry(name);
        }

        for(String name : zombies.getEntries()){
            zombies.removeEntry(name);
        }

        for(Profile profiles : allPlayers){
            profiles.player.getInventory().clear();
        }

        map.unload();
        allPlayers.clear();
        zombieBlockMap.clear();
        survivorBlockMap.clear();
        countdown = main.files().getConfig().get().getInt("general.game-countdown");
        timeRemaining = MAX_DAYS;
        eventBoard = null;
        zombieCount = 0;
        survivorCount = 0;
    }
    public boolean isSurvivor(Profile profile){
        return survivors.hasEntry(profile.player.getName()) || profile.getPlayerState() == PlayerState.ISSURVIVOR;
    }
    public boolean isZombie(Profile profile){
        return zombies.hasEntry(profile.player.getName()) || profile.getPlayerState() == PlayerState.ISZOMBIE;
    }
    public int getTimeRemaining(){
        return timeRemaining;
    }
    public String getCountdownFormatted(){
        int countdown = countdownTime.getOrDefault(this, this.countdown);
        String formattedTime;
        if (countdown >= 60) {
            int minutes = countdown / 60;
            int seconds = countdown % 60;
            formattedTime = String.format("%d:%02d min", minutes, seconds);
        } else {
            formattedTime = String.format("%ds", countdown);
        }
        return formattedTime;
    }
    public int getCountdownTime(){
        return countdownTime.getOrDefault(this, this.countdown);
    }
    public int getZombieCount() {
        return zombieCount;
    }
    public int getSurvivorCount() {
        return survivorCount;
    }
    public EventState getEventState() {
        return eventState;
    }
    public Collection<Profile> getAllPlayers() {
        return allPlayers;
    }
    public Team getSurvivors() {
        return survivors;
    }
    public Team getZombies() {
        return zombies;
    }
}
