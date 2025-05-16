package id.rnggagib.legendweapon.particles;

import id.rnggagib.LegendWeapon;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class ParticleEffectFactory {
    private final LegendWeapon plugin;
    private final Map<String, Particle> particleMap;
    private final Map<String, Sound> soundMap;
    
    public ParticleEffectFactory(LegendWeapon plugin) {
        this.plugin = plugin;
        this.particleMap = new HashMap<>();
        this.soundMap = new HashMap<>();
        initMaps();
    }
    
    private void initMaps() {
        for (Particle particle : Particle.values()) {
            particleMap.put(particle.name().toLowerCase(), particle);
        }
        
        for (Sound sound : Sound.values()) {
            soundMap.put(sound.name().toLowerCase(), sound);
        }
    }
    
    public ParticleEffect createEffectFromConfig(String effectId, ConfigurationSection config) {
        if (config == null) return null;
        
        String type = config.getString("type", "burst");
        String particleName = config.getString("particle", "flame");
        int count = config.getInt("count", 20);
        double speed = config.getDouble("speed", 0.1);
        
        Particle particle = particleMap.getOrDefault(particleName.toLowerCase(), Particle.FLAME);
        
        Sound sound = null;
        float volume = 1.0f;
        float pitch = 1.0f;
        
        if (config.contains("sound")) {
            String soundName = config.getString("sound", "");
            sound = soundMap.getOrDefault(soundName.toLowerCase(), null);
            volume = (float) config.getDouble("volume", 1.0);
            pitch = (float) config.getDouble("pitch", 1.0);
        }
        
        switch (type.toLowerCase()) {
            case "spiral":
                double spiralRadius = config.getDouble("radius", 1.0);
                double spiralHeightStep = config.getDouble("height-step", 0.1);
                int spiralIterations = config.getInt("iterations", 20);
                
                return new SpiralParticleEffect(plugin, effectId, particle, count, speed, sound, volume, pitch,
                                               spiralRadius, spiralHeightStep, spiralIterations);
                
            case "burst":
                double offsetX = config.getDouble("offset-x", 0.3);
                double offsetY = config.getDouble("offset-y", 0.3);
                double offsetZ = config.getDouble("offset-z", 0.3);
                
                return new BurstParticleEffect(plugin, effectId, particle, count, speed, sound, volume, pitch,
                                              offsetX, offsetY, offsetZ);
                
            case "circle":
                double circleRadius = config.getDouble("radius", 1.5);
                double circleHeightOffset = config.getDouble("height-offset", 0.1);
                int circlePoints = config.getInt("points", 20);
                
                return new CircleParticleEffect(plugin, effectId, particle, count, speed, sound, volume, pitch,
                                              circleRadius, circleHeightOffset, circlePoints);
                
            case "trail":
                double trailLength = config.getDouble("length", 3.0);
                int trailSteps = config.getInt("steps", 10);
                double dirX = config.getDouble("dir-x", 1.0);
                double dirY = config.getDouble("dir-y", 0.0);
                double dirZ = config.getDouble("dir-z", 0.0);
                
                Vector direction = new Vector(dirX, dirY, dirZ);
                
                return new TrailParticleEffect(plugin, effectId, particle, count, speed, sound, volume, pitch,
                                             trailLength, trailSteps, direction);
                
            case "helix":
                double helixRadius = config.getDouble("radius", 1.0);
                double helixHeight = config.getDouble("height", 3.0);
                int helixCount = config.getInt("helices", 2);
                int helixPoints = config.getInt("points-per-helix", 20);
                
                return new HelixParticleEffect(plugin, effectId, particle, count, speed, sound, volume, pitch,
                                            helixRadius, helixHeight, helixCount, helixPoints);
                
            default:
                return new BurstParticleEffect(plugin, effectId, particle, count, speed, sound, volume, pitch,
                                            0.3, 0.3, 0.3);
        }
    }
}