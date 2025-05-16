package id.rnggagib.legendweapon.models.abilities.actives;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.abilities.ActiveAbility;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class IceSpikeAbility extends ActiveAbility {
    
    public IceSpikeAbility(LegendWeapon plugin, String id, ConfigurationSection config) {
        super(plugin, id, config);
    }
    
    @Override
    protected boolean executeAbility(Player player, Location location) {
        int range = getRange();
        double damage = getDamage();
        int slowLevel = getIntProperty("slow-level", 2);
        int slowDuration = getIntProperty("slow-duration", 100);
        
        for (Entity entity : location.getWorld().getNearbyEntities(location, range, range, range)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(damage, player);
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slowDuration, slowLevel - 1));
            }
        }
        
        return true;
    }
}