package id.rnggagib.legendweapon.particles;

import id.rnggagib.LegendWeapon;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class BurstParticleEffect extends AbstractParticleEffect {
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    
    public BurstParticleEffect(LegendWeapon plugin, String name, Particle particleType,
                              int count, double speed, Sound sound, float volume, float pitch,
                              double offsetX, double offsetY, double offsetZ) {
        super(plugin, name, particleType, count, speed, sound, volume, pitch);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }
    
    @Override
    public void play(Location location) {
        playSound(location);
        location.getWorld().spawnParticle(
            particleType,
            location,
            count,
            offsetX, offsetY, offsetZ,
            speed
        );
    }
}