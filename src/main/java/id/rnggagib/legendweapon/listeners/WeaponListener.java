package id.rnggagib.legendweapon.listeners;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.LegendaryWeapon;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WeaponListener implements Listener {
    private final LegendWeapon plugin;
    private final Map<UUID, Map<String, Long>> cooldowns;
    
    public WeaponListener(LegendWeapon plugin) {
        this.plugin = plugin;
        this.cooldowns = new ConcurrentHashMap<>();
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (!plugin.getWeaponManager().isLegendaryWeapon(item)) {
            return;
        }
        
        LegendaryWeapon weapon = plugin.getWeaponManager().getWeaponFromItem(item);
        if (weapon == null) return;
        
        if (!player.hasPermission("legendweapon.use")) {
            plugin.getMessageManager().send(player, "weapons.cannot-use");
            event.setCancelled(true);
            return;
        }
        
        Map<String, Double> attributes = weapon.getAttributes();
        
        if (attributes.containsKey("damage")) {
            double damage = attributes.get("damage");
            event.setDamage(damage);
        }
        
        if (attributes.containsKey("critical-chance") && attributes.containsKey("critical-damage")) {
            double critChance = attributes.get("critical-chance");
            double critDamage = attributes.get("critical-damage") / 100.0; // Convert from percentage
            
            if (Math.random() * 100 <= critChance) {
                event.setDamage(event.getDamage() * (1 + critDamage));
                
                playHitAnimation(event.getEntity().getLocation(), weapon, true);
            } else {
                playHitAnimation(event.getEntity().getLocation(), weapon, false);
            }
        } else {
            playHitAnimation(event.getEntity().getLocation(), weapon, false);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !plugin.getWeaponManager().isLegendaryWeapon(item)) {
            return;
        }
        
        if (!player.hasPermission("legendweapon.use")) {
            plugin.getMessageManager().send(player, "weapons.cannot-use");
            return;
        }
        
        LegendaryWeapon weapon = plugin.getWeaponManager().getWeaponFromItem(item);
        if (weapon == null) return;
        
        if (event.getAction().name().contains("RIGHT_CLICK") && player.isSneaking()) {
            activateAbility(player, weapon);
        }
    }
    
    private void activateAbility(Player player, LegendaryWeapon weapon) {
        UUID playerId = player.getUniqueId();
        
        if (!cooldowns.containsKey(playerId)) {
            cooldowns.put(playerId, new HashMap<>());
        }
        
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        String weaponId = weapon.getId();
        
        if (playerCooldowns.containsKey(weaponId)) {
            long timeLeft = (playerCooldowns.get(weaponId) - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                plugin.getMessageManager().send(player, "abilities.on-cooldown", Map.of("time", String.valueOf(timeLeft)));
                return;
            }
        }
        
        Map<String, Double> attributes = weapon.getAttributes();
        if (!attributes.containsKey("ability-cooldown")) {
            return;
        }
        
        double cooldownTime = attributes.getOrDefault("ability-cooldown", 30.0);
        playerCooldowns.put(weaponId, System.currentTimeMillis() + (long)(cooldownTime * 1000));
        
        playAbilityAnimation(player.getLocation(), weapon);
        
        plugin.getMessageManager().send(player, "abilities.activated", Map.of("ability", weapon.getName() + " Special"));
    }
    
    private void playHitAnimation(org.bukkit.Location location, LegendaryWeapon weapon, boolean critical) {
        String weaponId = weapon.getId();
        
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getParticleManager().playEffect(weaponId + "_hit", location);
            
            if (critical) {
                plugin.getParticleManager().playEffect(weaponId + "_critical", location);
            }
        });
    }
    
    private void playAbilityAnimation(org.bukkit.Location location, LegendaryWeapon weapon) {
        String weaponId = weapon.getId();
        
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getParticleManager().playEffect(weaponId + "_ability", location);
        });
    }
}