package id.rnggagib.legendweapon.managers;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.particles.ParticleEffect;
import id.rnggagib.legendweapon.particles.ParticleRegistrationSystem;

import org.bukkit.Particle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ParticleManager {
    private final LegendWeapon plugin;
    private final Map<String, ParticleEffect> effects;
    private int particleDistance;
    private int maxParticlesPerAnimation;
    private ParticleRegistrationSystem registrationSystem;
    
    public ParticleManager(LegendWeapon plugin) {
        this.plugin = plugin;
        this.effects = new HashMap<>();
        loadSettings();
        
        this.registrationSystem = new ParticleRegistrationSystem(plugin);
    }
    
    public void loadSettings() {
        this.particleDistance = plugin.getConfigManager().getConfig().getInt("animation-settings.particle-distance", 32);
        this.maxParticlesPerAnimation = plugin.getConfigManager().getConfig().getInt("animation-settings.max-particles-per-animation", 100);
    }
    
    public void reloadParticles() {
        effects.clear();
        loadSettings();
        registrationSystem.registerAllEffects();
    }
    
    public void loadParticles() {
        registrationSystem.registerAllEffects();
    }
    
    public void playParticle(Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        if (location == null || location.getWorld() == null) return;
        
        location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
    }
    
    public void playParticleForPlayers(Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        if (location == null || location.getWorld() == null) return;
        
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= particleDistance) {
                player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
            }
        }
    }
    
    public void playColoredParticle(Location location, Particle particle, Color color, int count) {
        if (location == null || location.getWorld() == null) return;
        if (particle != Particle.REDSTONE && particle != Particle.DUST_COLOR_TRANSITION) return;
        
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0F);
        location.getWorld().spawnParticle(particle, location, count, 0, 0, 0, 0, dustOptions);
    }
    
    public void playEffect(String effectName, Location location) {
        if (!effects.containsKey(effectName)) {
            return;
        }
        
        effects.get(effectName).play(location);
    }
    
    public void registerEffect(String name, ParticleEffect effect) {
        effects.put(name, effect);
    }
    
    public int getParticleDistance() {
        return particleDistance;
    }
    
    public int getMaxParticlesPerAnimation() {
        return maxParticlesPerAnimation;
    }
    
    public boolean hasEffect(String effectName) {
        return effects.containsKey(effectName);
    }
    
    public Map<String, ParticleEffect> getEffects() {
        return new HashMap<>(effects);
    }
}