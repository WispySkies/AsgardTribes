package com.fivefourdee.asgardtribes;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class AsgardTribes extends JavaPlugin implements Listener {
    
    Logger logger = this.getLogger();
    private Economy economy;
    private Permission perms;
    Server server = getServer();
    String prefix = this.getConfig().getString("settings.prefix");
    FileConfiguration config = this.getConfig();
    PluginDescriptionFile pdfile = this.getDescription();
    Map<Player,String> invited = new LinkedHashMap<Player,String>();
    Map<Player,Boolean> swordCd = new HashMap<Player,Boolean>();
    
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
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
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
    
    private double getTribeBal(String tribe) {
        return getConfig().getDouble("tribes." + tribe+ ".balance");
    }
    
    private int getTribeLevel(String tribe) {
        return getConfig().getInt("tribes." + tribe+ ".level");
    }
    
    private String getTribeType(String tribe) {
        return getConfig().getString("tribes."+tribe+".type");
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
        setupPermissions();
    }
    
    /*
     * @Override public void onDisable(){ saveConfig(); }
     */
    
    @EventHandler
    public void onFireballExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof Fireball) {
            if(event.getEntity().hasMetadata("tribeskill")){
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if(inTribe(p)) {
            ItemStack sword = new ItemStack(Material.GOLD_SWORD,1);
            ItemMeta im = sword.getItemMeta();
            im.setDisplayName(ChatColor.RED+""+ChatColor.BOLD+"Tribe Sword");
            ArrayList<String> swordLore = new ArrayList<String>();
            swordLore.add(ChatColor.GRAY+"Bonding X");
            swordLore.add(ChatColor.RED+"A magical blade that releases powerful");
            swordLore.add(ChatColor.RED+"magic upon usage through drawing the");
            swordLore.add(ChatColor.RED+"power of present souls in the tribe.");
            im.setLore(swordLore);
            sword.addUnsafeEnchantment(Enchantment.DURABILITY,10);
            im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            sword.setItemMeta(im);
            int level = getTribeLevel(getTribe(p));
            int cooldown = getConfig().getInt("settings.swordcd."+level);
            if(p.getInventory().getItemInMainHand().equals(sword)&&(event.getAction()==Action.RIGHT_CLICK_AIR||event.getAction()==Action.RIGHT_CLICK_BLOCK)) {
                if(swordCd.getOrDefault(p,false)==false) {
                    swordCd.put(p,true);
                    Runnable task = new Runnable() {
                        public void run() {
                            swordCd.remove(p);
                            if(server.getOnlinePlayers().contains(p)) {
                                sendMsg(p,prefix+"&7Your Tribe Sword skill is refreshed!");
                            }
                        }
                    };
                    server.getScheduler().runTaskLater(this,task,cooldown*20L);
                    if(getTribeType(getTribe(p)).equals("Aesir")) {
                        Vector lookat = p.getLocation().getDirection().add(new Vector(0.0D,-0.1D,0.0D)).normalize();
                        Fireball fireball = p.getWorld().spawn(p.getLocation().add(new Vector(0.0D, 3.0D, 0.0D)),Fireball.class);
                        fireball.setVelocity(lookat);
                        fireball.setIsIncendiary(false);
                        fireball.setMetadata("tribeskill",new FixedMetadataValue(this,"yes"));
                    }else {
                        perms.playerAdd(p.getWorld().getName(),server.getOfflinePlayer(p.getUniqueId()),"essentials.lightning");
                        server.dispatchCommand(server.getConsoleSender(),"sudo "+p.getName()+" smite");
                        perms.playerRemove(p.getWorld().getName(),server.getOfflinePlayer(p.getUniqueId()),"essentials.lightning");
                        }
                    sendMsg(p,prefix+"&7Released Tribe Sword skill!");
                }else {
                    sendMsg(p,prefix+"&4Tribe sword skill on cooldown!");
                }
            }
        }
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player senderP = (Player) sender;
        String senderID = senderP.getUniqueId().toString().replace("-", "");
        
        if (cmd.getName().equalsIgnoreCase("tribe") || cmd.getName().equalsIgnoreCase("tribes")) {
            if (args.length < 1) {
                sendMsg(senderP, prefix + "&4Usage: /tribe help");
                sendMsg(senderP, prefix + "&c&lAsgard&6&lTribes");
                sendMsg(senderP, " &cRunning on &4" + server.getServerName() + ":" + server.getName());
                sendMsg(senderP, " &cVersion&4 " + pdfile.getVersion());
                sendMsg(senderP, " &cCoded by&4 54D&c with help from &4Qubd&c, &4XMen&c, &4Arpolix&c, &4CRAVENSCRAFT");
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
                }else if(args[0].equalsIgnoreCase("ally")) {
                    if(args.length<2) {
                        sendMsg(senderP,prefix+"&4Invalid arguments. Usage: /tribe ally <tribe>");
                    }else if(!inTribe(sender)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    }else if (!getConfig().getString("users." + senderID + ".rank").equals("Chief")) {
                        sendMsg(senderP, prefix + "&4You must be the Chief of the tribe to do so!");
                    }else if(!getConfig().contains("tribes."+args[1])) {
                        sendMsg(senderP,prefix+"&4Tribe does not exist.");
                    }else {
                        ArrayList<String> allies = new ArrayList<String>();
                        allies.addAll(getConfig().getStringList("tribes."+getTribe(senderP)+".allies"));
                        allies.add(args[1]);
                        getConfig().set("tribes."+getTribe(senderP)+".allies",allies);
                        saveConfig();
                        sendMsg(senderP,prefix+"&7Allied tribe &c"+args[1]+"&7!");
                    }
                }else if (args[0].equalsIgnoreCase("bal") || args[0].equalsIgnoreCase("balance")) {
                    if (!inTribe(sender)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    } else {
                        sendMsg(senderP, prefix + "&7Tribe currently has &a$"
                                + getTribeBal(getTribe(senderP)) + "&7.");
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
                    } else if (economy.getBalance(server.getOfflinePlayer(senderP.getUniqueId()))<getConfig().getDouble("settings.cost")) {
                        sendMsg(senderP, prefix + "&4You do not have enough money! It costs $"+getConfig().getDouble("settings.cost")+" to create a new Tribe.");
                    } else if (p.matcher(args[1]).find()) {
                        sendMsg(senderP, prefix + "&4Tribe name must be alphanumeric!");
                    } else if (getConfig().contains("tribes."+args[1])){
                        sendMsg(senderP, prefix +"&4Tribe name is taken, please choose another one!");
                    } else {
                        getConfig().set("tribes." + args[1] + ".description", "Default description.");
                        getConfig().set("tribes." + args[1] + ".level", 1);
                        getConfig().set("tribes." + args[1] + ".type", args[2]);
                        getConfig().set("tribes." + args[1] + ".balance", 0.0);
                        getConfig().set("users." + senderID + ".name", senderP.getName());
                        getConfig().set("users." + senderID + ".tribe", args[1]);
                        getConfig().set("users." + senderID + ".rank", "Chief");
                        saveConfig();
                        economy.withdrawPlayer(senderP,getConfig().getDouble("settings.cost"));
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
                            Double origBalT = getTribeBal(getTribe(senderP));
                            if (origBalP - money < 0) {
                                sendMsg(senderP, prefix + "&4You do not have that much money!");
                            } else {
                                double newBal = origBalT + money;
                                getConfig().set("tribes." + getTribe(senderP) + ".balance", newBal);
                                economy.withdrawPlayer(senderP, money);
                                saveConfig();
                                sendMsg(senderP,
                                        prefix + "&7Deposited &a$" + money + "&7 into tribe. New tribe balance: &a$"
                                                + getTribeBal(getTribe(senderP))
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
                    } else if (!getConfig().getString("users." + senderID + ".rank").equals("Chief")) {
                        sendMsg(senderP, prefix + "&4You must be the Chief of the tribe to do so!");
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
                }else if(args[0].equalsIgnoreCase("enemy")) {
                    if(args.length<2) {
                        sendMsg(senderP,prefix+"&4Invalid arguments. Usage: /tribe enemy <tribe>");
                    }else if(!inTribe(sender)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    }else if (!getConfig().getString("users." + senderID + ".rank").equals("Chief")) {
                        sendMsg(senderP, prefix + "&4You must be the Chief of the tribe to do so!");
                    }else if(!getConfig().contains("tribes."+args[1])) {
                        sendMsg(senderP,prefix+"&4Tribe does not exist.");
                    }else {
                        ArrayList<String> enemies = new ArrayList<String>();
                        enemies.addAll(getConfig().getStringList("tribes."+getTribe(senderP)+".enemies"));
                        enemies.add(args[1]);
                        getConfig().set("tribes."+getTribe(senderP)+".enemies",enemies);
                        saveConfig();
                        sendMsg(senderP,prefix+"&7Enemied tribe &c"+args[1]+"&7!");
                    }
                } else if (args[0].equalsIgnoreCase("help")) {
                    sendMsg(senderP, prefix + "&7Available commands:");          
                    sendMsg(senderP, " &c/tribe accept &6<tribe>&8 - &7Accepts a tribe invitation.");
                    sendMsg(senderP, " &c/tribe ally &6<tribe>&8 - &7Marks a tribe as an ally.");
                    sendMsg(senderP, " &c/tribe bal|balance&8 - &7Displays tribe balance.");
                    sendMsg(senderP, " &c/tribe c|chat &6<message>&8 - &7Talks in tribe chat.");
                    sendMsg(senderP, " &c/tribe create&8 &6<name> <type> - &7Creates a new tribe.");
                    sendMsg(senderP, " &c/tribe deny &6<tribe>&8 - &7Denies a tribe invitation.");
                    sendMsg(senderP, " &c/tribe deposit &6<amount>&8 - &7Deposits money into the tribe bank.");
                    sendMsg(senderP, " &c/tribe disband&8 - &7Disbands the entire tribe.");
                    sendMsg(senderP, " &c/tribe desc|description &6[desc]&8 - &7Sets tribe description.");
                    sendMsg(senderP, " &c/tribe enemy &6<tribe>&8 - &7Marks a tribe as an enemy.");
                    sendMsg(senderP, " &c/tribe help&8 - &7Shows this help dialogue.");
                    sendMsg(senderP, " &c/tribe info &6[tribe]&8 - &7Shows information of a tribe.");
                    sendMsg(senderP, " &c/tribe invite &6<player>&8 - &7Invites player to tribe.");
                    sendMsg(senderP, " &c/tribe leave&8 - &7Leaves current tribe.");
                    sendMsg(senderP, " &c/tribe kick &6<player>&8 - &7Kicks player from tribe.");
                    sendMsg(senderP, " &c/tribe rankup&8 - &7Ranks up tribe.");
//                    sendMsg(senderP, " &c/tribe reload&8 - &7Reloads configuration.");
                    sendMsg(senderP, " &c/tribe type &6<type>&8 - &7Sets the type of your tribe.");
                    sendMsg(senderP, " &c/tribe withdraw &6<amount>&8 - &7Withdraws money from the tribe bank.");
                } else if (args[0].equalsIgnoreCase("info")) {
                    if (args.length > 1) {
                        if(getConfig().contains("tribes."+args[1])) {
                            sendMsg(senderP, prefix + "&c&l" + args[1]);
                            sendMsg(senderP,
                                    " &7Type: &6" + getTribeType(args[1]));
                            sendMsg(senderP,
                                    " &7Level: &e" + getTribeLevel(args[1]));
                            sendMsg(senderP, " &7Description: &c"
                                    + getConfig().getString("tribes." + args[1] + ".description"));
                            Set<String> uuids = getConfig().getConfigurationSection("users").getKeys(false);
                            sendMsg(senderP, " &7Members:");
                            int count=0;
                            for (String u : uuids) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("   ");
                                if (getTribe(u).equals(args[1])) {
                                    if (getConfig().getString("users." + u + ".rank").equals("Chief")) {
                                        sb.append("&dChief ");
                                    }
                                    sb.append("&c" + getConfig().getString("users." + u + ".name"));
                                    sendMsg(senderP, sb.toString());
                                    count = count+1;
                                }
                            }
                            sendMsg(senderP, "&7 > &c"+count+"/"+getConfig().getInt("settings.members."+getTribeLevel(args[1])));
                            ArrayList<String> allies = new ArrayList<String>();
                            allies.addAll(getConfig().getStringList("tribes."+getTribe(args[1])+".allies"));
                            String[] alliesSA = allies.toArray(new String[allies.size()]);
                            String alliesS = arrayToString(alliesSA);
                            ArrayList<String> enemies = new ArrayList<String>();
                            enemies.addAll(getConfig().getStringList("tribes."+getTribe(senderP)+".enemies"));
                            String[] enemiesSA = enemies.toArray(new String[enemies.size()]);
                            String enemiesS = arrayToString(enemiesSA);
                            sendMsg(senderP, " &7Allies: &c"+alliesS);
                            sendMsg(senderP, " &7Enemies: &c"+enemiesS);
                        }else {
                            sendMsg(senderP, prefix+ "&4Tribe does not exist.");
                        }
                    }else {
                        if (!inTribe(sender)) {
                            sendMsg(senderP, prefix + "&4You are not in a tribe!");
                        } else {
                            sendMsg(senderP, prefix + "&c&l" + getTribe(senderP));
                            sendMsg(senderP,
                                    " &7Type: &6" + getTribeType(getTribe(senderP)));
                            sendMsg(senderP,
                                    " &7Level: &e" + getTribeLevel(getTribe(senderP)));
                            sendMsg(senderP, " &7Balance: &a$"
                                    + getTribeBal(getTribe(senderP)));
                            sendMsg(senderP, " &7Description: &c"
                                    + getConfig().getString("tribes." + getTribe(senderP) + ".description"));
                            Set<String> uuids = getConfig().getConfigurationSection("users").getKeys(false);
                            sendMsg(senderP, " &7Members:");
                            int count=0;
                            for (String u : uuids) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("   ");
                                if (getTribe(u).equals(getTribe(senderP))) {
                                    if (getConfig().getString("users." + u + ".rank").equals("Chief")) {
                                        sb.append("&dChief ");
                                    }
                                    sb.append("&c" + getConfig().getString("users." + u + ".name"));
                                    sendMsg(senderP, sb.toString());
                                    count = count+1;
                                }
                            }
                            sendMsg(senderP, "&7 > &c"+count+"/"+getConfig().getInt("settings.members."+getTribeLevel(getTribe(senderP))));
                            ArrayList<String> allies = new ArrayList<String>();
                            allies.addAll(getConfig().getStringList("tribes."+getTribe(senderP)+".allies"));
                            String[] alliesSA = allies.toArray(new String[allies.size()]);
                            String alliesS = arrayToString(alliesSA);
                            ArrayList<String> enemies = new ArrayList<String>();
                            enemies.addAll(getConfig().getStringList("tribes."+getTribe(senderP)+".enemies"));
                            String[] enemiesSA = enemies.toArray(new String[enemies.size()]);
                            String enemiesS = arrayToString(enemiesSA);
                            sendMsg(senderP, " &7Allies: &c"+alliesS);
                            sendMsg(senderP, " &7Enemies: &c"+enemiesS);
                        }
                    }
                }else if(args[0].equalsIgnoreCase("invite")) {
                    Set<String> uuids = getConfig().getConfigurationSection("users").getKeys(false);
                    if(args.length<2) {
                        sendMsg(senderP,prefix+"&4Invalid arguments. Usage: /tribe invite <player>"); 
                    }else if(args[1].equalsIgnoreCase(senderP.getName())){
                        sendMsg(senderP,prefix+"&4You cannot invite yourself!");
                    }else if(!inTribe(senderP)) {
                        sendMsg(senderP,prefix+"&4You are not in a tribe!");
                    }else if(!getConfig().getString("users."+senderID+".rank").equals("Chief")) {
                        sendMsg(senderP,prefix+"&4You must be the Chief of the tribe to do so!");
                    }else if(uuids.size()==getConfig().getInt("settings.members."+getTribeLevel(args[1]))) {
                        sendMsg(senderP,prefix+"&4Tribe is full! Rankup for more spaces.");
                    } else if(server.getPlayer(args[1])!=null){
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
                    } else if (!getConfig().contains("settings.prices."+getTribeLevel(getTribe(senderP)))){
                        sendMsg(senderP, prefix+"&4Tribe is at max rank!");
                    }else {
                        int level = getTribeLevel(getTribe(senderP));
                        double bal = getTribeBal(getTribe(senderP));
                        double req = getConfig().getInt("settings.prices."+level);
                        if(bal<req) {
                            sendMsg(senderP, prefix+"&4Tribe does not have enough money to rankup! Needed: &a$"+req);
                            sendMsg(senderP, prefix + "&7Tribe currently has &a$"
                                    + bal + "&7.");
                        }else {
                            sendMsg(senderP, prefix+"&7Tribe leveled up to level &e"+(level+1)+"&7!");
                            getConfig().set("tribes."+getTribe(senderP)+".level", level+1);
                            getConfig().set("tribes."+getTribe(senderP)+".balance", bal-req);
                            sendMsg(senderP, prefix + "&7Tribe currently has &a$"
                                    + bal + "&7.");
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
                }else if(args[0].equalsIgnoreCase("sword")) {
                    if(!inTribe(senderP)) {
                        sendMsg(senderP, prefix + "&4You are not in a tribe!");
                    }else if(senderP.getInventory().firstEmpty()==-1){
                        sendMsg(senderP, prefix + "&4Inventory full!");
                    }else {
                        ItemStack sword = new ItemStack(Material.GOLD_SWORD,1);
                        ItemMeta im = sword.getItemMeta();
                        im.setDisplayName(ChatColor.RED+""+ChatColor.BOLD+"Tribe Sword");
                        ArrayList<String> swordLore = new ArrayList<String>();
                        swordLore.add(ChatColor.GRAY+"Bonding X");
                        swordLore.add(ChatColor.RED+"A magical blade that releases powerful");
                        swordLore.add(ChatColor.RED+"magic upon usage through drawing the");
                        swordLore.add(ChatColor.RED+"power of present souls in the tribe.");
                        im.setLore(swordLore);
                        sword.addUnsafeEnchantment(Enchantment.DURABILITY,10);
                        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        sword.setItemMeta(im);
                        senderP.getInventory().addItem(sword);
                        sendMsg(senderP, prefix + "&7Received Tribe Sword.");
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
                            Double origBal = getTribeBal(getTribe(senderP));
                            if (origBal - money < 0) {
                                sendMsg(senderP, prefix + "&4Tribe does not have that much money! Tribe balance: &a$"
                                        + origBal);
                            } else {
                                double newBal = origBal - money;
                                getConfig().set("tribes." + getTribe(senderP) + ".balance", newBal);
                                saveConfig();
                                economy.depositPlayer(senderP, money);
                                sendMsg(senderP, prefix + "&7Withdrew &a$" + money + "&7 from tribe. New balance: &a$"
                                        + newBal + "&7.");
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
