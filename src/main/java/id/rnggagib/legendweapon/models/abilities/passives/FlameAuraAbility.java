package id.rnggagib.legendweapon.models.abilities.passives;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.abilities.PassiveAbility;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

public class FlameAuraAbility extends PassiveAbility {
    
    public FlameAuraAbility(LegendWeapon plugin, String id, ConfigurationSection config) {
        super(plugin, id, config);
    }
    
    @Override
    protected void applyEffect(Player player, Location location) {
        super.applyEffect(player, location);
        
        int range = getRange();
        double tickDamage = getDoubleProperty("tick-damage", 1.0);
        int fireDuration = getIntProperty("fire-duration", 40);
        
        for (Entity entity : location.getWorld().getNearbyEntities(location, range, range, range)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(tickDamage, player);
                
                if (!target.isDead() && target.getFireTicks() < fireDuration) {
                    target.setFireTicks(fireDuration);
                }
            }
        }
    }
}