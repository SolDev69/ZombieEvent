package me.scattermc.Zombies.utils;

import me.scattermc.Zombies.Main;
import org.bukkit.Server;

import java.util.Objects;

import static org.bukkit.Bukkit.getServer;

public class WorldUtils {
    public static final long DAY_DURATION = 6000;
    public static final long NIGHT_DURATION = 18000;
    public static boolean isDay() {
        Server server = getServer();
        String mainWorldName = Main.getInstance().files().getConfig().get().getString("general.game-world");

        if (mainWorldName != null) {
            long time = Objects.requireNonNull(server.getWorld(mainWorldName)).getTime();

            if (time > DAY_DURATION && time < NIGHT_DURATION) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    public static void setTime(TimeShade time){
        Server server = getServer();
        String mainWorldName = Main.getInstance().files().getConfig().get().getString("general.game-world");

        if (mainWorldName != null) {
            switch(time){
                case DAY -> {
                    Objects.requireNonNull(server.getWorld(mainWorldName)).setTime(DAY_DURATION);
                }
                case NIGHT -> {
                    Objects.requireNonNull(server.getWorld(mainWorldName)).setTime(NIGHT_DURATION);
                }
            }
        }
    }
    public static long getTime(){
        Server server = getServer();
        String mainWorldName = Main.getInstance().files().getConfig().get().getString("general.game-world");

        if (mainWorldName != null) {
            return Objects.requireNonNull(server.getWorld(mainWorldName)).getTime();
        }
        return 0;
    }
}
