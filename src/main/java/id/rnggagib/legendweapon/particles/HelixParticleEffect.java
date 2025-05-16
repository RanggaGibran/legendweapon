package id.rnggagib.legendweapon.particles;

import id.rnggagib.LegendWeapon;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class HelixParticleEffect extends AbstractParticleEffect {
    private final double radius;
    private final double height;
    private final int helices;
    private final int pointsPerHelix;
    
    public HelixParticleEffect(LegendWeapon plugin, String name, Particle particleType,
                              int count, double speed, Sound sound, float volume, float pitch,
                              double radius, double height, int helices, int pointsPerHelix) {
        super(plugin, name, particleType, count, speed, sound, volume, pitch);
        this.radius = radius;
        this.height = height;
        this.helices = helices;
        this.pointsPerHelix = pointsPerHelix;
    }
    
    @Override
    public void play(Location location) {
        playSound(location);
        
        new BukkitRunnable() {
            int tick = 0;
            final int totalTicks = pointsPerHelix;
            
            @Override
            public void run() {
                if (tick >= totalTicks) {
                    this.cancel();
                    return;
                }
                
                double angleStep = 2 * Math.PI / pointsPerHelix;
                double heightStep = height / pointsPerHelix;
                
                for (int i = 0; i < helices; i++) {
                    double angle = (tick * angleStep) + ((2 * Math.PI * i) / helices);
                    double[] point = getCirclePoint(radius, angle);
                    double x = point[0];
                    double z = point[1];
                    double y = heightStep * tick;
                    
                    Location particleLocation = location.clone().add(x, y, z);
                    location.getWorld().spawnParticle(
                        particleType,
                        particleLocation,
                        count,
                        0, 0, 0,
                        speed
                    );
                }
                
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}