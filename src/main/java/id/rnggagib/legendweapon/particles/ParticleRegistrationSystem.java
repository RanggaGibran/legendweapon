package id.rnggagib.legendweapon.particles;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.LegendaryWeapon;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ParticleRegistrationSystem {
    private final LegendWeapon plugin;
    private final ParticleEffectFactory effectFactory;
    
    public ParticleRegistrationSystem(LegendWeapon plugin) {
        this.plugin = plugin;
        this.effectFactory = new ParticleEffectFactory(plugin);
    }
    
    public void registerAllEffects() {
        FileConfiguration weaponConfig = plugin.getConfigManager().getWeapons();
        ConfigurationSection weaponsSection = weaponConfig.getConfigurationSection("weapons");
        
        if (weaponsSection == null) {
            return;
        }
        
        for (String weaponId : weaponsSection.getKeys(false)) {
            ConfigurationSection weaponSection = weaponsSection.getConfigurationSection(weaponId);
            if (weaponSection == null) continue;
            
            ConfigurationSection animationsSection = weaponSection.getConfigurationSection("animations");
            if (animationsSection == null) continue;
            
            registerWeaponAnimations(weaponId, animationsSection);
        }
    }
    
    private void registerWeaponAnimations(String weaponId, ConfigurationSection animationsSection) {
        for (String animationType : animationsSection.getKeys(false)) {
            ConfigurationSection animationSection = animationsSection.getConfigurationSection(animationType);
            if (animationSection == null) continue;
            
            String effectId = weaponId + "_" + animationType;
            registerAnimationEffect(effectId, animationSection);
            
            if (animationType.equals("hit")) {
                registerCriticalHitEffect(weaponId, animationSection);
            }
        }
    }
    
    private void registerAnimationEffect(String effectId, ConfigurationSection animationSection) {
        ConfigurationSection particlesSection = animationSection.getConfigurationSection("particles");
        if (particlesSection == null) return;
        
        MultiParticleEffect multiEffect = new MultiParticleEffect(plugin, effectId);
        
        for (String key : particlesSection.getKeys(false)) {
            ConfigurationSection particleSection = particlesSection.getConfigurationSection(key);
            if (particleSection == null) continue;
            
            ParticleEffect effect = effectFactory.createEffectFromConfig(effectId + "_" + key, particleSection);
            if (effect != null) {
                multiEffect.addEffect(effect);
            }
        }
        
        plugin.getParticleManager().registerEffect(effectId, multiEffect);
    }
    
    private void registerCriticalHitEffect(String weaponId, ConfigurationSection hitAnimationSection) {
        ConfigurationSection particlesSection = hitAnimationSection.getConfigurationSection("particles");
        if (particlesSection == null) return;
        
        String critEffectId = weaponId + "_critical";
        MultiParticleEffect critEffect = new MultiParticleEffect(plugin, critEffectId);
        
        for (String key : particlesSection.getKeys(false)) {
            ConfigurationSection particleSection = particlesSection.getConfigurationSection(key);
            if (particleSection == null) continue;
            
            particleSection.set("count", particleSection.getInt("count") * 1.5);
            particleSection.set("speed", particleSection.getDouble("speed") * 1.2);
            
            if (!particleSection.contains("type")) {
                particleSection.set("type", "burst");
            }
            
            String particleType = particleSection.getString("particle", "flame");
            if (particleType.equals("flame")) {
                particleSection.set("particle", "crit");
            } else if (particleType.equals("explosion")) {
                particleSection.set("particle", "enchanted_hit");
            }
            
            ParticleEffect effect = effectFactory.createEffectFromConfig(critEffectId + "_" + key, particleSection);
            if (effect != null) {
                critEffect.addEffect(effect);
            }
        }
        
        plugin.getParticleManager().registerEffect(critEffectId, critEffect);
    }
}