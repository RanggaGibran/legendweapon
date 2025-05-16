package id.rnggagib.legendweapon.models.abilities;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.LegendaryWeapon;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAbility implements Ability {
    protected final LegendWeapon plugin;
    protected final String id;
    protected final String name;
    protected final String description;
    protected final long cooldown;
    protected final boolean passive;
    protected final Map<String, Object> properties;
    
    public AbstractAbility(LegendWeapon plugin, String id, String name, String description, long cooldown, boolean passive) {
        this.plugin = plugin;
        this.id = id;
        this.name = name;
        this.description = description;
        this.cooldown = cooldown;
        this.passive = passive;
        this.properties = new HashMap<>();
    }
    
    public AbstractAbility(LegendWeapon plugin, String id, ConfigurationSection config, boolean passive) {
        this.plugin = plugin;
        this.id = id;
        this.name = config.getString("name", id);
        this.description = config.getString("description", "No description");
        this.cooldown = config.getLong("cooldown", 30) * 1000; // Convert to milliseconds
        this.passive = passive;
        this.properties = new HashMap<>();
        
        if (config.getKeys(false) != null) {
            for (String key : config.getKeys(false)) {
                if (!key.equals("name") && !key.equals("description") && !key.equals("cooldown")) {
                    properties.put(key, config.get(key));
                }
            }
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public boolean isPassive() {
        return passive;
    }
    
    public Object getProperty(String key) {
        return properties.getOrDefault(key, null);
    }
    
    public Object getProperty(String key, Object defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }
    
    public int getIntProperty(String key, int defaultValue) {
        Object value = properties.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    public double getDoubleProperty(String key, double defaultValue) {
        Object value = properties.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
    
    public String getStringProperty(String key, String defaultValue) {
        Object value = properties.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }
    
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        Object value = properties.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
}