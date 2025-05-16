package id.rnggagib.legendweapon.particles;

import id.rnggagib.LegendWeapon;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class CircleParticleEffect extends AbstractParticleEffect {
    private final double radius;
    private final double heightOffset;
    private final int points;
    
    public CircleParticleEffect(LegendWeapon plugin, String name, Particle particleType,
                               int count, double speed, Sound sound, float volume, float pitch,
                               double radius, double heightOffset, int points) {
        super(plugin, name, particleType, count, speed, sound, volume, pitch);
        this.radius = radius;
        this.heightOffset = heightOffset;
        this.points = points;
    }
    
    @Override
    public void play(Location location) {
        playSound(location);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                drawCircle(location);
                this.cancel();
            }
        }.runTaskLater(plugin, 0L);
    }
    
    private void drawCircle(Location location) {
        double angleStep = 2 * Math.PI / points;
        
        for (int i = 0; i < points; i++) {
            double angle = i * angleStep;
            double[] point = getCirclePoint(radius, angle);
            double x = point[0];
            double z = point[1];
            
            Location particleLocation = location.clone().add(x, heightOffset, z);
            location.getWorld().spawnParticle(
                particleType,
                particleLocation,
                count,
                0.05, 0.05, 0.05,
                speed
            );
        }
    }
}