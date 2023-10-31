package me.scattermc.Zombies.config;

import me.scattermc.Zombies.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public class Message {
    private String configSec;
    private String parsedString;
    private List<String> parsedList;
    private FileConfiguration langConfig;
    private boolean isString;

    public Message(String configSec){
        this.configSec = configSec;
        langConfig = Main.getInstance().files().getMessages().get();

        if(langConfig.isString(configSec)) {
            isString = true;
            parsedString = toColor(langConfig.getString(configSec));
        }
        else if(langConfig.isList(configSec)){
            isString = false;
            parsedList = langConfig.getStringList(configSec).stream().map(this::toColor).collect(Collectors.toList());
        } else {
            isString = true;
            parsedString = toColor(configSec);
        }
    }

    public Message placeholders(Map<String, String> placeholders){
        if(isString){
            for(String placeholder : placeholders.keySet())
                parsedString = parsedString.replace(placeholder, placeholders.get(placeholder));
        }else {

            parsedList = parsedList.stream().map(string -> {
                for(String placeholder : placeholders.keySet())
                    string = string.replace(placeholder, placeholders.get(placeholder));

                return string;
            }).collect(Collectors.toList());
        }

        return this;
    }

    public void send(CommandSender sender){
        if(isString){
            sender.sendMessage(parsedString);
        }else {
            for(String str : parsedList)
                sender.sendMessage(str);
        }
    }
    public void broadcast(){
        if(isString){
            Bukkit.getServer().broadcastMessage(parsedString);
        }else {
            for(String str : parsedList)
                Bukkit.getServer().broadcastMessage(str);
        }
    }
    public List<String> toList(){
        if(isString)
            return Collections.singletonList(parsedString);
        return parsedList;
    }
    public String toString(){
        if(isString)
            return parsedString;
        else return parsedList.toString();
    }
    public static void sendActionBar(Player player, String text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Message.color(text)));
    }
    @Deprecated
    public static void sendTitle(Player player, String title, String subtitle){
        player.sendTitle(color(title), color(subtitle));
    }
    public static Message of(String message){
        return new Message(message);
    }
    public String toColor(String base){
        return ChatColor.translateAlternateColorCodes('&', base);
    }
    public static String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
    public static String color(String base){
        return ChatColor.translateAlternateColorCodes('&', base);
    }
}

