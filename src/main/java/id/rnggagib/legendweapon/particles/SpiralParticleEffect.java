package id.rnggagib.legendweapon.particles;

import id.rnggagib.LegendWeapon;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class SpiralParticleEffect extends AbstractParticleEffect {
    private final double radius;
    private final double heightStep;
    private final int iterations;
    private final double angleStep;
    
    public SpiralParticleEffect(LegendWeapon plugin, String name, Particle particleType, 
                               int count, double speed, Sound sound, float volume, float pitch,
                               double radius, double heightStep, int iterations) {
        super(plugin, name, particleType, count, speed, sound, volume, pitch);
        this.radius = radius;
        this.heightStep = heightStep;
        this.iterations = iterations;
        this.angleStep = Math.PI / 8;
    }
    
    @Override
    public void play(Location location) {
        playSound(location);
        
        new BukkitRunnable() {
            double angle = 0;
            int step = 0;
            
            @Override
            public void run() {
                if (step > iterations) {
                    this.cancel();
                    return;
                }
                
                double[] point = getCirclePoint(radius, angle);
                double x = point[0];
                double z = point[1];
                double y = heightStep * step;
                
                Location particleLocation = location.clone().add(x, y, z);
                location.getWorld().spawnParticle(
                    particleType, 
                    particleLocation, 
                    count, 
                    0.05, 0.05, 0.05, 
                    speed
                );
                
                angle += angleStep;
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}