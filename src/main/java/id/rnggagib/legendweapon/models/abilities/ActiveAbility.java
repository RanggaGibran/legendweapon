package id.rnggagib.legendweapon.models.abilities;

import id.rnggagib.LegendWeapon;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActiveAbility extends AbstractAbility {
    private final Map<UUID, Long> lastUsed;
    private final int range;
    private final double damage;
    
    public ActiveAbility(LegendWeapon plugin, String id, String name, String description, long cooldown, int range, double damage) {
        super(plugin, id, name, description, cooldown, false);
        this.lastUsed = new HashMap<>();
        this.range = range;
        this.damage = damage;
    }
    
    public ActiveAbility(LegendWeapon plugin, String id, ConfigurationSection config) {
        super(plugin, id, config, false);
        this.lastUsed = new HashMap<>();
        this.range = config.getInt("range", 5);
        this.damage = config.getDouble("damage", 0);
    }
    
    @Override
    public boolean activate(Player player, Location location) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        if (lastUsed.containsKey(playerId)) {
            long timeElapsed = currentTime - lastUsed.get(playerId);
            if (timeElapsed < cooldown) {
                long remainingCooldown = (cooldown - timeElapsed) / 1000;
                plugin.getMessageManager().send(player, "abilities.on-cooldown", Map.of("time", String.valueOf(remainingCooldown)));
                return false;
            }
        }
        
        if (executeAbility(player, location)) {
            lastUsed.put(playerId, currentTime);
            
            String effectId = id.toLowerCase().replace("_", "-") + "_ability";
            playEffect(player, location, effectId);
            
            plugin.getMessageManager().send(player, "abilities.activated", Map.of("ability", name));
            return true;
        }
        
        return false;
    }
    
    protected boolean executeAbility(Player player, Location location) {
        return true;
    }
    
    protected void playEffect(Player player, Location location, String effectId) {
        if (plugin.getParticleManager().hasEffect(effectId)) {
            plugin.getParticleManager().playEffect(effectId, location);
        }
    }
    
    public boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!lastUsed.containsKey(playerId)) {
            return false;
        }
        
        long timeElapsed = System.currentTimeMillis() - lastUsed.get(playerId);
        return timeElapsed < cooldown;
    }
    
    public long getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!lastUsed.containsKey(playerId)) {
            return 0;
        }
        
        long timeElapsed = System.currentTimeMillis() - lastUsed.get(playerId);
        return Math.max(0, (cooldown - timeElapsed) / 1000);
    }
    
    public int getRange() {
        return range;
    }
    
    public double getDamage() {
        return damage;
    }
}