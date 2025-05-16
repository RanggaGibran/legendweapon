package id.rnggagib.legendweapon.managers;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.abilities.Ability;
import id.rnggagib.legendweapon.models.abilities.ActiveAbility;
import id.rnggagib.legendweapon.models.abilities.PassiveAbility;
import id.rnggagib.legendweapon.models.abilities.actives.FlameBurstAbility;
import id.rnggagib.legendweapon.models.abilities.actives.IceSpikeAbility;
import id.rnggagib.legendweapon.models.abilities.actives.VoidBurstAbility;
import id.rnggagib.legendweapon.models.abilities.passives.FlameAuraAbility;
import id.rnggagib.legendweapon.models.abilities.passives.FrostAuraAbility;
import id.rnggagib.legendweapon.models.abilities.passives.VoidSightAbility;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AbilityManager {
    private final LegendWeapon plugin;
    private final Map<String, Ability> registeredAbilities;
    private final Map<UUID, Map<String, Long>> playerCooldowns;
    
    public AbilityManager(LegendWeapon plugin) {
        this.plugin = plugin;
        this.registeredAbilities = new HashMap<>();
        this.playerCooldowns = new ConcurrentHashMap<>();
    }
    
    public void loadAbilities() {
        registeredAbilities.clear();
    }
    
    public Ability createAbilityFromConfig(String weaponId, String abilityId, ConfigurationSection config, boolean isPassive) {
        String fullId = weaponId + ":" + abilityId;
        
        if (isPassive) {
            switch (abilityId.toLowerCase()) {
                case "flame-aura":
                    return new FlameAuraAbility(plugin, fullId, config);
                case "frost-aura":
                    return new FrostAuraAbility(plugin, fullId, config);
                case "void-sight":
                    return new VoidSightAbility(plugin, fullId, config);
                default:
                    return new PassiveAbility(plugin, fullId, config);
            }
        } else {
            switch (abilityId.toLowerCase()) {
                case "flame-burst":
                    return new FlameBurstAbility(plugin, fullId, config);
                case "ice-spike":
                    return new IceSpikeAbility(plugin, fullId, config);
                case "void-burst":
                    return new VoidBurstAbility(plugin, fullId, config);
                default:
                    return new ActiveAbility(plugin, fullId, config);
            }
        }
    }
    
    public void registerAbility(Ability ability) {
        registeredAbilities.put(ability.getId(), ability);
    }
    
    public Ability getAbility(String id) {
        return registeredAbilities.get(id);
    }
    
    public boolean activateAbility(Player player, Ability ability) {
        if (ability == null) {
            return false;
        }
        
        return ability.activate(player, player.getLocation());
    }
    
    public void stopPassiveAbilities(Player player) {
        for (Ability ability : registeredAbilities.values()) {
            if (ability.isPassive() && ability instanceof PassiveAbility) {
                ((PassiveAbility) ability).stopEffect(player);
            }
        }
    }
    
    public Map<String, Ability> getRegisteredAbilities() {
        return new HashMap<>(registeredAbilities);
    }
    
    public boolean isOnCooldown(Player player, String abilityId) {
        UUID playerId = player.getUniqueId();
        
        if (!playerCooldowns.containsKey(playerId)) {
            return false;
        }
        
        Map<String, Long> cooldowns = playerCooldowns.get(playerId);
        if (!cooldowns.containsKey(abilityId)) {
            return false;
        }
        
        long lastUsed = cooldowns.get(abilityId);
        Ability ability = getAbility(abilityId);
        
        if (ability == null) {
            return false;
        }
        
        return System.currentTimeMillis() - lastUsed < ability.getCooldown();
    }
    
    public long getRemainingCooldown(Player player, String abilityId) {
        UUID playerId = player.getUniqueId();
        
        if (!playerCooldowns.containsKey(playerId)) {
            return 0;
        }
        
        Map<String, Long> cooldowns = playerCooldowns.get(playerId);
        if (!cooldowns.containsKey(abilityId)) {
            return 0;
        }
        
        long lastUsed = cooldowns.get(abilityId);
        Ability ability = getAbility(abilityId);
        
        if (ability == null) {
            return 0;
        }
        
        long remainingCooldown = (ability.getCooldown() - (System.currentTimeMillis() - lastUsed)) / 1000;
        return Math.max(0, remainingCooldown);
    }
    
    public void setCooldown(Player player, String abilityId) {
        UUID playerId = player.getUniqueId();
        
        if (!playerCooldowns.containsKey(playerId)) {
            playerCooldowns.put(playerId, new HashMap<>());
        }
        
        playerCooldowns.get(playerId).put(abilityId, System.currentTimeMillis());
    }
    
    public void clearCooldowns(Player player) {
        playerCooldowns.remove(player.getUniqueId());
    }
}