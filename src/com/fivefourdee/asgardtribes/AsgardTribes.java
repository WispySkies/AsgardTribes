package com.fivefourdee.asgardtribes;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

    private boolean setupEconomy(){
        logger = getLogger();
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null){
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
                /*
                getConfig().set("settings.prefix", "&8[&6Tribes&8]&r ");
                getConfig().set("tribes.Default.description",
                        "Default tribe. If you see this, then your old config probably fucked up.");
                getConfig().set("tribes.Default.level", 1);
                getConfig().set("tribes.Default.type", "Aesir");
                getConfig().set("tribes.Default.balance", 0.0);
                getConfig().set("users.default.name", "Default");
                getConfig().set("users.default.tribe", "Default");
                getConfig().set("users.default.rank", "Chief");
                */
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
        String tribe = getConfig().getString("users." + senderP.getUniqueId().toString().replace("-", "") + ".tribe");
        if (tribe == null) {
            return false;
        } else {
            return true;
        }
    }
    
    private String arrayToString(String[] array){
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for(String particle:array){
            if(!first){
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
        if(type.equals("Aesir")) {
            desc =  "A technique passed down from generation to generation. "+
                    "Drawing from the power of his soul, the caster unleashes "+
                    "a devastating strike to all who stand in his way. "+
                    "However, the soul is a fragile object, and by drawing "+
                    "power from itself, the backlash to the caster\'s physical "+
                    "wellbeing shall be severe.";
        }
        else if(type.equals("Vanir")) {
            desc =  "A technique passed down through ages. "+
                    "Drawing from the network of souls of present tribe members, "+
                    "the caster creates a massive Fireball capable of annihilating "+
                    "even the strongest of Gods and deities. However, since the "+
                    "Fireball is created by a network of souls, its strength shall "+
                    "depend on the complexity of the network.";
        }
        else {
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
    @Override public void onDisable(){ saveConfig(); }
    */
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player senderP = (Player)sender;
        String senderID = senderP.getUniqueId().toString().replace("-", "");
        String tribe = getConfig()
                .getString("users." + senderID + ".tribe");
        
        if (cmd.getName().equalsIgnoreCase("tribe") || cmd.getName().equalsIgnoreCase("tribes")) {
            if (args.length < 1) {
                sendMsg(senderP, prefix + "&c&lAsgard&6&lTribes");
                sendMsg(senderP, " &cRunning on &4" + server.getServerName() + ":" + server.getName());
                sendMsg(senderP, " &cVersion&4 " + pdfile.getVersion());
                sendMsg(senderP, " &cCoded by&4 54D&c with help from &4Qubd&c, &4XMen&c, &4uncensored anime");
            } else {
                if (args[0].equalsIgnoreCase("bal")||args[0].equalsIgnoreCase("balance")) {
                    if (!inTribe(sender)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    } else {
                        sendMsg(senderP, prefix + "&7Tribe currently has &a$"+getConfig().getDouble("tribes."+tribe+".balance")+"&7.");
                    }
                } else if (args[0].equalsIgnoreCase("create")) {
                    if (args.length != 3) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe create <name> <type>");
                        sendMsg(senderP, prefix + "&7Types:");
                        sendMsg(senderP, " &6Aesir&8 - &7"+typesDesc("Aesir"));
                        sendMsg(senderP, " &6Vanir&8 - &7"+typesDesc("Vanir"));
                    } else if (!args[2].equals("Aesir") && !args[2].equals("Vanir")) {
                        sendMsg(senderP, 
                                prefix + "&4Invalid arguments. Usage: /tribe create <name> <type>");
                        sendMsg(senderP, prefix + "&7Types:");
                        sendMsg(senderP, " &6Aesir&8 - &7"+typesDesc("Aesir"));
                        sendMsg(senderP, " &6Vanir&8 - &7"+typesDesc("Vanir"));
                    } else if (inTribe(sender)) {
                        sendMsg(senderP, prefix + "&4You are already in a tribe!");
                    } else {
                        getConfig().set("tribes." + args[1] + ".description",
                                "Default description.");
                        getConfig().set("tribes." + args[1] + ".level", 1);
                        getConfig().set("tribes." + args[1] + ".type", args[2]);
                        getConfig().set("tribes." + args[1] + ".balance", 0.0);
                        getConfig().set("users." + senderID + ".name", senderP.getName());
                        getConfig().set("users." + senderID + ".tribe", args[1]);
                        getConfig().set("users." + senderID + ".rank", "Chief");
                        saveConfig();
                        sendMsg(senderP, prefix + "&7Created a new &6"+args[2]+"&7 tribe called &c"+args[1]+"&7.");
                    }
                } else if (args[0].equalsIgnoreCase("deposit")) {
                    if (args.length != 2) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe deposit <amount>");
                    } else if (!inTribe(senderP)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    } else {
                        boolean isDouble = true;
                        double money = 0.0;
                        try{
                            money = Double.parseDouble(args[1]);
                        } catch (Exception e) {
                            isDouble = false;
                        }
                        if(isDouble==true && money>0) {
                            Double origBalP = economy.getBalance(senderP);
                            Double origBalT = getConfig().getDouble("tribes."+tribe+".balance");
                            if (origBalP-money<0) {
                                sendMsg(senderP, prefix + "&4You do not have that much money!");                                
                            } else {
                                double newBal = origBalT+money;
                                getConfig().set("tribes."+tribe+".balance", newBal);
                                economy.withdrawPlayer(senderP,money);
                                saveConfig();
                                sendMsg(senderP, prefix + "&7Deposited &a$"+money+"&7 into tribe. New tribe balance: &a$"+getConfig().getDouble("tribes."+tribe+".balance")+"&7.");
                            }
                        } else {
                            sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe deposit <amount>");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("desc")||args[0].equalsIgnoreCase("description")) {
                    if(args.length<2) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe description <string>");
                    } else if (!inTribe(senderP)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    } else {
                        args[0]="";
                        String temp = arrayToString(args);
                        getConfig().set("tribes."+tribe+".description", temp);
                        saveConfig();
                        sendMsg(senderP, prefix + "&7Tribe description set to:&6"+temp+"&7.");
                    }
                } else if (args[0].equalsIgnoreCase("help")) {
                    sendMsg(senderP, prefix + "&7Available commands:");
                    sendMsg(senderP, " &4/tribe bal|balance&8 - &7Creates a new tribe.");
                    sendMsg(senderP, " &4/tribe create&8 - &7Creates a new tribe.");
                    sendMsg(senderP, " &4/tribe deposit&8 - &7Deposits money into the tribe bank.");
                    sendMsg(senderP, " &4/tribe desc|description&8 - &7Sets tribe description.");
                    sendMsg(senderP, " &4/tribe help&8 - &7Shows this help dialogue.");
                    sendMsg(senderP, " &4/tribe kick&8 - &7Kicks player from tribe.");
                    sendMsg(senderP, " &4/tribe reload&8 - &7Reloads configuration.");
                    sendMsg(senderP, " &4/tribe type&8 - &7Sets the type of your tribe.");
                    sendMsg(senderP, " &4/tribe withdraw&8 - &7Withdraws money from the tribe bank.");
                } else if (args[0].equalsIgnoreCase("kick")) {
                    if (args.length<2) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe kick <player>");
                    } else {
                        OfflinePlayer kickee = getOfflinePlayer("users."+args[0]);
                        if (!inTribe(senderP)) {
                            sendMsg(senderP, prefix + "&4You are not in a tribe!");
                        }else if (!getConfig().contains("users."+kickee.getUniqueId())){
                            sendMsg(senderP, prefix + "&4User does not exist.");
                        } else if (!getConfig().getString("users."+kickee.getUniqueId()+".tribe").equals("users."+server.getPlayer(senderP.toString()).getUniqueId()+".tribe")) {
                            sendMsg(senderP, prefix + "&4User is not in your tribe!");
                        } else if(!getConfig().getString("users."+server.getPlayer(senderP.toString()).getUniqueId()+".rank").equals("Chief")) {
                            sendMsg(senderP, prefix + "&4You must be the Chief of the tribe to do so!");
                        } else if(args[1].equalsIgnoreCase(sender.getName())){
                            sendMsg(senderP, prefix + "&4You cannot kick yourself!");
                        } else {
                            getConfig().set("users."+kickee.getUniqueId(), null);
                            sendMsg(senderP, prefix + "&7Kicked &6"+args[1]+"&7 from tribe.");
                            if(server.getPlayer(args[1])!=null) {
                                sendMsg(server.getPlayer(args[1]), prefix + "&4You have been kicked from &c"+getConfig().getString("users."+senderP+".tribe")+"&4!");
                            }
                        }
                    }
                } else if (args[0].equalsIgnoreCase("reload")) {
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
                        sendMsg(senderP, " &6Aesir&8 - &7"+typesDesc("Aesir"));
                        sendMsg(senderP, " &6Vanir&8 - &7"+typesDesc("Vanir"));
                    } else if (!args[1].equals("Aesir") && !args[1].equals("Vanir")) {
                        sendMsg(senderP, 
                                prefix + "&4Invalid arguments. Usage: /tribe type <type>");
                        sendMsg(senderP, prefix + "&7Types:");
                        sendMsg(senderP, " &6Aesir&8 - &7"+typesDesc("Aesir"));
                        sendMsg(senderP, " &6Vanir&8 - &7"+typesDesc("Vanir"));
                    } else {
                        if (inTribe(sender)) {
                            getConfig().set("tribes." + tribe + ".type", args[1]);
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
                        try{
                            money = Double.parseDouble(args[1]);
                        } catch (Exception e) {
                            isDouble = false;
                        }
                        if(isDouble==true && money>0) {
                            Double origBal = getConfig().getDouble("tribes."+tribe+".balance");
                            if (origBal-money<0) {
                                sendMsg(senderP, prefix + "&4Tribe does not have that much money! Tribe balance: &a$"+getConfig().getDouble("tribes."+tribe+".balance"));                                
                            } else {
                                double newBal = origBal-money;
                                getConfig().set("tribes."+tribe+".balance", newBal);
                                saveConfig();
                                economy.depositPlayer(senderP,money);
                                sendMsg(senderP, prefix + "&7Withdrew &a$"+money+"&7 from tribe. New balance: &a$"+getConfig().getDouble("tribes."+tribe+".balance")+"&7.");
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
