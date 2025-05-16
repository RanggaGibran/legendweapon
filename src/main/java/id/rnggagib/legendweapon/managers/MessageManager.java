package id.rnggagib.legendweapon.managers;

import id.rnggagib.LegendWeapon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final LegendWeapon plugin;
    private final MiniMessage miniMessage;
    
    public MessageManager(LegendWeapon plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    public void send(CommandSender sender, String path) {
        send(sender, path, new HashMap<>());
    }
    
    public void send(CommandSender sender, String path, Map<String, String> placeholders) {
        String message = getMessage(path, placeholders);
        if (message.isEmpty()) return;
        
        if (sender instanceof Player player) {
            Audience audience = plugin.adventure().player(player);
            audience.sendMessage(miniMessage.deserialize(message));
        } else {
            sender.sendMessage(stripColors(message));
        }
    }
    
    public void sendConsole(String path) {
        sendConsole(path, new HashMap<>());
    }
    
    public void sendConsole(String path, Map<String, String> placeholders) {
        String message = getMessage(path, placeholders);
        if (message.isEmpty()) return;
        
        plugin.getServer().getConsoleSender().sendMessage(stripColors(message));
    }
    
    public void broadcast(String path) {
        broadcast(path, new HashMap<>());
    }
    
    public void broadcast(String path, Map<String, String> placeholders) {
        String message = getMessage(path, placeholders);
        if (message.isEmpty()) return;
        
        plugin.adventure().all().sendMessage(miniMessage.deserialize(message));
    }
    
    public String getMessage(String path, Map<String, String> placeholders) {
        String prefix = plugin.getConfigManager().getMessages().getString("prefix", "");
        String message = plugin.getConfigManager().getMessages().getString(path, "");
        
        if (message.isEmpty()) {
            return "";
        }
        
        message = prefix + " " + message;
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        if (plugin.isPlaceholderAPIEnabled() && message.contains("%")) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                message = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, message);
                break;
            }
        }
        
        return message;
    }
    
    public Component getComponent(String path) {
        return getComponent(path, new HashMap<>());
    }
    
    public Component getComponent(String path, Map<String, String> placeholders) {
        String message = getMessage(path, placeholders);
        if (message.isEmpty()) {
            return Component.empty();
        }
        return miniMessage.deserialize(message);
    }
    
    private String stripColors(String message) {
        return message.replaceAll("<[^>]*>", "");
    }
}