package id.rnggagib.legendweapon.combat;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.LegendaryWeapon;
import id.rnggagib.legendweapon.models.WeaponType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ComboManager {
    private final LegendWeapon plugin;
    private final Map<UUID, ComboData> playerCombos;
    private final Map<String, ComboPattern> weaponTypePatterns;
    private final long comboExpiryTime = 1500; // 1.5 seconds to continue a combo

    public ComboManager(LegendWeapon plugin) {
        this.plugin = plugin;
        this.playerCombos = new HashMap<>();
        this.weaponTypePatterns = new HashMap<>();
        registerDefaultComboPatterns();
    }

    private void registerDefaultComboPatterns() {
        // Sword combos
        ComboPattern swordPattern = new ComboPattern("sword");
        swordPattern.addCombo("LLL", new ComboAction("Triple Strike", 1.5, Particle.SWEEP_ATTACK, Sound.ENTITY_PLAYER_ATTACK_SWEEP));
        swordPattern.addCombo("LRL", new ComboAction("Dash Attack", 1.7, Particle.CRIT, Sound.ENTITY_PLAYER_ATTACK_STRONG));
        swordPattern.addCombo("RLR", new ComboAction("Whirlwind", 2.0, Particle.CLOUD, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK));
        weaponTypePatterns.put("sword", swordPattern);

        // Axe combos
        ComboPattern axePattern = new ComboPattern("axe");
        axePattern.addCombo("LLL", new ComboAction("Heavy Strike", 2.0, Particle.CRIT, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK));
        axePattern.addCombo("LRL", new ComboAction("Skull Crusher", 2.2, Particle.EXPLOSION_LARGE, Sound.ENTITY_PLAYER_ATTACK_STRONG));
        axePattern.addCombo("RLR", new ComboAction("Ground Breaker", 2.5, Particle.BLOCK_CRACK, Sound.ENTITY_GENERIC_EXPLODE));
        weaponTypePatterns.put("axe", axePattern);

        // Staff combos
        ComboPattern staffPattern = new ComboPattern("staff");
        staffPattern.addCombo("LLL", new ComboAction("Magic Burst", 1.3, Particle.ENCHANTMENT_TABLE, Sound.ENTITY_ILLUSIONER_CAST_SPELL));
        staffPattern.addCombo("LRL", new ComboAction("Arcane Blast", 1.8, Particle.END_ROD, Sound.ENTITY_EVOKER_CAST_SPELL));
        staffPattern.addCombo("RLR", new ComboAction("Energy Wave", 2.2, Particle.DRAGON_BREATH, Sound.ENTITY_ENDER_DRAGON_SHOOT));
        weaponTypePatterns.put("staff", staffPattern);

        // Bow combos
        ComboPattern bowPattern = new ComboPattern("bow");
        bowPattern.addCombo("LLL", new ComboAction("Triple Shot", 1.4, Particle.CRIT, Sound.ENTITY_ARROW_SHOOT));
        bowPattern.addCombo("LRL", new ComboAction("Piercing Shot", 1.6, Particle.END_ROD, Sound.ENTITY_ARROW_HIT_PLAYER));
        bowPattern.addCombo("RLR", new ComboAction("Rain of Arrows", 1.8, Particle.FALLING_WATER, Sound.ENTITY_ARROW_SHOOT));
        weaponTypePatterns.put("bow", bowPattern);
    }

    public void registerCombo(String weaponType, String comboString, ComboAction action) {
        ComboPattern pattern = weaponTypePatterns.getOrDefault(weaponType, new ComboPattern(weaponType));
        pattern.addCombo(comboString, action);
        weaponTypePatterns.put(weaponType, pattern);
    }

    public void registerClick(Player player, LegendaryWeapon weapon, boolean isLeftClick) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        if (!playerCombos.containsKey(playerId)) {
            ComboData newCombo = new ComboData(weapon.getId(), weapon.getType().getId());
            newCombo.addClick(isLeftClick, currentTime);
            playerCombos.put(playerId, newCombo);
            return;
        }
        
        ComboData comboData = playerCombos.get(playerId);
        
        if (!comboData.getWeaponId().equals(weapon.getId()) || 
            currentTime - comboData.getLastClickTime() > comboExpiryTime) {
            comboData = new ComboData(weapon.getId(), weapon.getType().getId());
            playerCombos.put(playerId, comboData);
        }
        
        comboData.addClick(isLeftClick, currentTime);
        checkAndExecuteCombo(player, comboData);
    }
    
    private void checkAndExecuteCombo(Player player, ComboData comboData) {
        if (comboData.getComboString().length() < 3) return;
        
        String weaponType = comboData.getWeaponType();
        String comboString = comboData.getComboString();
        
        if (!weaponTypePatterns.containsKey(weaponType)) return;
        
        ComboPattern pattern = weaponTypePatterns.get(weaponType);
        ComboAction action = pattern.getComboAction(comboString);
        
        if (action == null) return;
        
        executeComboAction(player, action);
        comboData.resetCombo();
    }
    
    private void executeComboAction(Player player, ComboAction action) {
        double damageMultiplier = action.getDamageMultiplier();
        Particle particle = action.getParticle();
        Sound sound = action.getSound();
        
        player.sendMessage(plugin.getMessageManager().getMessage("combat.combo-execute",
            Map.of("combo", action.getName())));
        
        Location location = player.getLocation();
        location.getWorld().playSound(location, sound, 1.0f, 1.0f);
        
        new BukkitRunnable() {
            double radius = 0.5;
            int steps = 0;
            final int maxSteps = 10;
            
            @Override
            public void run() {
                if (steps >= maxSteps) {
                    this.cancel();
                    return;
                }
                
                double angle = steps * (Math.PI / 5);
                for (int i = 0; i < 8; i++) {
                    double circleAngle = i * (Math.PI / 4) + angle;
                    double x = radius * Math.cos(circleAngle);
                    double z = radius * Math.sin(circleAngle);
                    Location particleLoc = player.getLocation().add(x, 1.0, z);
                    
                    player.getWorld().spawnParticle(
                        particle,
                        particleLoc,
                        3,
                        0.05, 0.05, 0.05,
                        0.05
                    );
                }
                
                radius += 0.2;
                steps++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }
    
    public double getComboMultiplier(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!playerCombos.containsKey(playerId)) {
            return 1.0;
        }
        
        ComboData comboData = playerCombos.get(playerId);
        String weaponType = comboData.getWeaponType();
        String comboString = comboData.getComboString();
        
        if (!weaponTypePatterns.containsKey(weaponType)) {
            return 1.0;
        }
        
        ComboPattern pattern = weaponTypePatterns.get(weaponType);
        ComboAction action = pattern.getComboAction(comboString);
        
        if (action == null) {
            return 1.0;
        }
        
        return action.getDamageMultiplier();
    }
    
    public void clear() {
        playerCombos.clear();
    }
    
    public void clearPlayer(UUID playerId) {
        playerCombos.remove(playerId);
    }
}