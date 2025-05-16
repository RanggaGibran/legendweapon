package id.rnggagib.legendweapon.models.abilities.actives;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.abilities.ActiveAbility;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

public class VoidBurstAbility extends ActiveAbility {
    
    public VoidBurstAbility(LegendWeapon plugin, String id, ConfigurationSection config) {
        super(plugin, id, config);
    }
    
    @Override
    protected boolean executeAbility(Player player, Location location) {
        int range = getRange();
        double damage = getDamage();
        int teleportDistance = getIntProperty("teleport-distance", 5);
        
        Vector direction = player.getLocation().getDirection().normalize().multiply(teleportDistance);
        Location targetLocation = player.getLocation().add(direction);
        
        targetLocation.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.teleport(targetLocation);
        targetLocation.getWorld().playSound(targetLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        
        for (Entity entity : targetLocation.getWorld().getNearbyEntities(targetLocation, range, range, range)) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(damage, player);
                
                Vector knockbackDirection = target.getLocation().subtract(targetLocation).toVector().normalize().multiply(2);
                target.setVelocity(target.getVelocity().add(knockbackDirection));
            }
        }
        
        return true;
    }
}