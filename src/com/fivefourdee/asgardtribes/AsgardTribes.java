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

public class AsgardTribes extends JavaPlugin implements Listener{
	
	Logger logger = this.getLogger();
	Server server = getServer();
	String prefix = this.getConfig().getString("settings.prefix");
	FileConfiguration config = this.getConfig();
	PluginDescriptionFile pdfile = this.getDescription();
	
	private void createConfig(){
	    try{
	        if(!getDataFolder().exists()){
	            getDataFolder().mkdirs();
	        }
	        File file = new File(getDataFolder(),"config.yml");
	        if(!file.exists()){
	            getLogger().info("config.yml not found, creating! Please unload and load this plugin again!");
	    		/*
	            getConfig().set("settings.prefix","&8[&6Tribes&8]&r ");
	    		getConfig().set("tribes.Default.description","Default tribe. If you see this, then your old config probably fucked up.");
	    		getConfig().set("tribes.Default.level",1);
	    		getConfig().set("tribes.Default.type","[]");
	    		getConfig().set("tribes.Default.balance",0);
	    		getConfig().set("users.default.name","Default");
	    		getConfig().set("users.default.tribe","Default");
	    	    */
	            saveDefaultConfig();
	        }
	        else{
	            getLogger().info("config.yml found, loading!");
	        }
	    }catch(Exception e){
	        e.printStackTrace();
	    }
	}
	
	private boolean inTribe(CommandSender sender){
		Player senderP = (Player)sender;
		String tribe = getConfig().getString("users."+senderP.getUniqueId().toString().replace("-","")+".tribe");
		if(tribe==null){
			return false;
		}
		else{
			return true;
		}
	}
	
	@Override
	public void onEnable(){
		Bukkit.getPluginManager().registerEvents(this,this);
		createConfig();
	}
	
	/*@Override
	public void onDisable(){
	    saveConfig();
	}*/
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("tribe")||cmd.getName().equalsIgnoreCase("tribes")){
			if(args.length<1){
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+"&c&lAsgard&6&lTribes"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&'," &cRunning on &4"+server.getServerName()+":"+server.getName()));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&'," &cVersion&4 "+pdfile.getVersion()));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&'," &cCoded by&4 54D"));
			}
			else{
				if(args[0].equalsIgnoreCase("help")){
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+"&7Available commands:"));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&'," &4/tribe help&8 - &6Shows this help dialogue."));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&'," &4/tribe reload&8 - &6Reloads configuration."));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&'," &4/tribe type&8 - &6Sets the type of your tribe."));
				}
				else if(args[0].equalsIgnoreCase("reload")){
					if(sender.hasPermission("tribe.reload")){
						reloadConfig();
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+"&7Successfully reloaded."));
					}
					else{
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&8&l[&4&lGuard&8&l]&4 Insufficient permissions."));
					}
				}
				else if(args[0].equalsIgnoreCase("type")){
					if(args.length!=2){
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+"&4Invalid arguments. Usage: /tribe type <type>"));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+"&7Types:"));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&'," &4Aesir&8 - &6desc1"));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&'," &4Vanir&8 - &6desc2"));
					}
					else if(!args[1].equals("Aesir")&&!args[1].equals("Vanir")){
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+"&4Invalid arguments. Usage: /tribe type <type>"));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+"&7Types:"));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&'," &4Aesir&8 - &6desc1"));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&'," &4Vanir&8 - &6desc2"));
					}
					else{
						if(inTribe(sender)){
							Player senderP = (Player)sender;
							String tribe = getConfig().getString("users."+senderP.getUniqueId().toString().replace("-","")+".tribe");
							getConfig().set("tribes."+tribe+".type",args[1]);
							saveConfig();
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+"&7Tribe type set to "+args[1]+"."));
						}
						else{
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+"&4You are not in a tribe!"));
						}
					}
				}
				else{
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+"&4Invalid subcommand. Usage: /tribe help"));
				}
			}
		}
		return true;
	}
}
