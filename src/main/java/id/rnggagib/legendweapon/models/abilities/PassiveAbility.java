package id.rnggagib.legendweapon.models.abilities;

import id.rnggagib.LegendWeapon;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PassiveAbility extends AbstractAbility {
    private final Map<UUID, BukkitRunnable> activeEffects;
    private final int range;
    private final int duration;
    private final int tickRate;
    
    public PassiveAbility(LegendWeapon plugin, String id, String name, String description, long cooldown, int range, int duration, int tickRate) {
        super(plugin, id, name, description, cooldown, true);
        this.activeEffects = new HashMap<>();
        this.range = range;
        this.duration = duration;
        this.tickRate = tickRate;
    }
    
    public PassiveAbility(LegendWeapon plugin, String id, ConfigurationSection config) {
        super(plugin, id, config, true);
        this.activeEffects = new HashMap<>();
        this.range = config.getInt("range", 3);
        this.duration = config.getInt("duration", 5);
        this.tickRate = config.getInt("tick-rate", 20);
    }

    @Override
    public boolean activate(Player player, Location location) {
        UUID playerId = player.getUniqueId();
        
        if (activeEffects.containsKey(playerId)) {
            activeEffects.get(playerId).cancel();
            activeEffects.remove(playerId);
        }
        
        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;
            int maxTicks = duration * 20;
            
            @Override
            public void run() {
                if (ticks >= maxTicks || !player.isOnline()) {
                    this.cancel();
                    activeEffects.remove(playerId);
                    return;
                }
                
                if (ticks % tickRate == 0) {
                    applyEffect(player, player.getLocation());
                }
                
                ticks++;
            }
        };
        
        task.runTaskTimer(plugin, 0L, 1L);
        activeEffects.put(playerId, task);
        
        return true;
    }
    
    protected void applyEffect(Player player, Location location) {
        String effectId = id.toLowerCase().replace("_", "-") + "_passive";
        if (plugin.getParticleManager().hasEffect(effectId)) {
            plugin.getParticleManager().playEffect(effectId, location);
        }
    }
    
    public void stopEffect(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (activeEffects.containsKey(playerId)) {
            activeEffects.get(playerId).cancel();
            activeEffects.remove(playerId);
        }
    }
    
    public boolean hasActiveEffect(Player player) {
        return activeEffects.containsKey(player.getUniqueId());
    }
    
    public int getRange() {
        return range;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public int getTickRate() {
        return tickRate;
    }
}