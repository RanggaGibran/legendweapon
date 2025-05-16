package id.rnggagib.legendweapon.managers;

import id.rnggagib.LegendWeapon;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final LegendWeapon plugin;
    private FileConfiguration config;
    private FileConfiguration weapons;
    private FileConfiguration messages;
    
    private File configFile;
    private File weaponsFile;
    private File messagesFile;
    
    public ConfigManager(LegendWeapon plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfigs() {
        createFiles();
        
        config = YamlConfiguration.loadConfiguration(configFile);
        weapons = YamlConfiguration.loadConfiguration(weaponsFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    private void createFiles() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        weaponsFile = new File(plugin.getDataFolder(), "weapons.yml");
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        if (!weaponsFile.exists()) {
            plugin.saveResource("weapons.yml", false);
        }
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getWeapons() {
        return weapons;
    }
    
    public FileConfiguration getMessages() {
        return messages;
    }
    
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml: " + e.getMessage());
        }
    }
    
    public void saveWeapons() {
        try {
            weapons.save(weaponsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save weapons.yml: " + e.getMessage());
        }
    }
    
    public void reloadConfigs() {
        loadConfigs();
    }
}