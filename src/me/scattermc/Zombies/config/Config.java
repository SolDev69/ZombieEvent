package me.scattermc.Zombies.config;

import me.scattermc.Zombies.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
public class Config {
    private FileConfiguration config = null;
    private File configfile = null;
    private String path = "";
    private final Main main;
    public Config(String path, Main main){
        this.path = path;
        this.main = main;

        saveDefault();
    }
    public void reload() {
        if (config == null)
            configfile = new File(main.getDataFolder() + File.separator + path);
        config = YamlConfiguration.loadConfiguration(configfile);

        Reader defConfigStream;
        try {
            defConfigStream = new InputStreamReader(new FileInputStream(configfile), StandardCharsets.UTF_8);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            config.setDefaults(defConfig);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public FileConfiguration get() {
        if (config == null)
            reload();
        return config;
    }
    public void save() {
        if (config == null || configfile == null)
            return;
        try {
            config.save(configfile);
        } catch (IOException ignored) {
            Bukkit.getLogger().info(configfile + " trouble saving! Please check the .yml file.");
        }
    }
    public void saveDefault() {
        configfile = new File(main.getDataFolder() + File.separator + path);

        if (!configfile.exists()) {
            configfile.getParentFile().mkdirs();
            main.saveResource(path.toString(), false);
        }
    }
}
