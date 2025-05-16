// filepath: d:\Plugin Nusa\legendweapon\src\main\java\id\rnggagib\legendweapon\commands\CommandManager.java
package id.rnggagib.legendweapon.commands;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.LegendaryWeapon;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final LegendWeapon plugin;
    
    public CommandManager(LegendWeapon plugin) {
        this.plugin = plugin;
    }
    
    public void registerCommands() {
        plugin.getCommand("legendweapon").setExecutor(this);
        plugin.getCommand("legendweapon").setTabCompleter(this);
        
        plugin.getCommand("lwgive").setExecutor(this);
        plugin.getCommand("lwgive").setTabCompleter(this);
        
        plugin.getCommand("lwreload").setExecutor(this);
        plugin.getCommand("lwlist").setExecutor(this);
        plugin.getCommand("lwlist").setTabCompleter(this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("legendweapon")) {
            return handleMainCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("lwgive")) {
            return handleGiveCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("lwreload")) {
            return handleReloadCommand(sender);
        } else if (command.getName().equalsIgnoreCase("lwlist")) {
            return handleListCommand(sender, args);
        }
        
        return false;
    }
    
    private boolean handleMainCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReloadCommand(sender);
            case "give":
                return handleGiveCommandAlias(sender, args);
            case "list":
                return handleListCommand(sender, args.length > 1 ? new String[]{args[1]} : new String[]{});
            case "help":
                showHelp(sender);
                return true;
            default:
                showHelp(sender);
                return true;
        }
    }
    
    private boolean handleGiveCommandAlias(CommandSender sender, String[] args) {
        if (args.length < 3) {
            plugin.getMessageManager().send(sender, "commands.help-format", 
                Map.of("command", "/lw give <player> <weapon> [level]", "description", "Give a legendary weapon to a player"));
            return true;
        }
        
        String[] newArgs = new String[args.length - 1];
        newArgs[0] = args[1]; // player name
        newArgs[1] = args[2]; // weapon id
        if (args.length > 3) {
            newArgs[2] = args[3]; // level (optional)
        }
        
        return handleGiveCommand(sender, newArgs);
    }
    
    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("legendweapon.give")) {
            plugin.getMessageManager().send(sender, "general.no-permission");
            return true;
        }
        
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "commands.help-format", 
                Map.of("command", "/lwgive <player> <weapon> [level]", "description", "Give a legendary weapon to a player"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getMessageManager().send(sender, "weapons.invalid-player", Map.of("player", args[0]));
            return true;
        }
        
        String weaponId = args[1];
        LegendaryWeapon weapon = plugin.getWeaponManager().getWeaponById(weaponId);
        
        if (weapon == null) {
            plugin.getMessageManager().send(sender, "weapons.invalid-weapon", Map.of("weapon", weaponId));
            return true;
        }
        
        int level = 1;
        if (args.length > 2) {
            try {
                level = Integer.parseInt(args[2]);
                if (level < 1) level = 1;
            } catch (NumberFormatException e) {
                plugin.getMessageManager().send(sender, "commands.help-format", 
                    Map.of("command", "/lwgive <player> <weapon> [level]", "description", "Give a legendary weapon to a player"));
                return true;
            }
        }
        
        plugin.getWeaponManager().giveWeapon(target, weaponId, level);
        
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        placeholders.put("weapon", weapon.getName());
        placeholders.put("level", String.valueOf(level));
        
        plugin.getMessageManager().send(sender, "weapons.give-success", placeholders);
        
        return true;
    }
    
    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("legendweapon.reload")) {
            plugin.getMessageManager().send(sender, "general.no-permission");
            return true;
        }
        
        plugin.reload();
        plugin.getMessageManager().send(sender, "general.plugin-reload");
        return true;
    }
    
    private boolean handleListCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("legendweapon.admin")) {
            plugin.getMessageManager().send(sender, "general.no-permission");
            return true;
        }
        
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException ignored) {}
        }
        
        Map<String, LegendaryWeapon> weapons = plugin.getWeaponManager().getAllWeapons();
        List<String> weaponList = new ArrayList<>(weapons.keySet());
        
        int totalWeapons = weaponList.size();
        int weaponsPerPage = 8;
        int totalPages = (int) Math.ceil(totalWeapons / (double) weaponsPerPage);
        
        if (page > totalPages) page = totalPages;
        
        plugin.getMessageManager().send(sender, "commands.help-header");
        plugin.getMessageManager().send(sender, "ui.page-navigation", Map.of("current", String.valueOf(page), "total", String.valueOf(totalPages)));
        
        int start = (page - 1) * weaponsPerPage;
        int end = Math.min(start + weaponsPerPage, totalWeapons);
        
        for (int i = start; i < end; i++) {
            String weaponId = weaponList.get(i);
            LegendaryWeapon weapon = weapons.get(weaponId);
            
            plugin.getMessageManager().send(sender, "commands.help-format", 
                Map.of("command", weaponId, "description", weapon.getName() + " (" + weapon.getRarity() + ")"));
        }
        
        if (page < totalPages) {
            plugin.getMessageManager().send(sender, "ui.next-page");
        }
        
        if (page > 1) {
            plugin.getMessageManager().send(sender, "ui.previous-page");
        }
        
        plugin.getMessageManager().send(sender, "commands.help-footer");
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        plugin.getMessageManager().send(sender, "commands.help-header");
        
        plugin.getMessageManager().send(sender, "commands.help-format", 
            Map.of("command", "/lw give <player> <weapon> [level]", "description", "Give a legendary weapon to a player"));
        plugin.getMessageManager().send(sender, "commands.help-format", 
            Map.of("command", "/lw list [page]", "description", "List all available weapons"));
        plugin.getMessageManager().send(sender, "commands.help-format", 
            Map.of("command", "/lw reload", "description", "Reload the plugin configuration"));
        plugin.getMessageManager().send(sender, "commands.help-format", 
            Map.of("command", "/lw help", "description", "Show this help message"));
        
        plugin.getMessageManager().send(sender, "commands.help-footer");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (command.getName().equalsIgnoreCase("legendweapon")) {
            if (args.length == 1) {
                completions.add("give");
                completions.add("list");
                completions.add("reload");
                completions.add("help");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
                completions.addAll(plugin.getWeaponManager().getAllWeapons().keySet());
            }
        } else if (command.getName().equalsIgnoreCase("lwgive")) {
            if (args.length == 1) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            } else if (args.length == 2) {
                completions.addAll(plugin.getWeaponManager().getAllWeapons().keySet());
            }
        }
        
        return completions;
    }
}