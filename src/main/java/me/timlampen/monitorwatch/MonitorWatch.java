package me.timlampen.monitorwatch;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import net.milkbowl.vault.chat.Chat;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.apache.http.client.HttpClient;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONArray;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Primary on 8/13/2016.
 */
public class MonitorWatch extends JavaPlugin{
    Chat chata;
    MonitorWatch p;
   // HttpClient httpClient = HttpClientBuilder.create().build();
    String key = getConfig().getString("key");
    Gson gson = new Gson();
    HashMap<String, Object> longRefresh = new HashMap<String, Object>();
    HashMap<String, Object> shortRefresh = new HashMap<String, Object>();
    public ArrayList<String> cmds = new ArrayList<>();
    public ArrayList<String> chat = new ArrayList<>();
    public ArrayList<String> signs = new ArrayList<>();
    public ArrayList<String> logins = new ArrayList<>();
    public ArrayList<String> logouts = new ArrayList<>();
    @Override
    public void onEnable(){
        p = this;
        File file = new File(getDataFolder() + "/config.yml/");
        if(!file.exists()){
            saveConfig();
            getConfig().set("key", "INSERTKEYHERE");
            saveConfig();
        }
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 0, 1);
        new BukkitRunnable(){
            public void run() {
                ArrayList<String> wNames = new ArrayList<String>();
                ArrayList<String> pNames = new ArrayList<String>();
                OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
                for(World w : Bukkit.getWorlds()){
                    wNames.add(w.getName() + "/|%|/" + w.getEntities().size());
                }
                for(Player p : Bukkit.getOnlinePlayers()) {
                    if (chata == null) {
                        pNames.add(p.getName() + "/|%|/" + p.getUniqueId() + "/|%|/" + p.getAddress().getAddress().toString().replace("/", "") + "/|%|/" + p.getWorld().getName() + "/|%|/null/|%|/null/|%|/" + p.getLocation().getBlockX() + "/|%|/" + p.getLocation().getBlockY() + "/|%|/" + p.getLocation().getBlockZ());
                    } else {
                        pNames.add(p.getName() + "/|%|/" + p.getUniqueId() + "/|%|/" + p.getAddress().getAddress().toString().replace("/", "") + "/|%|/" + p.getWorld().getName() + "/|%|/" + chata.getPlayerPrefix(p) + "/|%|/" + chata.getPlayerSuffix(p) + "/|%|/" + p.getLocation().getBlockX() + "/|%|/" + p.getLocation().getBlockY() + "/|%|/" + p.getLocation().getBlockZ());
                    }
                }
                Date date = new Date(System.currentTimeMillis());
                DecimalFormat df = new DecimalFormat("#.##");
                longRefresh.put("time", date.toLocaleString());
                longRefresh.put("tps", df.format(Lag.getTPS(5*20)));
                longRefresh.put("playerlist", new JSONArray(pNames));
                longRefresh.put("onlineplayers", Bukkit.getOnlinePlayers().size());
                longRefresh.put("freeram", Runtime.getRuntime().freeMemory()/1024/ 1024);
                longRefresh.put("allocatedram", Runtime.getRuntime().totalMemory()/1024/1024);
                longRefresh.put("maxram", Runtime.getRuntime().maxMemory()/1024/1024);
                try{
                    longRefresh.put("cpuusage", getProcessCpuLoad());
                }catch(Exception e){
                    e.printStackTrace();
                }
                longRefresh.put("worlds", new JSONArray(wNames));
                wNames.clear();
                pNames.clear();
                try {
                    longRefresh.put("maxplayers", getMaxPlayers());
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            PostResult result = null;
                            try {
                                result = gson.fromJson(postToURL("http://www.monitorwatch.net/api/Receiver", gson.toJson(longRefresh)), PostResult.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                                longRefresh.clear();
                            }
                            if(result==null){
                                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Unable to recieve information from the webserver, either the server is down or the URL changed");
                            }
                            else if(result.success){
                                for(String cmd : result.commands){
                                    cmd = cmd.contains("/") ? cmd.replace("/", "") : cmd;
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                                    //           Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Recieved reply from web: " + cmd);
                                }
                            }
                            else{
                                //         Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Data sent to the webserver, however success=false");
                            }
                            longRefresh.clear();
                        }
                    }.runTaskAsynchronously(p);
                } catch (Exception e) {
                    //Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error connection to the webserver: " + ChatColor.DARK_AQUA + e.getLocalizedMessage());
                    e.printStackTrace();
                    longRefresh.clear();
                }
            }
        }.runTaskTimer(this, 5*20, 5*20);

        new BukkitRunnable(){
            @Override
            public void run() {
                shortRefresh.put("commands", new JSONArray(cmds));
                shortRefresh.put("chat", new JSONArray(chat));
                shortRefresh.put("signs", new JSONArray(signs));
                shortRefresh.put("logins", new JSONArray(logins));
                shortRefresh.put("logouts", new JSONArray(logouts));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            PostResult result = gson.fromJson(postToURL("http://www.monitorwatch.net/api/Receiver", gson.toJson(shortRefresh)), PostResult.class);
                            if(result==null){
                                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Unable to recieve information from the webserver, either the server is down or the URL changed");
                            }
                            if (result.success) {
                                for (String cmd : result.commands) {
                                    cmd = cmd.contains("/") ? cmd.replace("/", "") : cmd;
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                                    //            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Recieved reply from web: " + cmd);
                                }
                            }
                            else{
                                //        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Data sent to the webserver, however success=false");
                            }
                            cmds.clear();
                            chat.clear();
                            signs.clear();
                            logins.clear();
                            logouts.clear();
                            shortRefresh.clear();
                        } catch(Exception e){
                            e.printStackTrace();
                            cmds.clear();
                            chat.clear();
                            signs.clear();
                            logins.clear();
                            logouts.clear();
                            shortRefresh.clear();
                        }
                    }
                }.runTaskAsynchronously(p);
            }
        }.runTaskTimer(this, 20*2, 20*2);
        Bukkit.getPluginManager().registerEvents(new FlaggedListener(this), this);
        setupChat();
    }

    @Override
    public void onDisable(){
        saveConfig();
    }

    public String postToURL(String url, String data) throws Exception {
        HttpResponse<JsonNode> jsonResponse = Unirest.post(url)
                .header("accept", "application/json")
                .header("key", key)
                .field("json", data)
                .asJson();
        java.util.Scanner s = new java.util.Scanner(jsonResponse.getRawBody(), "UTF-8").useDelimiter("\\A");
        String str = s.hasNext() ? s.next() : "";
        return str;
    }
    public static int getMaxPlayers()
            throws ReflectiveOperationException {
        String bukkitversion = Bukkit.getServer().getClass().getPackage()
                .getName().substring(23);
        Object playerlist = Class.forName("org.bukkit.craftbukkit." + bukkitversion    + ".CraftServer")
                .getDeclaredMethod("getHandle", null).invoke(Bukkit.getServer(), null);
        Field maxplayers = getField(playerlist.getClass(), "maxPlayers");
        maxplayers.setAccessible(true);
        return maxplayers.getInt(playerlist);
    }
    private static Field getField(Class clazz, String fieldName)
            throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            } else {
                return getField(superClass, fieldName);
            }
        }
    }
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chata = rsp.getProvider();
        return chat != null;
    }
    public static double getProcessCpuLoad() throws Exception {

        MBeanServer mbs    = ManagementFactory.getPlatformMBeanServer();
        ObjectName name    = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });

        if (list.isEmpty())     return Double.NaN;

        Attribute att = (Attribute)list.get(0);
        Double value  = (Double)att.getValue();

        // usually takes a couple of seconds before we get real values
        if (value == -1.0)      return Double.NaN;
        // returns a percentage value with 1 decimal point precision
        return ((int)(value * 1000) / 10.0);
    }
}