package id.rnggagib.legendweapon.managers;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.LegendaryWeapon;
import id.rnggagib.legendweapon.models.WeaponType;
import id.rnggagib.legendweapon.models.WeaponRarity;
import id.rnggagib.legendweapon.models.abilities.Ability;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeaponManager {
    private final LegendWeapon plugin;
    private final Map<String, LegendaryWeapon> weapons;
    private final Map<String, WeaponType> weaponTypes;
    private final MiniMessage miniMessage;
    private final NamespacedKey weaponKey;
    private final NamespacedKey levelKey;
    
    public WeaponManager(LegendWeapon plugin) {
        this.plugin = plugin;
        this.weapons = new HashMap<>();
        this.weaponTypes = new HashMap<>();
        this.miniMessage = MiniMessage.miniMessage();
        this.weaponKey = new NamespacedKey(plugin, "legendary_weapon");
        this.levelKey = new NamespacedKey(plugin, "weapon_level");
    }
    
    public void loadWeapons() {
        weapons.clear();
        weaponTypes.clear();
        
        loadWeaponTypes();
        loadWeaponDefinitions();
    }
    
    private void loadWeaponTypes() {
        ConfigurationSection typeSection = plugin.getConfigManager().getWeapons().getConfigurationSection("weapon-types");
        if (typeSection == null) return;
        
        for (String typeName : typeSection.getKeys(false)) {
            ConfigurationSection section = typeSection.getConfigurationSection(typeName);
            if (section == null) continue;
            
            try {
                Material material = Material.valueOf(section.getString("item", "DIAMOND_SWORD"));
                double attackSpeed = section.getDouble("attack-speed", 1.0);
                double baseDamage = section.getDouble("base-damage", 5.0);
                
                WeaponType type = new WeaponType(typeName, material, attackSpeed, baseDamage);
                weaponTypes.put(typeName.toLowerCase(), type);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material for weapon type: " + typeName);
            }
        }
    }
    
    private void loadWeaponDefinitions() {
        ConfigurationSection weaponSection = plugin.getConfigManager().getWeapons().getConfigurationSection("weapons");
        if (weaponSection == null) return;
        
        for (String weaponId : weaponSection.getKeys(false)) {
            ConfigurationSection section = weaponSection.getConfigurationSection(weaponId);
            if (section == null) continue;
            
            String name = section.getString("name", "Unknown Weapon");
            String typeId = section.getString("type", "sword");
            String rarityStr = section.getString("rarity", "common");
            
            WeaponType type = weaponTypes.getOrDefault(typeId.toLowerCase(), getDefaultWeaponType());
            WeaponRarity rarity = WeaponRarity.valueOf(rarityStr.toUpperCase());
            
            List<String> lore = section.getStringList("lore");
            
            Map<String, Double> attributes = loadAttributes(section.getConfigurationSection("attributes"));
            Map<String, Ability> abilities = loadAbilities(weaponId, section.getConfigurationSection("abilities"));
            
            LegendaryWeapon weapon = new LegendaryWeapon(weaponId, name, type, rarity, lore, attributes, abilities);
            weapons.put(weaponId.toLowerCase(), weapon);
        }
    }
    
    private Map<String, Double> loadAttributes(ConfigurationSection section) {
        Map<String, Double> attributes = new HashMap<>();
        
        if (section != null) {
            for (String key : section.getKeys(false)) {
                attributes.put(key, section.getDouble(key));
            }
        }
        
        return attributes;
    }
    
    private Map<String, Ability> loadAbilities(String weaponId, ConfigurationSection section) {
        Map<String, Ability> abilities = new HashMap<>();
        
        if (section != null) {
            ConfigurationSection passiveSection = section.getConfigurationSection("passive");
            if (passiveSection != null) {
                for (String abilityId : passiveSection.getKeys(false)) {
                    ConfigurationSection abilityConfig = passiveSection.getConfigurationSection(abilityId);
                    if (abilityConfig != null) {
                        Ability ability = plugin.getAbilityManager().createAbilityFromConfig(weaponId, abilityId, abilityConfig, true);
                        abilities.put(ability.getId(), ability);
                        plugin.getAbilityManager().registerAbility(ability);
                    }
                }
            }
            
            ConfigurationSection activeSection = section.getConfigurationSection("active");
            if (activeSection != null) {
                for (String abilityId : activeSection.getKeys(false)) {
                    ConfigurationSection abilityConfig = activeSection.getConfigurationSection(abilityId);
                    if (abilityConfig != null) {
                        Ability ability = plugin.getAbilityManager().createAbilityFromConfig(weaponId, abilityId, abilityConfig, false);
                        abilities.put(ability.getId(), ability);
                        plugin.getAbilityManager().registerAbility(ability);
                    }
                }
            }
        }
        
        return abilities;
    }
    
    public boolean isLegendaryWeapon(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(weaponKey, PersistentDataType.STRING);
    }
    
    public LegendaryWeapon getWeaponFromItem(ItemStack item) {
        if (!isLegendaryWeapon(item)) return null;
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        String weaponId = container.get(weaponKey, PersistentDataType.STRING);
        if (weaponId == null) return null;
        
        return weapons.getOrDefault(weaponId.toLowerCase(), null);
    }
    
    public ItemStack createWeaponItem(String id, int level) {
        LegendaryWeapon weapon = weapons.get(id.toLowerCase());
        if (weapon == null) return null;
        
        ItemStack item = new ItemStack(weapon.getType().getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        String displayName = formatWeaponName(weapon);
        meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(displayName)));
        
        List<Component> componentLore = new ArrayList<>();
        
        componentLore.add(miniMessage.deserialize(plugin.getMessageManager().getMessage("rarities." + weapon.getRarity().name().toLowerCase(), new HashMap<>())));
        
        for (String loreLine : weapon.getLore()) {
            componentLore.add(miniMessage.deserialize(loreLine));
        }
        
        componentLore.add(miniMessage.deserialize("<dark_gray>---------------"));
        componentLore.add(miniMessage.deserialize("<yellow>Stats:"));
        
        double multiplier = plugin.getWeaponProgressionManager().getStatMultiplier(level);
        
        for (Map.Entry<String, Double> entry : weapon.getAttributes().entrySet()) {
            String attributeName = formatAttributeName(entry.getKey());
            double baseValue = entry.getValue();
            double scaledValue = Math.round(baseValue * multiplier * 10) / 10.0;
            
            componentLore.add(miniMessage.deserialize("<gray>" + attributeName + ": <white>" + scaledValue));
        }
        
        if (!weapon.getAbilities().isEmpty()) {
            componentLore.add(miniMessage.deserialize("<dark_gray>---------------"));
            componentLore.add(miniMessage.deserialize("<yellow>Abilities:"));
            
            for (Ability ability : weapon.getAbilities().values()) {
                componentLore.add(miniMessage.deserialize("<gray>" + ability.getName() + ": <white>" + ability.getDescription()));
            }
        }
        
        componentLore.add(miniMessage.deserialize("<dark_gray>---------------"));
        componentLore.add(miniMessage.deserialize("<gray>Level: <white>" + level));
        
        int exp = 0;
        int requiredExp = plugin.getWeaponProgressionManager().getExperienceRequired(level);
        int maxLevel = plugin.getWeaponProgressionManager().getMaxLevel(weapon.getRarity());
        
        if (level < maxLevel) {
            componentLore.add(miniMessage.deserialize("<gray>EXP: <white>" + exp + "/" + requiredExp));
        } else {
            componentLore.add(miniMessage.deserialize("<gold>MAXIMUM LEVEL"));
        }
        
        List<String> bukkitLore = new ArrayList<>();
        for (Component component : componentLore) {
            String legacyText = LegacyComponentSerializer.legacySection().serialize(component);
            bukkitLore.add(legacyText);
        }
        meta.setLore(bukkitLore);
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(weaponKey, PersistentDataType.STRING, id.toLowerCase());
        container.set(levelKey, PersistentDataType.INTEGER, level);
        container.set(new NamespacedKey(plugin, "weapon_experience"), PersistentDataType.INTEGER, exp);
        
        item.setItemMeta(meta);
        return item;
    }
    
    public void giveWeapon(Player player, String weaponId, int level) {
        ItemStack weaponItem = createWeaponItem(weaponId, level);
        if (weaponItem == null) return;
        
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("weapon", getWeaponById(weaponId).getName());
        placeholders.put("level", String.valueOf(level));
        placeholders.put("rarity", getWeaponById(weaponId).getRarity().name().toLowerCase());
        
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), weaponItem);
        } else {
            player.getInventory().addItem(weaponItem);
        }
        
        plugin.getMessageManager().send(player, "weapons.receive-weapon", placeholders);
    }
    
    public LegendaryWeapon getWeaponById(String id) {
        return weapons.get(id.toLowerCase());
    }
    
    public Map<String, LegendaryWeapon> getAllWeapons() {
        return new HashMap<>(weapons);
    }
    
    private String formatWeaponName(LegendaryWeapon weapon) {
        WeaponRarity rarity = weapon.getRarity();
        String colorPrefix;
        
        switch (rarity) {
            case COMMON:
                colorPrefix = "<white>";
                break;
            case UNCOMMON:
                colorPrefix = "<green>";
                break;
            case RARE:
                colorPrefix = "<blue>";
                break;
            case EPIC:
                colorPrefix = "<dark_purple>";
                break;
            case LEGENDARY:
                colorPrefix = "<gold>";
                break;
            case MYTHIC:
                colorPrefix = "<gradient:#FF00FF:#00FFFF>";
                break;
            default:
                colorPrefix = "<white>";
        }
        
        return colorPrefix + weapon.getName();
    }
    
    private String formatAttributeName(String attributeId) {
        String[] words = attributeId.split("-");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            result.append(word.substring(0, 1).toUpperCase())
                  .append(word.substring(1))
                  .append(" ");
        }
        
        return result.toString().trim();
    }
    
    private WeaponType getDefaultWeaponType() {
        return new WeaponType("sword", Material.DIAMOND_SWORD, 1.6, 7.0);
    }
    
    public double getScaledAttributeValue(ItemStack weapon, String attributeId) {
        if (!isLegendaryWeapon(weapon)) return 0;
        
        LegendaryWeapon legendaryWeapon = getWeaponFromItem(weapon);
        if (legendaryWeapon == null) return 0;
        
        double baseValue = legendaryWeapon.getAttributes().getOrDefault(attributeId, 0.0);
        int level = plugin.getWeaponProgressionManager().getWeaponLevel(weapon);
        
        double multiplier = plugin.getWeaponProgressionManager().getStatMultiplier(level);
        return baseValue * multiplier;
    }
}