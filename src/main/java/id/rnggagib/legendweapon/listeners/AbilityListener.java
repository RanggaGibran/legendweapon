package id.rnggagib.legendweapon.listeners;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.LegendaryWeapon;
import id.rnggagib.legendweapon.models.abilities.Ability;
import id.rnggagib.legendweapon.models.abilities.ActiveAbility;
import id.rnggagib.legendweapon.models.abilities.PassiveAbility;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AbilityListener implements Listener {
    private final LegendWeapon plugin;
    private final Map<UUID, String> activeWeapons;
    
    public AbilityListener(LegendWeapon plugin) {
        this.plugin = plugin;
        this.activeWeapons = new ConcurrentHashMap<>();
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) return;
        if (!plugin.getWeaponManager().isLegendaryWeapon(item)) return;
        
        LegendaryWeapon weapon = plugin.getWeaponManager().getWeaponFromItem(item);
        if (weapon == null) return;
        
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && player.isSneaking()) {
            event.setCancelled(true);
            activateWeaponAbility(player, weapon);
        }
    }
    
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        
        checkAndUpdatePassiveEffects(player, newItem);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        
        checkAndUpdatePassiveEffects(player, heldItem);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        activeWeapons.remove(player.getUniqueId());
        plugin.getAbilityManager().stopPassiveAbilities(player);
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        
        checkAndUpdatePassiveEffects(player, heldItem);
    }
    
    private void checkAndUpdatePassiveEffects(Player player, ItemStack newItem) {
        UUID playerId = player.getUniqueId();
        
        if (newItem == null || !plugin.getWeaponManager().isLegendaryWeapon(newItem)) {
            if (activeWeapons.containsKey(playerId)) {
                plugin.getAbilityManager().stopPassiveAbilities(player);
                activeWeapons.remove(playerId);
            }
            return;
        }
        
        LegendaryWeapon weapon = plugin.getWeaponManager().getWeaponFromItem(newItem);
        if (weapon == null) return;
        
        String weaponId = weapon.getId();
        
        if (activeWeapons.containsKey(playerId) && activeWeapons.get(playerId).equals(weaponId)) {
            return;
        }
        
        plugin.getAbilityManager().stopPassiveAbilities(player);
        activeWeapons.put(playerId, weaponId);
        
        for (Ability ability : weapon.getAbilities().values()) {
            if (ability.isPassive()) {
                plugin.getAbilityManager().activateAbility(player, ability);
            }
        }
    }
    
    private void activateWeaponAbility(Player player, LegendaryWeapon weapon) {
        for (Ability ability : weapon.getAbilities().values()) {
            if (!ability.isPassive() && ability instanceof ActiveAbility) {
                plugin.getAbilityManager().activateAbility(player, ability);
                return;
            }
        }
    }
}