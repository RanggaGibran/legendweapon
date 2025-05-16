package id.rnggagib.legendweapon.models.abilities.actives;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.abilities.ActiveAbility;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

public class FlameBurstAbility extends ActiveAbility {
    
    public FlameBurstAbility(LegendWeapon plugin, String id, ConfigurationSection config) {
        super(plugin, id, config);
    }
    
    @Override
    protected boolean executeAbility(Player player, Location location) {
        int range = getRange();
        double damage = getDamage();
        
        for (Entity entity : location.getWorld().getNearbyEntities(location, range, range, range)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(damage, player);
                target.setFireTicks(getIntProperty("fire-duration", 100));
            }
        }
        
        return true;
    }
}