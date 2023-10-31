package me.scattermc.Zombies.commands;

import com.google.common.collect.ImmutableMap;
import me.scattermc.Zombies.Main;
import me.scattermc.Zombies.config.Message;
import me.scattermc.Zombies.event.EventGame;
import me.scattermc.Zombies.event.EventManager;
import me.scattermc.Zombies.event.EventState;
import me.scattermc.Zombies.player.Profile;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EventCommand implements CommandExecutor, TabExecutor {
    private final Main main;
    private final EventManager eventManager;
    private final EventGame eventGame;
    public EventCommand(Main main, EventManager eventManager){
        this.main = main;
        this.eventManager = eventManager;
        this.eventGame = eventManager.getEvent();
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission(Message.of("general.event-commandpermission").toString())){
            Message.of("general.event-commanddenied").send(sender);
            return true;
        }

        if(!(sender instanceof Player player)){
            switch(args.length){
                case 1 -> {
                    String subCommand = args[0];
                    String[] subCmds = {
                            "start",
                            "stop",
                            "setzombie",
                            "setsurvivor"
                    };

                    if(subCommand.equalsIgnoreCase("start")){
                        if(Bukkit.getOnlinePlayers().size() < 1){
                            Message.of("general.event-no-players-online").send(sender);
                            return true;
                        }

                        if(eventGame.getEventState() == EventState.STARTED){
                            Message.of("general.event-alreadystarted").send(sender);
                            return true;
                        }

                        String countdownTime = eventGame.getCountdownFormatted();

                        eventGame.start();
                        Message.of("general.event-commandstart-success")
                                .placeholders(ImmutableMap.of("<countdownTime>", countdownTime))
                                .send(sender);
                    }else if(subCommand.equalsIgnoreCase("stop")){
                        if(eventGame.getEventState() != EventState.STARTED){
                            Message.of("general.event-notstarted").send(sender);
                            return true;
                        }

                        eventGame.forceStop();
                        Message.of("general.event-forcestopped-success").send(sender);
                    }else{
                        StringBuilder validArg = new StringBuilder();

                        for (String sub : subCmds) {
                            validArg.append("&7- &d").append(sub).append("\n");
                        }

                        Message.of("&cInvalid arguments. Please use:\n" + validArg).send(sender);
                    }
                }
                case 2 -> {
                    String subCommand = args[0];
                    String playerName = args[1];

                    Player target = Bukkit.getPlayer(playerName);

                    if(target == null || !target.isOnline()){
                        Message.of("general.player-notfound").send(sender);
                        return true;
                    }

                    if(subCommand.equalsIgnoreCase("setzombie")){
                        if(eventGame.getEventState() != EventState.STARTED){
                            Message.of("general.event-setfail").send(sender);
                            return true;
                        }

                        Profile targetProfile = main.manager().getProfiles().getDataPlayer(target);

                        if(eventGame.isZombie(targetProfile)){
                            Message.of("&cError: &2" + targetProfile.player.getName() + " &cis already a &2ZOMBIE").send(sender);
                            return true;
                        }
                        eventGame.setZombie(targetProfile);
                    }else if(subCommand.equalsIgnoreCase("setsurvivor")){
                        if(eventGame.getEventState() != EventState.STARTED){
                            Message.of("general.event-setfail").send(sender);
                            return true;
                        }

                        Profile targetProfile = main.manager().getProfiles().getDataPlayer(target);

                        if(eventGame.isSurvivor(targetProfile)){
                            Message.of("&cError: &d" + targetProfile.player.getName() + " &cis already a &dSURVIVOR").send(sender);
                            return true;
                        }
                        eventGame.setSurvivor(targetProfile);
                    }
                }
                default -> Message.of("general.event-command-usage").send(sender);
            }
            return true;
        }

        switch(args.length){
            case 1 -> {
                String subCommand = args[0];
                String[] subCmds = {
                        "start",
                        "stop",
                        "setzombie",
                        "setsurvivor"
                };

                if(subCommand.equalsIgnoreCase("start")){
                    if(Bukkit.getOnlinePlayers().size() < 1){
                        Message.of("general.event-no-players-online").send(player);
                        return true;
                    }

                    if(eventGame.getEventState() == EventState.STARTED){
                        Message.of("general.event-alreadystarted").send(player);
                        return true;
                    }

                    String countdownTime = eventGame.getCountdownFormatted();

                    eventGame.start();
                    Message.of("general.event-commandstart-success")
                            .placeholders(ImmutableMap.of("<countdownTime>", countdownTime))
                            .send(player);
                }else if(subCommand.equalsIgnoreCase("stop")){
                    if(eventGame.getEventState() != EventState.STARTED){
                        Message.of("general.event-notstarted").send(player);
                        return true;
                    }

                    eventGame.forceStop();
                    Message.of("general.event-forcestopped-success").send(player);
                }else{
                    StringBuilder validArg = new StringBuilder();

                    for (String sub : subCmds) {
                        validArg.append("&7- &d").append(sub).append("\n");
                    }

                    Message.of("&cInvalid arguments. Please use:\n" + validArg).send(sender);
                }
            }
            case 2 -> {
                String subCommand = args[0];
                String playerName = args[1];

                Player target = Bukkit.getPlayer(playerName);

                if(target == null || !target.isOnline()){
                    Message.of("general.player-notfound").send(player);
                    return true;
                }

                if(subCommand.equalsIgnoreCase("setzombie")){
                    if(eventGame.getEventState() != EventState.STARTED){
                        Message.of("general.event-setfail").send(player);
                        return true;
                    }

                    Profile targetProfile = main.manager().getProfiles().getDataPlayer(target);

                    if(eventGame.isZombie(targetProfile)){
                        Message.of("&cError: &2" + targetProfile.player.getName() + " &cis already a &2ZOMBIE").send(player);
                        return true;
                    }
                    eventGame.setZombie(targetProfile);
                }else if(subCommand.equalsIgnoreCase("setsurvivor")){
                    if(eventGame.getEventState() != EventState.STARTED){
                        Message.of("general.event-setfail").send(player);
                        return true;
                    }

                    Profile targetProfile = main.manager().getProfiles().getDataPlayer(target);

                    if(eventGame.isSurvivor(targetProfile)){
                        Message.of("&cError: &d" + targetProfile.player.getName() + " &cis already a &dSURVIVOR").send(sender);
                        return true;
                    }
                    eventGame.setSurvivor(targetProfile);
                }
            }
            default -> Message.of("general.event-command-usage").send(player);
        }

        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> subCommands = new ArrayList<>();
        List<String> playerList = new ArrayList<>();

        if(args.length <= 1) {
            subCommands.add("start");
            subCommands.add("stop");
            subCommands.add("setzombie");
            subCommands.add("setsurvivor");
            return subCommands;
        }else if(args.length == 2){
            for(Player players : Bukkit.getOnlinePlayers()){
                playerList.add(players.getName());
            }
        }
        return playerList;
    }
}
