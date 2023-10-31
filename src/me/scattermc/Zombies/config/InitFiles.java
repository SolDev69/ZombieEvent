package me.scattermc.Zombies.config;

import me.scattermc.Zombies.Main;

import java.util.ArrayList;
import java.util.List;

public class InitFiles {
    private final Main main;
    private Config config;
    private Config messages;
    private final List<Config> files;
    public InitFiles(Main main){
        this.main = main;

        this.files = new ArrayList<>();
        registerYMLS();
    }
    private void registerYMLS(){
        this.config = new Config("config.yml", main);
        this.messages = new Config("messages.yml", main);

        files.add(config);
        files.add(messages);
    }
    public Config getConfig(){
        return config;
    }
    public Config getMessages(){
        return messages;
    }
    public List<Config> getFiles(){
        return files;
    }
}
