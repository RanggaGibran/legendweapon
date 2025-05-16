package id.rnggagib.legendweapon.particles;

import id.rnggagib.LegendWeapon;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class MultiParticleEffect implements ParticleEffect {
    private final LegendWeapon plugin;
    private final String name;
    private final List<ParticleEffect> effects;
    
    public MultiParticleEffect(LegendWeapon plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.effects = new ArrayList<>();
    }
    
    public void addEffect(ParticleEffect effect) {
        effects.add(effect);
    }
    
    @Override
    public void play(Location location) {
        for (ParticleEffect effect : effects) {
            effect.play(location);
        }
    }
    
    @Override
    public String getName() {
        return name;
    }
}