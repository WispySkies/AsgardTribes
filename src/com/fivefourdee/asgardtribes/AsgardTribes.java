package com.fivefourdee.asgardtribes;

import net.md_5.bungee.api.ChatColor;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class AsgardTribes extends JavaPlugin implements Listener {
    
    Logger logger = this.getLogger();
    Server server = getServer();
    String prefix = this.getConfig().getString("settings.prefix");
    FileConfiguration config = this.getConfig();
    PluginDescriptionFile pdfile = this.getDescription();
    
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
    
    public void sendMsg(Player player, String msg) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }
    
    
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        createConfig();
    }
    
    /*
     * @Override public void onDisable(){ saveConfig(); }
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
                sendMsg(senderP, " &cCoded by&4 54D");
            } else {
                if (args[0].equalsIgnoreCase("bal")||args[0].equalsIgnoreCase("balance")) {
                    if (!inTribe(sender)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    } else {
                        sendMsg(senderP, prefix + "&7Tribe currently has &a$"+getConfig().getDouble("tribes."+tribe+".balance")+"&7.");
                    }
                }
                if (args[0].equalsIgnoreCase("create")) {
                    if (args.length != 3) {
                        sendMsg(senderP, prefix + "&4Invalid arguments. Usage: /tribe create <name> <type>");
                        sendMsg(senderP, prefix + "&7Types:");
                        sendMsg(senderP, " &6Aesir&8 - &7desc1");
                        sendMsg(senderP, " &6Vanir&8 - &7desc2");
                    } else if (!args[2].equals("Aesir") && !args[2].equals("Vanir")) {
                        sendMsg(senderP, 
                                prefix + "&4Invalid arguments. Usage: /tribe create <name> <type>");
                        sendMsg(senderP, prefix + "&7Types:");
                        sendMsg(senderP, " &6Aesir&8 - &7desc1");
                        sendMsg(senderP, " &6Vanir&8 - &7desc2");
                    } else if (inTribe(sender)) {
                        sendMsg(senderP, prefix + "&4You are already in a tribe!");
                    } else {
                        getConfig().set("tribes." + args[1] + ".description",
                                "Default description.");
                        getConfig().set("tribes." + args[1] + ".level", 1);
                        getConfig().set("tribes." + args[1] + ".type", args[2]);
                        getConfig().set("tribes." + args[1] + ".balance", 0);
                        getConfig().set("users." + senderID + ".name", senderP.getName());
                        getConfig().set("users." + senderID + ".tribe", args[1]);
                        getConfig().set("users." + senderID + ".rank", "Chief");
                        saveConfig();
                        sendMsg(senderP, prefix + "&7Created a new &6"+args[2]+"&7 tribe called &4"+args[1]+"&7.");
                    }
                } else if (args[0].equalsIgnoreCase("help")) {
                    sendMsg(senderP, prefix + "&7Available commands:");
                    sendMsg(senderP, " &4/tribe bal|balance&8 - &7Creates a new tribe.");
                    sendMsg(senderP, " &4/tribe create&8 - &7Creates a new tribe.");
                    sendMsg(senderP, " &4/tribe help&8 - &7Shows this help dialogue.");
                    sendMsg(senderP, " &4/tribe reload&8 - &7Reloads configuration.");
                    sendMsg(senderP, " &4/tribe type&8 - &7Sets the type of your tribe.");
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
                        sendMsg(senderP, " &6Aesir&8 - &7desc1");
                        sendMsg(senderP, " &6Vanir&8 - &7desc2");
                    } else if (!args[1].equals("Aesir") && !args[1].equals("Vanir")) {
                        sendMsg(senderP, 
                                prefix + "&4Invalid arguments. Usage: /tribe type <type>");
                        sendMsg(senderP, prefix + "&7Types:");
                        sendMsg(senderP, " &6Aesir&8 - &7desc1");
                        sendMsg(senderP, " &6Vanir&8 - &7desc2");
                    } else {
                        if (inTribe(sender)) {
                            getConfig().set("tribes." + tribe + ".type", args[1]);
                            saveConfig();
                            sendMsg(senderP, prefix + "&7Tribe type set to &6" + args[1] + "&7.");
                        } else {
                            sendMsg(senderP, prefix + "&4You are not in a tribe!");
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
