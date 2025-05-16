package id.rnggagib;

import id.rnggagib.legendweapon.managers.AbilityManager;
import id.rnggagib.legendweapon.managers.ConfigManager;
import id.rnggagib.legendweapon.managers.MessageManager;
import id.rnggagib.legendweapon.managers.ParticleManager;
import id.rnggagib.legendweapon.managers.WeaponManager;
import id.rnggagib.legendweapon.commands.CommandManager;
import id.rnggagib.legendweapon.listeners.WeaponListener;
import id.rnggagib.legendweapon.listeners.PlayerListener;
import id.rnggagib.legendweapon.listeners.AbilityListener;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;

public class LegendWeapon extends JavaPlugin {
    private static LegendWeapon instance;
    
    private ConfigManager configManager;
    private MessageManager messageManager;
    private WeaponManager weaponManager;
    private ParticleManager particleManager;
    private AbilityManager abilityManager;
    private CommandManager commandManager;
    private BukkitAudiences adventure;
    private Economy economy;
    private boolean placeholderAPIEnabled = false;

    @Override
    public void onEnable() {
        instance = this;
        
        adventure = BukkitAudiences.create(this);
        
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        messageManager = new MessageManager(this);
        
        abilityManager = new AbilityManager(this);
        
        particleManager = new ParticleManager(this);
        
        weaponManager = new WeaponManager(this);
        weaponManager.loadWeapons();
        
        particleManager.loadParticles();
        
        setupDependencies();
        
        commandManager = new CommandManager(this);
        commandManager.registerCommands();
        
        registerListeners();
        
        messageManager.sendConsole("general.plugin-enabled");
    }

    @Override
    public void onDisable() {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
        
        messageManager.sendConsole("general.plugin-disabled");
        instance = null;
    }
    
    private void setupDependencies() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
            }
        }
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true;
        }
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new WeaponListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new AbilityListener(this), this);
    }
    
    public void reload() {
        configManager.loadConfigs();
        abilityManager.loadAbilities();
        weaponManager.loadWeapons();
        particleManager.reloadParticles();
    }
    
    public static LegendWeapon getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    public WeaponManager getWeaponManager() {
        return weaponManager;
    }
    
    public ParticleManager getParticleManager() {
        return particleManager;
    }
    
    public AbilityManager getAbilityManager() {
        return abilityManager;
    }
    
    public BukkitAudiences adventure() {
        if (adventure == null) {
            throw new IllegalStateException("Audience is null - plugin is disabled");
        }
        return adventure;
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
}