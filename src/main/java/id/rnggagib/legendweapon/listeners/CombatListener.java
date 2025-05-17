package id.rnggagib.legendweapon.listeners;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.combat.ComboManager;
import id.rnggagib.legendweapon.combat.ParrySystem;
import id.rnggagib.legendweapon.models.LegendaryWeapon;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CombatListener implements Listener {
    private final LegendWeapon plugin;
    private final ComboManager comboManager;
    private final ParrySystem parrySystem;
    private final Map<String, Long> weaponCooldowns;
    private final long parryWindowCooldown = 200; // 0.2s between parry attempts
    
    public CombatListener(LegendWeapon plugin, ComboManager comboManager, ParrySystem parrySystem) {
        this.plugin = plugin;
        this.comboManager = comboManager;
        this.parrySystem = parrySystem;
        this.weaponCooldowns = new HashMap<>();
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) return;
        if (!plugin.getWeaponManager().isLegendaryWeapon(item)) return;
        
        LegendaryWeapon weapon = plugin.getWeaponManager().getWeaponFromItem(item);
        if (weapon == null) return;
        
        // Handle right-click (block) actions for parrying
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                // This is for abilities, handled elsewhere
                return;
            }
            
            // Basic parry attempt
            String weaponId = weapon.getId();
            long currentTime = System.currentTimeMillis();
            
            if (weaponCooldowns.containsKey(weaponId) && 
                currentTime - weaponCooldowns.get(weaponId) < parryWindowCooldown) {
                return;
            }
            
            weaponCooldowns.put(weaponId, currentTime);
            parrySystem.attemptParry(player);
        }
        
        // Record the attack for combo
        boolean isLeftClick = 
            event.getAction() == Action.LEFT_CLICK_AIR || 
            event.getAction() == Action.LEFT_CLICK_BLOCK;
            
        comboManager.registerClick(player, weapon, isLeftClick);
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Handle incoming damage for parry
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ItemStack item = player.getInventory().getItemInMainHand();
            
            if (plugin.getWeaponManager().isLegendaryWeapon(item)) {
                if (parrySystem.isParrying(player)) {
                    // Successful parry
                    event.setCancelled(true);
                    parrySystem.registerSuccessfulParry(player, event.getDamager());
                    return;
                }
            }
        }
        
        // Handle outgoing damage for combos
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getInventory().getItemInMainHand();
            
            if (plugin.getWeaponManager().isLegendaryWeapon(item)) {
                // Apply combo damage multiplier
                double comboMultiplier = comboManager.getComboMultiplier(player);
                
                // Apply parry damage boost if active
                if (parrySystem.hasParryBoost(player)) {
                    comboMultiplier *= parrySystem.getParryDamageMultiplier();
                    
                    Location loc = event.getEntity().getLocation().add(0, 1, 0);
                    loc.getWorld().spawnParticle(
                        Particle.FLASH,
                        loc,
                        1,
                        0, 0, 0,
                        0
                    );
                    
                    loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.0f);
                }
                
                if (comboMultiplier > 1.0) {
                    event.setDamage(event.getDamage() * comboMultiplier);
                    
                    if (event.getEntity() instanceof LivingEntity) {
                        Location loc = event.getEntity().getLocation().add(0, 1, 0);
                        loc.getWorld().spawnParticle(
                            Particle.CRIT,
                            loc,
                            10,
                            0.3, 0.3, 0.3,
                            0.1
                        );
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        comboManager.clearPlayer(player.getUniqueId());
    }
}