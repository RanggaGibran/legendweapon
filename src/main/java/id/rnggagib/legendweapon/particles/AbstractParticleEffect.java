package id.rnggagib.legendweapon.particles;

import id.rnggagib.LegendWeapon;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class AbstractParticleEffect implements ParticleEffect {
    protected final LegendWeapon plugin;
    protected final String name;
    protected final Particle particleType;
    protected final int count;
    protected final double speed;
    protected final Sound sound;
    protected final float volume;
    protected final float pitch;
    
    public AbstractParticleEffect(LegendWeapon plugin, String name, Particle particleType, 
                                 int count, double speed, Sound sound, float volume, float pitch) {
        this.plugin = plugin;
        this.name = name;
        this.particleType = particleType;
        this.count = count;
        this.speed = speed;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public abstract void play(Location location);
    
    protected void playSound(Location location) {
        if (sound != null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }
    
    protected double[] getCirclePoint(double radius, double angle) {
        double x = radius * Math.cos(angle);
        double z = radius * Math.sin(angle);
        return new double[]{x, z};
    }
}