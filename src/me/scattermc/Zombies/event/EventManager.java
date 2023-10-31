package me.scattermc.Zombies.event;

import me.scattermc.Zombies.Main;
import me.scattermc.Zombies.event.listeners.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class EventManager {
    private final Main main;
    private EventGame eventGame;
    public EventManager(Main main){
        this.main = main;

        PluginManager pm = Bukkit.getPluginManager();
        queueEvent();

        pm.registerEvents(new EventListener(main, eventGame, this), main);
    }
    public void queueEvent(){
        eventGame = new EventGame(main);
    }
    public EventGame getEvent() {
        if(eventGame != null) {
            if (eventGame.getEventState() == EventState.STARTED || eventGame.getEventState() == EventState.WAITING) {
                return eventGame;
            }
        }
        return null;
    }
}
