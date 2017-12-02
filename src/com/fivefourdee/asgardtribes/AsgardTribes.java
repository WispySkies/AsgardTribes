package com.fivefourdee.asgardtribes;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class AsgardTribes extends JavaPlugin implements Listener {
    
    Logger logger = this.getLogger();
    private Economy economy;
    Server server = getServer();
    String prefix = this.getConfig().getString("settings.prefix");
    FileConfiguration config = this.getConfig();
    PluginDescriptionFile pdfile = this.getDescription();
    Map<Player,String> invited = new LinkedHashMap<Player, String>();
    
    private boolean setupEconomy() {
        logger = getLogger();
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
            logger.info("Vault found!");
        }
        return economy != null;
    }
    
    private void createConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean inTribe(CommandSender sender) {
        Player senderP = (Player) sender;
        String tribe = getConfig()
                .getString("users." + senderP.getUniqueId().toString().replaceAll("-", "") + ".tribe");
        if (tribe == null) {
            return false;
        } else {
            return true;
        }
    }
    
    private String getTribe(Object in) {
        String tribe;
        if (in instanceof Player) {
            Player user = (Player) in;
            tribe = getConfig().getString("users." + user.getUniqueId().toString().replaceAll("-", "") + ".tribe");
        } else if (in instanceof String) {
            String uuid = ((String) in).replaceAll("-", "");
            tribe = getConfig().getString("users." + uuid + ".tribe");
        } else {
            tribe = null;
            try {
                throw new Exception("Not user nor string");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return tribe;
    }
    
    private String arrayToString(String[] array) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String particle : array) {
            if (!first) {
                sb.append(' ');
            }
            sb.append(particle);
            first = false;
        }
        return sb.toString();
    }
    
    private void sendMsg(Player player, String msg) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }
    
    private String typesDesc(String type) {
        String desc;
        if (type.equals("Aesir")) {
            desc = "A technique passed down from generation to generation. "
                    + "Drawing from the power of his soul, the caster unleashes "
                    + "a devastating strike to all who stand in his way. "
                    + "However, the soul is a fragile object, and by drawing "
                    + "power from itself, the backlash to the caster\'s physical " + "wellbeing shall be severe.";
        } else if (type.equals("Vanir")) {
            desc = "A technique passed down through ages. "
                    + "Drawing from the network of souls of present tribe members, "
                    + "the caster creates a massive Fireball capable of annihilating "
                    + "even the strongest of Gods and deities. However, since the "
                    + "Fireball is created by a network of souls, its strength shall "
                    + "depend on the complexity of the network.";
        } else {
            desc = "";
        }
        return desc;
    }
    
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        createConfig();
        setupEconomy();
    }
    
    /*
     * @Override public void onDisable(){ saveConfig(); }
     */
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player senderP = (Player) sender;
        String senderID = senderP.getUniqueId().toString().replace("-", "");
        
        if (cmd.getName().equalsIgnoreCase("tribe") || cmd.getName().equalsIgnoreCase("tribes")) {
            if (args.length < 1) {
                sendMsg(senderP, prefix + "&c&lAsgard&6&lTribes");
                sendMsg(senderP, " &cRunning on &4" + server.getServerName() + ":" + server.getName());
                sendMsg(senderP, " &cVersion&4 " + pdfile.getVersion());
                sendMsg(senderP, " &cCoded by&4 54D&c with help from &4Qubd&c, &4XMen&c, &4Arpolix, &4CRAVENSCRAFT");
            } else {
                if(args[0].equalsIgnoreCase("accept")) {
                    if(args.length<2) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe accept <tribe>");
                    }else if(!invited.containsKey(senderP)) {
                        sendMsg(senderP, prefix+"&4You have no pending invitations!");
                    }else if(!args[1].equals(invited.get(senderP))) {
                        sendMsg(senderP, prefix+"&4You have no pending invitations from that tribe!");
                    }else if(inTribe(senderP)) {
                        sendMsg(senderP, prefix+"&4You are already in a tribe!");
                    }else {
                        String userID = senderP.getUniqueId().toString().replaceAll("-", "");
                        getConfig().set("users." + userID + ".name",senderP.getName());
                        getConfig().set("users." + userID + ".tribe",args[1]);
                        getConfig().set("users." + userID + ".rank","Member");
                        Set<String> uuids = getConfig().getConfigurationSection("users").getKeys(false);
                        for (String s : uuids) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(s);
                            sb.insert(20, "-");
                            sb.insert(16, "-");
                            sb.insert(12, "-");
                            sb.insert(8, "-");
                            UUID u = UUID.fromString(sb.toString());
                            if (server.getPlayer(u) != null&&getTribe(s).equals(args[1])) {
                                sendMsg(server.getPlayer(u),
                                        prefix + "&c" + senderP.getName() + "&7 has joined the tribe.");
                            }
                        }
                        saveConfig();
                        invited.remove(senderP);
                    }
                } else if (args[0].equalsIgnoreCase("bal") || args[0].equalsIgnoreCase("balance")) {
                    if (!inTribe(sender)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    } else {
                        sendMsg(senderP, prefix + "&7Tribe currently has &a$"
                                + getConfig().getDouble("tribes." + getTribe(senderP) + ".balance") + "&7.");
                    }
                } else if (args[0].equalsIgnoreCase("chat") || args[0].equalsIgnoreCase("c")) {
                    if (!inTribe(senderP)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    } else if (args.length<2) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe chat <message>");
                    } else {
                        args[0] = "";
                        String msg = arrayToString(args);
                        List<Player> players = new ArrayList<Player>();
                        server.getOnlinePlayers().stream().forEach(p -> players.add(p));
                        for (Player p : players) {
                            String uuid = p.getUniqueId().toString().replaceAll("-", "");
                            if (getConfig().contains("users." + uuid) && getTribe(p).equals(getTribe(senderP))) {
                                sendMsg(p, "&6<" + getTribe(senderP) + "> &c" + sender.getName() + "&6:&c" + msg);
                            }
                        }
                    }
                } else if (args[0].equalsIgnoreCase("create")) {
                    Pattern p = Pattern.compile(".*\\W+.*");
                    if (args.length != 3) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe create <name> <type>");
                        sendMsg(senderP, prefix + "&7Types:");
                        sendMsg(senderP, " &6Aesir&8 - &7" + typesDesc("Aesir"));
                        sendMsg(senderP, " &6Vanir&8 - &7" + typesDesc("Vanir"));
                    } else if (!args[2].equalsIgnoreCase("Aesir") && !args[2].equalsIgnoreCase("Vanir")) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe create <name> <type>");
                        sendMsg(senderP, prefix + "&7Types:");
                        sendMsg(senderP, " &6Aesir&8 - &7" + typesDesc("Aesir"));
                        sendMsg(senderP, " &6Vanir&8 - &7" + typesDesc("Vanir"));
                    } else if (inTribe(sender)) {
                        sendMsg(senderP, prefix + "&4You are already in a tribe!");
                    } else if (p.matcher(args[1]).find()) {
                        sendMsg(senderP, prefix + "&4Tribe name must be alphanumeric!");
                    } else {
                        getConfig().set("tribes." + args[1] + ".description", "Default description.");
                        getConfig().set("tribes." + args[1] + ".level", 1);
                        getConfig().set("tribes." + args[1] + ".type", args[2]);
                        getConfig().set("tribes." + args[1] + ".balance", 0.0);
                        getConfig().set("users." + senderID + ".name", senderP.getName());
                        getConfig().set("users." + senderID + ".tribe", args[1]);
                        getConfig().set("users." + senderID + ".rank", "Chief");
                        saveConfig();
                        sendMsg(senderP,
                                prefix + "&7Created a new &6" + args[2] + "&7 tribe called &c" + args[1] + "&7.");
                    }
                }else if(args[0].equalsIgnoreCase("deny")) {
                    if(args.length<2) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe deny <tribe>");
                    }else if(!invited.containsKey(senderP)) {
                        sendMsg(senderP, prefix+"&4You have no pending invitations!");
                    }else if(!args[1].equals(invited.get(senderP))) {
                        sendMsg(senderP, prefix+"&4You have no pending invitations from that tribe!");
                    }else {
                        sendMsg(senderP, prefix+"&7You have denied the invitation from &c"+args[1]+"&7.");
                        Set<String> uuids = getConfig().getConfigurationSection("users").getKeys(false);
                        for(String s:uuids) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(s);
                            sb.insert(20, "-");
                            sb.insert(16, "-");
                            sb.insert(12, "-");
                            sb.insert(8, "-");
                            UUID u = UUID.fromString(sb.toString());
                            if(getTribe(s).equals(args[1])&&getConfig().getString("users."+s+".rank").equals("Chief")&&server.getPlayer(u)!=null) {
                                sendMsg(server.getPlayer(u),prefix+"&c"+senderP.getName()+"&7 has denied your tribe invitation.");
                                break;
                            }
                        }
                        invited.remove(senderP);
                    }
                }else if (args[0].equalsIgnoreCase("deposit")) {
                    if (args.length < 2) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe deposit <amount>");
                    } else if (!inTribe(senderP)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    } else {
                        boolean isDouble = true;
                        double money = 0.0;
                        try {
                            money = Double.parseDouble(args[1]);
                        } catch (Exception e) {
                            isDouble = false;
                        }
                        if (isDouble == true && money > 0) {
                            Double origBalP = economy.getBalance(senderP);
                            Double origBalT = getConfig().getDouble("tribes." + getTribe(senderP) + ".balance");
                            if (origBalP - money < 0) {
                                sendMsg(senderP, prefix + "&4You do not have that much money!");
                            } else {
                                double newBal = origBalT + money;
                                getConfig().set("tribes." + getTribe(senderP) + ".balance", newBal);
                                economy.withdrawPlayer(senderP, money);
                                saveConfig();
                                sendMsg(senderP,
                                        prefix + "&7Deposited &a$" + money + "&7 into tribe. New tribe balance: &a$"
                                                + getConfig().getDouble("tribes." + getTribe(senderP) + ".balance")
                                                + "&7.");
                            }
                        } else {
                            sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe deposit <amount>");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("desc") || args[0].equalsIgnoreCase("description")) {
                    if (args.length < 2) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe description <string>");
                    } else if (!inTribe(senderP)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    } else {
                        args[0] = "";
                        String temp = arrayToString(args);
                        getConfig().set("tribes." + getTribe(senderP) + ".description", temp);
                        saveConfig();
                        sendMsg(senderP, prefix + "&7Tribe description set to:&6" + temp + "&7.");
                    }
                } else if (args[0].equalsIgnoreCase("disband")) {
                    if (!inTribe(senderP)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    } else if (!getConfig().getString("users." + senderID + ".rank").equals("Chief")) {
                        sendMsg(senderP, prefix + "&4You must be the Chief of the tribe to do so!");
                    } else {
                        Set<String> uuids = getConfig().getConfigurationSection("users").getKeys(false);
                        for (String s : uuids) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(s);
                            sb.insert(20, "-");
                            sb.insert(16, "-");
                            sb.insert(12, "-");
                            sb.insert(8, "-");
                            UUID u = UUID.fromString(sb.toString());
                            if (server.getPlayer(u) != null && getTribe(s).equalsIgnoreCase(getTribe(senderP))) {
                                sendMsg(server.getPlayer(u),
                                        prefix + "&7Tribe &c" + getTribe(senderP) + "&7 was disbanded.");
                                getConfig().set("users." + s.replaceAll("-", ""), null);
                            }
                        }
                        getConfig().set("tribes." + getTribe(senderP), null);
                        saveConfig();
                    }
                } else if (args[0].equalsIgnoreCase("help")) {
                    sendMsg(senderP, prefix + "&7Available commands:");
                    sendMsg(senderP, " &4/tribe accept&8 - &7Accepts a tribe invitation.");
                    sendMsg(senderP, " &4/tribe bal|balance&8 - &7Creates a new tribe.");
                    sendMsg(senderP, " &4/tribe c|chat&8 - &7Talks in tribe chat.");
                    sendMsg(senderP, " &4/tribe create&8 - &7Creates a new tribe.");
                    sendMsg(senderP, " &4/tribe deny&8 - &7Denies a tribe invitation.");
                    sendMsg(senderP, " &4/tribe deposit&8 - &7Deposits money into the tribe bank.");
                    sendMsg(senderP, " &4/tribe disband&8 - &7Disbands the entire tribe.");
                    sendMsg(senderP, " &4/tribe desc|description&8 - &7Sets tribe description.");
                    sendMsg(senderP, " &4/tribe help&8 - &7Shows this help dialogue.");
                    sendMsg(senderP, " &4/tribe info&8 - &7Shows information of a tribe.");
                    sendMsg(senderP, " &4/tribe invite&8 - &7Invites player to tribe.");
                    sendMsg(senderP, " &4/tribe leave&8 - &7Leaves current tribe.");
                    sendMsg(senderP, " &4/tribe kick&8 - &7Kicks player from tribe.");
                    sendMsg(senderP, " &4/tribe rankup&8 - &7Ranks up tribe.");
                    sendMsg(senderP, " &4/tribe reload&8 - &7Reloads configuration.");
                    sendMsg(senderP, " &4/tribe type&8 - &7Sets the type of your tribe.");
                    sendMsg(senderP, " &4/tribe withdraw&8 - &7Withdraws money from the tribe bank.");
                } else if (args[0].equalsIgnoreCase("info")) {
                    if (args.length > 2) {
                        if(getConfig().contains(args[2])) {
                            sendMsg(senderP, prefix + "&c&l" + args[2]);
                            sendMsg(senderP,
                                    " &7Type: &6" + getConfig().getString("tribes." + args[2] + ".type"));
                            sendMsg(senderP,
                                    " &7Level: &e" + getConfig().getInt("tribes." + args[2] + ".level"));
                            sendMsg(senderP, " &7Description: &c"
                                    + getConfig().getString("tribes." + args[2] + ".description"));
                            sendMsg(senderP, " &7Members:");
                            Set<String> uuids = getConfig().getConfigurationSection("users").getKeys(false);
                            for (String u : uuids) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("   ");
                                if (getTribe(u).equals(args[2])) {
                                    if (getConfig().getString("users." + u + ".rank").equals("Chief")) {
                                        sb.append("&dChief ");
                                    }
                                    sb.append("&c" + getConfig().getString("users." + u + ".name"));
                                    sendMsg(senderP, sb.toString());
                                }
                            }
                        }else {
                            sendMsg(senderP, prefix+ "&4Tribe does not exist.");
                        }
                    }else {
                        if (!inTribe(sender)) {
                            sendMsg(senderP, prefix + "&4You are not in a tribe!");
                        } else {
                            sendMsg(senderP, prefix + "&c&l" + getTribe(senderP));
                            sendMsg(senderP,
                                    " &7Type: &6" + getConfig().getString("tribes." + getTribe(senderP) + ".type"));
                            sendMsg(senderP,
                                    " &7Level: &e" + getConfig().getInt("tribes." + getTribe(senderP) + ".level"));
                            sendMsg(senderP, " &7Balance: &a$"
                                    + getConfig().getDouble("tribes." + getTribe(senderP) + ".balance"));
                            sendMsg(senderP, " &7Description: &c"
                                    + getConfig().getString("tribes." + getTribe(senderP) + ".description"));
                            sendMsg(senderP, " &7Members:");
                            Set<String> uuids = getConfig().getConfigurationSection("users").getKeys(false);
                            for (String u : uuids) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("   ");
                                if (getTribe(u).equals(getTribe(senderP))) {
                                    if (getConfig().getString("users." + u + ".rank").equals("Chief")) {
                                        sb.append("&dChief ");
                                    }
                                    sb.append("&c" + getConfig().getString("users." + u + ".name"));
                                    sendMsg(senderP, sb.toString());
                                }
                            }
                        }
                    }
                }else if(args[0].equalsIgnoreCase("invite")) {
                    if(args.length<2) {
                        sendMsg(senderP,prefix+"&4Invalid arguments. Usage: /tribe invite <player>"); 
                    }else if(args[1].equalsIgnoreCase(senderP.getName())){
                        sendMsg(senderP,prefix+"&4You cannot invite yourself!");
                    }else if(!inTribe(senderP)) {
                        sendMsg(senderP,prefix+"&4You are not in a tribe!");
                    }else if(!getConfig().getString("users."+senderID+".rank").equals("Chief")) {
                        sendMsg(senderP,prefix+"&4You must be the Chief of the tribe to do so!");
                    }else if(server.getPlayer(args[1])!=null){
                        Player p = server.getPlayer(args[1]);
                        if(invited.containsKey(p)) {
                            sendMsg(senderP,prefix+"&4You have sent this user an invitation already!");
                        }else {
                            invited.put(p, getTribe(senderP));
                            String pName = p.getName();
                            Runnable task = new Runnable() {
                                public void run() {
                                    if(invited.containsKey(p)) {
                                        if(server.getPlayer(args[1])!=null) {
                                            sendMsg(p,prefix+"&7Your previous invitation from &c"+invited.get(p)+"&7 has expired.");
                                        }
                                        invited.remove(p);
                                        sendMsg(senderP,prefix+"&7Your previous invitation to &c"+pName+"&7 has expired.");
                                    }
                                }
                            };
                            server.getScheduler().runTaskLater(this,task,1200L);
                            String tribe = getTribe(senderP);
                            sendMsg(senderP,prefix+"&7You have invited&c "+pName+"&7 to &c"+tribe+"&7.");
                            sendMsg(p,prefix+"&7You have been invited to &c"+tribe+"&7.");
                            sendMsg(p,prefix+"&7Do &c/tribe <accept|deny> <tribe>&7. This invitation expires in 60 seconds.");
                        }
                    }else {
                        sendMsg(senderP,prefix+"&4User is not online!");
                    }
                }else if (args[0].equalsIgnoreCase("kick")) {
                    if (args.length < 2) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe kick <player>");
                    } else {
                        String userName = "nouserexistsasthisname", userID = "nouserexistsasthisname";
                        Set<String> uuids = getConfig().getConfigurationSection("users").getKeys(false);
                        for (String s : uuids) {
                            if (getConfig().getString("users." + s + ".name").equalsIgnoreCase(args[1])) {
                                userName = getConfig().getString("users." + s + ".name");
                                userID = s.toString().replaceAll("-", "");
                                break;
                            }
                        }
                        if (!inTribe(senderP)) {
                            sendMsg(senderP, prefix + "&4You are not in a tribe!");
                        } else if (userName.equals("nouserexistsasthisname")) {
                            sendMsg(senderP, prefix + "&4User does not exist.");
                        } else if (!getConfig().getString("users." + senderID + ".rank").equals("Chief")) {
                            sendMsg(senderP, prefix + "&4You must be the Chief of the tribe to do so!");
                        } else if (!getTribe(userID).equals(getTribe(senderP))) {
                            sendMsg(senderP, prefix + "&4User is not in your tribe!");
                        } else if (args[1].equalsIgnoreCase(sender.getName())) {
                            sendMsg(senderP, prefix + "&4You cannot kick yourself!");
                        } else {
                            sendMsg(senderP, prefix + "&7Kicked &6" + args[1] + "&7 from tribe.");
                            if (server.getPlayer(args[1]) != null) {
                                sendMsg(server.getPlayer(args[1]),
                                        prefix + "&7You have been kicked from &c" + getTribe(senderP) + "&7!");
                            }
                            for (String s : uuids) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(s);
                                sb.insert(20, "-");
                                sb.insert(16, "-");
                                sb.insert(12, "-");
                                sb.insert(8, "-");
                                UUID u = UUID.fromString(sb.toString());
                                if (server.getPlayer(u) != null&&getTribe(s).equals(getTribe(args[1]))) {
                                    sendMsg(server.getPlayer(u),
                                            prefix + "&c" + senderP.getName() + "&7 has been kicked from the tribe.");
                                }
                            }
                            getConfig().set("users." + userID, null);
                            saveConfig();
                        }
                    }
                } else if (args[0].equalsIgnoreCase("leave")) {
                    if (!inTribe(senderP)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    } else if (getConfig().getString("users." + senderID + ".rank").equals("Chief")) {
                        sendMsg(senderP, prefix + "&4You are the Chief of the tribe!");
                    } else {
                        String oldTribe = getTribe(senderP);
                        sendMsg(senderP, prefix + "&7Left &c" + getTribe(senderP) + "&7 tribe.");
                        getConfig().set("users." + senderID, null);
                        saveConfig();
                        Set<String> uuids = getConfig().getConfigurationSection("users").getKeys(false);
                        for (String s : uuids) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(s);
                            sb.insert(20, "-");
                            sb.insert(16, "-");
                            sb.insert(12, "-");
                            sb.insert(8, "-");
                            UUID u = UUID.fromString(sb.toString());
                            if (server.getPlayer(u) != null&&getTribe(s).equalsIgnoreCase(oldTribe)) {
                                sendMsg(server.getPlayer(u),
                                        prefix + "&c" + senderP.getName() + "&7 has left the tribe.");
                            }
                        }
                    }
                } else if (args[0].equalsIgnoreCase("rankup")) {
                    if(!inTribe(senderP)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    } else if (!getConfig().getString("users." + senderID + ".rank").equals("Chief")) {
                        sendMsg(senderP, prefix + "&4You must be the Chief of the tribe to do so!");
                    } else if (!getConfig().contains("settings.prices."+(getConfig().getInt("tribes."+getTribe(senderP)+".level")))){
                        sendMsg(senderP, prefix+"&4Tribe is at max rank!");
                    }else {
                        int level = getConfig().getInt("tribes."+getTribe(senderP)+".level");
                        double bal = getConfig().getDouble("tribes."+getTribe(senderP)+".balance");
                        double req = getConfig().getInt("settings.prices."+level);
                        if(bal<req) {
                            sendMsg(senderP, prefix+"&4Tribe does not have enough money to rankup! Needed: &a$"+req);
                            sendMsg(senderP, prefix + "&7Tribe currently has &a$"
                                    + getConfig().getDouble("tribes." + getTribe(senderP) + ".balance") + "&7.");
                        }else {
                            sendMsg(senderP, prefix+"&7Tribe leveled up to level &e"+(level+1)+"&7!");
                            getConfig().set("tribes."+getTribe(senderP)+".level", level+1);
                            getConfig().set("tribes."+getTribe(senderP)+".balance", bal-req);
                            sendMsg(senderP, prefix + "&7Tribe currently has &a$"
                                    + getConfig().getDouble("tribes." + getTribe(senderP) + ".balance") + "&7.");
                            saveConfig();
                        }
                    }
                }else if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("tribe.reload")) {
                        reloadConfig();
                        sendMsg(senderP, prefix + "&7Successfully reloaded.");
                    } else {
                        sendMsg(senderP, "&8&l[&4&lGuard&8&l]&4 Insufficient permissions.");
                    }
                } else if (args[0].equalsIgnoreCase("type")) {
                    if (args.length != 2) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe type <type>");
                        sendMsg(senderP, prefix + "&7Types:");
                        sendMsg(senderP, " &6Aesir&8 - &7" + typesDesc("Aesir"));
                        sendMsg(senderP, " &6Vanir&8 - &7" + typesDesc("Vanir"));
                    } else if (!args[1].equals("Aesir") && !args[1].equals("Vanir")) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe type <type>");
                        sendMsg(senderP, prefix + "&7Types:");
                        sendMsg(senderP, " &6Aesir&8 - &7" + typesDesc("Aesir"));
                        sendMsg(senderP, " &6Vanir&8 - &7" + typesDesc("Vanir"));
                    } else {
                        if (inTribe(sender)) {
                            getConfig().set("tribes." + getTribe(senderP) + ".type", args[1]);
                            saveConfig();
                            sendMsg(senderP, prefix + "&7Tribe type set to &6" + args[1] + "&7.");
                        } else {
                            sendMsg(senderP, prefix + "&4You are not in a tribe!");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("withdraw")) {
                    if (args.length != 2) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe withdraw <amount>");
                    } else if (!inTribe(senderP)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    } else {
                        boolean isDouble = true;
                        double money = 0.0;
                        try {
                            money = Double.parseDouble(args[1]);
                        } catch (Exception e) {
                            isDouble = false;
                        }
                        if (isDouble == true && money > 0) {
                            Double origBal = getConfig().getDouble("tribes." + getTribe(senderP) + ".balance");
                            if (origBal - money < 0) {
                                sendMsg(senderP, prefix + "&4Tribe does not have that much money! Tribe balance: &a$"
                                        + getConfig().getDouble("tribes." + getTribe(senderP) + ".balance"));
                            } else {
                                double newBal = origBal - money;
                                getConfig().set("tribes." + getTribe(senderP) + ".balance", newBal);
                                saveConfig();
                                economy.depositPlayer(senderP, money);
                                sendMsg(senderP, prefix + "&7Withdrew &a$" + money + "&7 from tribe. New balance: &a$"
                                        + getConfig().getDouble("tribes." + getTribe(senderP) + ".balance") + "&7.");
                            }
                        } else {
                            sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe withdraw <amount>");
                        }
                    }
                } else {
                    sendMsg(senderP, prefix + "&4Invalid subcommand. Usage: /tribe help");
                }
            }
        }
        return true;
    }
}
