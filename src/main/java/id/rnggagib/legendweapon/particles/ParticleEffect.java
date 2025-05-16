package id.rnggagib.legendweapon.particles;

import org.bukkit.Location;

public interface ParticleEffect {
    void play(Location location);
    String getName();
}