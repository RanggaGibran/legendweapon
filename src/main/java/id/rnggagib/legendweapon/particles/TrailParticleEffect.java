package id.rnggagib.legendweapon.particles;

import id.rnggagib.LegendWeapon;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class TrailParticleEffect extends AbstractParticleEffect {
    private final double length;
    private final int steps;
    private final Vector direction;
    
    public TrailParticleEffect(LegendWeapon plugin, String name, Particle particleType,
                              int count, double speed, Sound sound, float volume, float pitch,
                              double length, int steps, Vector direction) {
        super(plugin, name, particleType, count, speed, sound, volume, pitch);
        this.length = length;
        this.steps = steps;
        this.direction = direction != null ? direction.normalize() : new Vector(1, 0, 0);
    }
    
    @Override
    public void play(Location location) {
        playSound(location);
        
        new BukkitRunnable() {
            int step = 0;
            
            @Override
            public void run() {
                if (step >= steps) {
                    this.cancel();
                    return;
                }
                
                double distance = (length / steps) * step;
                Vector offset = direction.clone().multiply(distance);
                Location particleLocation = location.clone().add(offset);
                
                location.getWorld().spawnParticle(
                    particleType,
                    particleLocation,
                    count,
                    0.05, 0.05, 0.05,
                    speed
                );
                
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}