package id.rnggagib.legendweapon.combat;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.LegendaryWeapon;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParrySystem {
    private final LegendWeapon plugin;
    private final Map<UUID, Long> parryTimers;
    private final Map<UUID, Long> parrySuccessTimers;
    private final long parryWindow = 500; // 0.5 seconds to parry
    private final long parrySuccessDuration = 2000; // 2 seconds of parry success boost
    
    public ParrySystem(LegendWeapon plugin) {
        this.plugin = plugin;
        this.parryTimers = new HashMap<>();
        this.parrySuccessTimers = new HashMap<>();
    }
    
    public void attemptParry(Player player) {
        parryTimers.put(player.getUniqueId(), System.currentTimeMillis());
        
        Location location = player.getLocation().add(0, 1, 0);
        location.getWorld().spawnParticle(
            Particle.CRIT,
            location,
            15,
            0.3, 0.3, 0.3,
            0.1
        );
        
        location.getWorld().playSound(location, Sound.ITEM_SHIELD_BLOCK, 0.5f, 2.0f);
    }
    
    public boolean isParrying(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!parryTimers.containsKey(playerId)) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long parryTime = parryTimers.get(playerId);
        
        return currentTime - parryTime <= parryWindow;
    }
    
    public void registerSuccessfulParry(Player player, Entity attacker) {
        UUID playerId = player.getUniqueId();
        parrySuccessTimers.put(playerId, System.currentTimeMillis());
        
        Location location = player.getLocation().add(0, 1, 0);
        location.getWorld().spawnParticle(
            Particle.FLASH,
            location,
            3,
            0.3, 0.3, 0.3,
            0
        );
        
        location.getWorld().playSound(location, Sound.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
        
        if (attacker instanceof LivingEntity) {
            ((LivingEntity) attacker).setNoDamageTicks(30); // Stagger the attacker for 1.5 seconds
            attacker.setVelocity(attacker.getLocation().getDirection().multiply(-1).setY(0.5));
            
            Location attackerLoc = attacker.getLocation().add(0, 1, 0);
            attackerLoc.getWorld().spawnParticle(
                Particle.SONIC_BOOM,
                attackerLoc,
                1,
                0, 0, 0,
                0
            );
        }
        
        player.sendMessage(plugin.getMessageManager().getMessage("combat.parry-success", new HashMap<>()));
    }
    
    public boolean hasParryBoost(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!parrySuccessTimers.containsKey(playerId)) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long successTime = parrySuccessTimers.get(playerId);
        
        return currentTime - successTime <= parrySuccessDuration;
    }
    
    public double getParryDamageMultiplier() {
        return 1.5; // 50% damage increase after successful parry
    }
    
    public void clearExpiredParries() {
        long currentTime = System.currentTimeMillis();
        
        parryTimers.entrySet().removeIf(entry -> currentTime - entry.getValue() > parryWindow);
        parrySuccessTimers.entrySet().removeIf(entry -> currentTime - entry.getValue() > parrySuccessDuration);
    }
}