package me.timlampen.monitorwatch;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Date;

/**
 * Created by Primary on 8/14/2016.
 */
public class FlaggedListener implements Listener{
    MonitorWatch ss;
    public FlaggedListener(MonitorWatch ss){
        this.ss = ss;
    }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        String msg = event.getMessage();
        Date date = new Date(System.currentTimeMillis());
        ss.chat.add(player.getName() + "/|%|/" + player.getUniqueId() + "/|%|/" + player.getAddress().toString().replace("/", "") + "/|%|/" + date.toLocaleString() + "/|%|/" + player.getWorld().getName() + "/|%|/" +  msg);
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        String cmd = event.getMessage();
        Date date = new Date(System.currentTimeMillis());
        ss.cmds.add(player.getName() + "/|%|/"  + player.getUniqueId() + "/|%|/" + player.getAddress().toString().replace("/", "") + "/|%|/" + date.toLocaleString() + "/|%|/" + player.getWorld().getName() + "/|%|/" + cmd);
    }

    @EventHandler
    public void onSign(SignChangeEvent event){
        Player player = event.getPlayer();
        String[] lines = event.getLines();
        Date date = new Date(System.currentTimeMillis());
        String s = "";
        for(int i = 0; i<4; i++){
            String line = lines[i];
            if(line==null){
                line = "";
            }
            s += line + "/|%|/";
        }
        ss.signs.add(player.getName() + "/|%|/"  + player.getUniqueId() + "/|%|/" + player.getAddress().toString().replace("/", "") + "/|%|/" + player.getWorld().getName() + "/|%|/" + date.toLocaleString() + "/|%|/" + s );
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        Date date = new Date(System.currentTimeMillis());
        ss.logins.add(player.getName() + "/|%|/"  + player.getUniqueId() + "/|%|/" + player.getAddress().toString().replace("/", "") + "/|%|/" + player.getWorld().getName() + "/|%|/" + player.getLocation().getBlockX() + "/|%|/" + player.getLocation().getBlockY() + "/|%|/" + player.getLocation().getBlockZ() + "/|%|/" + date.toLocaleString());
    }

    @EventHandler
    public void onJoin(PlayerQuitEvent event){
        Player player = event.getPlayer();
        Date date = new Date(System.currentTimeMillis());
        ss.logouts.add(player.getName() + "/|%|/"  + player.getUniqueId() + "/|%|/" + player.getAddress().toString().replace("/", "") + "/|%|/" + player.getWorld().getName() + "/|%|/" + player.getLocation().getBlockX() + "/|%|/" + player.getLocation().getBlockY() + "/|%|/" + player.getLocation().getBlockZ() + "/|%|/" + date.toLocaleString());
    }
}
