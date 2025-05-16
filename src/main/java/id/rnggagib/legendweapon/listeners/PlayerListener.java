package id.rnggagib.legendweapon.listeners;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.LegendaryWeapon;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerListener implements Listener {
    private final LegendWeapon plugin;
    private final Map<UUID, Long> lastSwapTime;
    private static final long SWAP_COOLDOWN = 500; // 0.5 seconds
    
    public PlayerListener(LegendWeapon plugin) {
        this.plugin = plugin;
        this.lastSwapTime = new ConcurrentHashMap<>();
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Load player data if needed
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Save player data if needed
        
        UUID playerId = event.getPlayer().getUniqueId();
        lastSwapTime.remove(playerId);
    }
    
    @EventHandler
    public void onPlayerSwapItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = event.getMainHandItem();
        
        if (mainHand == null || !plugin.getWeaponManager().isLegendaryWeapon(mainHand)) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        if (lastSwapTime.containsKey(playerId)) {
            long lastTime = lastSwapTime.get(playerId);
            
            if (currentTime - lastTime < SWAP_COOLDOWN) {
                // Double swap detected
                event.setCancelled(true);
                
                LegendaryWeapon weapon = plugin.getWeaponManager().getWeaponFromItem(mainHand);
                if (weapon != null) {
                    toggleWeaponMode(player, weapon);
                }
            }
        }
        
        lastSwapTime.put(playerId, currentTime);
    }
    
    private void toggleWeaponMode(Player player, LegendaryWeapon weapon) {
        // This could toggle between different modes of the weapon
        // For example, a bow could switch between different arrow types
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("weapon", weapon.getName());
        
        plugin.getMessageManager().send(player, "weapons.mode-changed", placeholders);
    }
}