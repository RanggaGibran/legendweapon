package id.rnggagib.legendweapon.models.abilities.passives;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.abilities.PassiveAbility;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FrostAuraAbility extends PassiveAbility {
    
    public FrostAuraAbility(LegendWeapon plugin, String id, ConfigurationSection config) {
        super(plugin, id, config);
    }
    
    @Override
    protected void applyEffect(Player player, Location location) {
        super.applyEffect(player, location);
        
        int range = getRange();
        int slowLevel = getIntProperty("slow-level", 1);
        int slowDuration = getIntProperty("slow-duration", 60);
        
        for (Entity entity : location.getWorld().getNearbyEntities(location, range, range, range)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, slowDuration, slowLevel - 1));
            }
        }
    }
}