package id.rnggagib.legendweapon.combat;

import org.bukkit.Particle;
import org.bukkit.Sound;

public class ComboAction {
    private final String name;
    private final double damageMultiplier;
    private final Particle particle;
    private final Sound sound;
    
    public ComboAction(String name, double damageMultiplier, Particle particle, Sound sound) {
        this.name = name;
        this.damageMultiplier = damageMultiplier;
        this.particle = particle;
        this.sound = sound;
    }
    
    public String getName() {
        return name;
    }
    
    public double getDamageMultiplier() {
        return damageMultiplier;
    }
    
    public Particle getParticle() {
        return particle;
    }
    
    public Sound getSound() {
        return sound;
    }
}