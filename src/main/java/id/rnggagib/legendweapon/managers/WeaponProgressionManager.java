package id.rnggagib.legendweapon.managers;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.LegendaryWeapon;
import id.rnggagib.legendweapon.models.WeaponRarity;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WeaponProgressionManager {
    private final LegendWeapon plugin;
    private final NamespacedKey expKey;
    private final Map<UUID, Map<String, Integer>> cachedExperience;
    private final Map<WeaponRarity, Integer> maxLevelByRarity;
    
    public WeaponProgressionManager(LegendWeapon plugin) {
        this.plugin = plugin;
        this.expKey = new NamespacedKey(plugin, "weapon_experience");
        this.cachedExperience = new HashMap<>();
        this.maxLevelByRarity = new HashMap<>();
        
        initializeLevelCaps();
        loadFromConfig();
    }
    
    private void initializeLevelCaps() {
        maxLevelByRarity.put(WeaponRarity.COMMON, 10);
        maxLevelByRarity.put(WeaponRarity.UNCOMMON, 15);
        maxLevelByRarity.put(WeaponRarity.RARE, 20);
        maxLevelByRarity.put(WeaponRarity.EPIC, 25);
        maxLevelByRarity.put(WeaponRarity.LEGENDARY, 30);
        maxLevelByRarity.put(WeaponRarity.MYTHIC, 40);
    }
    
    private void loadFromConfig() {
        if (plugin.getConfigManager().getConfig().contains("progression.max-levels")) {
            for (WeaponRarity rarity : WeaponRarity.values()) {
                String path = "progression.max-levels." + rarity.name().toLowerCase();
                if (plugin.getConfigManager().getConfig().contains(path)) {
                    int maxLevel = plugin.getConfigManager().getConfig().getInt(path);
                    maxLevelByRarity.put(rarity, maxLevel);
                }
            }
        }
    }
    
    public int getWeaponLevel(ItemStack item) {
        if (!plugin.getWeaponManager().isLegendaryWeapon(item)) {
            return 1;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 1;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey levelKey = new NamespacedKey(plugin, "weapon_level");
        
        if (container.has(levelKey, PersistentDataType.INTEGER)) {
            return container.get(levelKey, PersistentDataType.INTEGER);
        }
        
        return 1;
    }
    
    public int getWeaponExperience(ItemStack item) {
        if (!plugin.getWeaponManager().isLegendaryWeapon(item)) {
            return 0;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        if (container.has(expKey, PersistentDataType.INTEGER)) {
            return container.get(expKey, PersistentDataType.INTEGER);
        }
        
        return 0;
    }
    
    public int getExperienceRequired(int level) {
        return 50 * level * level;
    }
    
    public void addExperience(Player player, ItemStack item, int amount) {
        if (!plugin.getWeaponManager().isLegendaryWeapon(item)) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        String weaponId = container.get(new NamespacedKey(plugin, "legendary_weapon"), PersistentDataType.STRING);
        if (weaponId == null) return;
        
        LegendaryWeapon weapon = plugin.getWeaponManager().getWeaponById(weaponId);
        if (weapon == null) return;
        
        int currentLevel = getWeaponLevel(item);
        int currentExp = getWeaponExperience(item);
        
        WeaponRarity rarity = weapon.getRarity();
        int maxLevel = maxLevelByRarity.getOrDefault(rarity, 30);
        
        if (currentLevel >= maxLevel) {
            return;
        }
        
        currentExp += amount;
        int requiredExp = getExperienceRequired(currentLevel);
        
        if (currentExp >= requiredExp) {
            int newLevel = currentLevel + 1;
            currentExp -= requiredExp;
            
            setWeaponLevel(item, newLevel);
            setWeaponExperience(item, currentExp);
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("weapon", weapon.getName());
            placeholders.put("level", String.valueOf(newLevel));
            
            playLevelUpEffect(player, weapon.getRarity());
            plugin.getMessageManager().send(player, "weapons.level-up", placeholders);
            
            updateWeaponStats(item);
        } else {
            setWeaponExperience(item, currentExp);
        }
        
        player.getInventory().setItemInMainHand(item);
    }
    
    private void setWeaponLevel(ItemStack item, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(new NamespacedKey(plugin, "weapon_level"), PersistentDataType.INTEGER, level);
        
        item.setItemMeta(meta);
        updateWeaponLore(item);
    }
    
    private void setWeaponExperience(ItemStack item, int exp) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(expKey, PersistentDataType.INTEGER, exp);
        
        item.setItemMeta(meta);
    }
    
    private void updateWeaponLore(ItemStack item) {
        String weaponId = plugin.getWeaponManager().getWeaponFromItem(item).getId();
        int level = getWeaponLevel(item);
        
        ItemStack updatedItem = plugin.getWeaponManager().createWeaponItem(weaponId, level);
        
        ItemMeta oldMeta = item.getItemMeta();
        ItemMeta newMeta = updatedItem.getItemMeta();
        
        if (oldMeta != null && newMeta != null) {
            newMeta.getPersistentDataContainer().set(expKey, PersistentDataType.INTEGER, 
                oldMeta.getPersistentDataContainer().getOrDefault(expKey, PersistentDataType.INTEGER, 0));
            
            item.setItemMeta(newMeta);
        }
    }
    
    private void updateWeaponStats(ItemStack item) {
        LegendaryWeapon weapon = plugin.getWeaponManager().getWeaponFromItem(item);
        if (weapon == null) return;
        
        int level = getWeaponLevel(item);
        
        if (level > 1) {
            double multiplier = 1.0 + ((level - 1) * 0.1);
        }
    }
    
    private void playLevelUpEffect(Player player, WeaponRarity rarity) {
        Location location = player.getLocation().add(0, 1, 0);
        
        Sound sound;
        Particle mainParticle;
        Particle secondaryParticle;
        
        switch (rarity) {
            case COMMON:
                sound = Sound.ENTITY_PLAYER_LEVELUP;
                mainParticle = Particle.VILLAGER_HAPPY;
                secondaryParticle = Particle.CRIT;
                break;
            case UNCOMMON:
                sound = Sound.ENTITY_PLAYER_LEVELUP;
                mainParticle = Particle.COMPOSTER;
                secondaryParticle = Particle.CRIT;
                break;
            case RARE:
                sound = Sound.BLOCK_ENCHANTMENT_TABLE_USE;
                mainParticle = Particle.ENCHANTMENT_TABLE;
                secondaryParticle = Particle.END_ROD;
                break;
            case EPIC:
                sound = Sound.BLOCK_ENCHANTMENT_TABLE_USE;
                mainParticle = Particle.ENCHANTMENT_TABLE;
                secondaryParticle = Particle.SOUL_FIRE_FLAME;
                break;
            case LEGENDARY:
                sound = Sound.ENTITY_WITHER_SPAWN;
                mainParticle = Particle.FLAME;
                secondaryParticle = Particle.FLASH;
                break;
            case MYTHIC:
                sound = Sound.ENTITY_ENDER_DRAGON_GROWL;
                mainParticle = Particle.DRAGON_BREATH;
                secondaryParticle = Particle.SONIC_BOOM;
                break;
            default:
                sound = Sound.ENTITY_PLAYER_LEVELUP;
                mainParticle = Particle.VILLAGER_HAPPY;
                secondaryParticle = Particle.CRIT;
                break;
        }
        
        player.getWorld().playSound(location, sound, 1.0f, 1.0f);
        
        for (int i = 0; i < 5; i++) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getParticleManager().playParticle(location, mainParticle, 20, 0.5, 0.5, 0.5, 0.1);
                plugin.getParticleManager().playParticle(location, secondaryParticle, 30, 0.5, 1.0, 0.5, 0.2);
                
                if (rarity == WeaponRarity.MYTHIC || rarity == WeaponRarity.LEGENDARY) {
                    plugin.getParticleManager().playParticle(location, Particle.EXPLOSION_NORMAL, 3, 0.5, 0.5, 0.5, 0.1);
                }
            }, i * 5L);
        }
    }
    
    public void gainExperienceFromKill(Player player, ItemStack weapon, int baseXp) {
        LegendaryWeapon legendaryWeapon = plugin.getWeaponManager().getWeaponFromItem(weapon);
        if (legendaryWeapon == null) return;
        
        WeaponRarity rarity = legendaryWeapon.getRarity();
        int rarityMultiplier;
        
        switch (rarity) {
            case COMMON:
                rarityMultiplier = 1;
                break;
            case UNCOMMON:
                rarityMultiplier = 2;
                break;
            case RARE:
                rarityMultiplier = 3;
                break;
            case EPIC:
                rarityMultiplier = 4;
                break;
            case LEGENDARY:
                rarityMultiplier = 5;
                break;
            case MYTHIC:
                rarityMultiplier = 7;
                break;
            default:
                rarityMultiplier = 1;
        }
        
        int finalXp = baseXp * rarityMultiplier;
        addExperience(player, weapon, finalXp);
    }
    
    public double getStatMultiplier(int level) {
        return 1.0 + ((level - 1) * 0.1);
    }
    
    public int getMaxLevel(WeaponRarity rarity) {
        return maxLevelByRarity.getOrDefault(rarity, 30);
    }
}