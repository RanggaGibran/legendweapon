package id.rnggagib.legendweapon.listeners;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.LegendaryWeapon;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProgressionListener implements Listener {
    private final LegendWeapon plugin;
    private final HashMap<UUID, HashMap<UUID, Integer>> damageTracker;
    
    public ProgressionListener(LegendWeapon plugin) {
        this.plugin = plugin;
        this.damageTracker = new HashMap<>();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        
        Player player = (Player) event.getDamager();
        LivingEntity target = (LivingEntity) event.getEntity();
        
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon.getType() == Material.AIR) return;
        
        if (plugin.getWeaponManager().isLegendaryWeapon(weapon)) {
            UUID targetId = target.getUniqueId();
            UUID playerId = player.getUniqueId();
            
            if (!damageTracker.containsKey(targetId)) {
                damageTracker.put(targetId, new HashMap<>());
            }
            
            int currentDamage = damageTracker.get(targetId).getOrDefault(playerId, 0);
            int newDamage = currentDamage + (int) event.getFinalDamage();
            
            damageTracker.get(targetId).put(playerId, newDamage);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return;
        
        UUID entityId = entity.getUniqueId();
        
        if (damageTracker.containsKey(entityId)) {
            HashMap<UUID, Integer> damageMap = damageTracker.get(entityId);
            
            int totalExp = calculateBaseExp(entity);
            
            for (UUID playerId : damageMap.keySet()) {
                Player player = plugin.getServer().getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    ItemStack weapon = player.getInventory().getItemInMainHand();
                    
                    if (plugin.getWeaponManager().isLegendaryWeapon(weapon)) {
                        int damage = damageMap.get(playerId);
                        int totalDamage = (int) entity.getMaxHealth();
                        
                        double percentage = Math.min(1.0, (double) damage / totalDamage);
                        int exp = (int) (totalExp * percentage);
                        
                        plugin.getWeaponProgressionManager().gainExperienceFromKill(player, weapon, exp);
                    }
                }
            }
            
            damageTracker.remove(entityId);
        }
    }
    
    private int calculateBaseExp(LivingEntity entity) {
        double health = entity.getMaxHealth();
        
        if (health <= 10) {
            return 5;
        } else if (health <= 20) {
            return 10;
        } else if (health <= 50) {
            return 25;
        } else if (health <= 100) {
            return 50;
        } else {
            return 100;
        }
    }
}