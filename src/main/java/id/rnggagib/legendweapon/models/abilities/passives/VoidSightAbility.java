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

public class VoidSightAbility extends PassiveAbility {
    
    public VoidSightAbility(LegendWeapon plugin, String id, ConfigurationSection config) {
        super(plugin, id, config);
    }
    
    @Override
    protected void applyEffect(Player player, Location location) {
        super.applyEffect(player, location);
        
        int glowDuration = getIntProperty("glow-duration", 60);
        int range = getRange();
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, glowDuration, 0));
        
        for (Entity entity : location.getWorld().getNearbyEntities(location, range, range, range)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, glowDuration, 0));
            }
        }
    }
}