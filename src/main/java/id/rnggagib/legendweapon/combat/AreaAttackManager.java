package id.rnggagib.legendweapon.combat;

import id.rnggagib.LegendWeapon;
import id.rnggagib.legendweapon.models.LegendaryWeapon;
import id.rnggagib.legendweapon.models.WeaponRarity;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AreaAttackManager {
    private final LegendWeapon plugin;
    private final Map<String, AreaAttack> registeredAttacks;
    
    public AreaAttackManager(LegendWeapon plugin) {
        this.plugin = plugin;
        this.registeredAttacks = new HashMap<>();
        registerDefaultAreaAttacks();
    }
    
    private void registerDefaultAreaAttacks() {
        // Sword Area Attacks
        registerAreaAttack("sword_spin", new AreaAttack(
            "Sword Spin",
            3.0,
            1.0,
            Particle.SWEEP_ATTACK,
            Sound.ENTITY_PLAYER_ATTACK_SWEEP,
            this::circleAttackPattern
        ));
        
        registerAreaAttack("sword_wave", new AreaAttack(
            "Sword Wave",
            5.0,
            0.8,
            Particle.CRIT,
            Sound.ENTITY_PLAYER_ATTACK_STRONG,
            this::waveAttackPattern
        ));
        
        // Axe Area Attacks
        registerAreaAttack("ground_smash", new AreaAttack(
            "Ground Smash",
            4.0,
            1.5,
            Particle.EXPLOSION_HUGE,
            Sound.ENTITY_GENERIC_EXPLODE,
            this::explosionAttackPattern
        ));
        
        registerAreaAttack("cleave", new AreaAttack(
            "Cleave",
            3.0,
            1.2,
            Particle.CRIT,
            Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK,
            this::coneAttackPattern
        ));
        
        // Staff Area Attacks
        registerAreaAttack("arcane_explosion", new AreaAttack(
            "Arcane Explosion",
            5.0,
            1.0,
            Particle.END_ROD,
            Sound.ENTITY_ILLUSIONER_CAST_SPELL,
            this::explosionAttackPattern
        ));
        
        registerAreaAttack("magic_wave", new AreaAttack(
            "Magic Wave",
            6.0,
            0.7,
            Particle.ENCHANTMENT_TABLE,
            Sound.ENTITY_EVOKER_CAST_SPELL,
            this::sphereAttackPattern
        ));
        
        // Bow Area Attacks
        registerAreaAttack("arrow_rain", new AreaAttack(
            "Arrow Rain",
            5.0,
            0.8,
            Particle.CRIT,
            Sound.ENTITY_ARROW_SHOOT,
            this::rainAttackPattern
        ));
    }
    
    public void registerAreaAttack(String id, AreaAttack attack) {
        registeredAttacks.put(id, attack);
    }
    
    public void performAreaAttack(Player player, LegendaryWeapon weapon, String attackId) {
        if (!registeredAttacks.containsKey(attackId)) {
            return;
        }
        
        AreaAttack attack = registeredAttacks.get(attackId);
        Location location = player.getLocation();
        
        // Play sound
        location.getWorld().playSound(location, attack.getSound(), 1.0f, 1.0f);
        
        // Apply attack pattern
        attack.getAttackPattern().accept(new AttackContext(player, location, attack));
        
        // Get targets
        Collection<Entity> targets = location.getWorld().getNearbyEntities(
            location, 
            attack.getRadius(), 
            attack.getRadius(), 
            attack.getRadius()
        );
        
        double damage = calculateAttackDamage(player, weapon, attack);
        
        for (Entity target : targets) {
            if (target instanceof LivingEntity && target != player) {
                LivingEntity livingTarget = (LivingEntity) target;
                livingTarget.damage(damage, player);
                
                // Apply knockback
                Vector knockback = target.getLocation().toVector().subtract(player.getLocation().toVector())
                    .normalize().multiply(0.5).setY(0.2);
                target.setVelocity(target.getVelocity().add(knockback));
            }
        }
        
        // Send message
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("attack", attack.getName());
        player.sendMessage(plugin.getMessageManager().getMessage("combat.area-attack", placeholders));
    }
    
    private double calculateAttackDamage(Player player, LegendaryWeapon weapon, AreaAttack attack) {
        double baseDamage = plugin.getWeaponManager().getScaledAttributeValue(
            player.getInventory().getItemInMainHand(), "damage");
        
        double rarityMultiplier = getRarityMultiplier(weapon.getRarity());
        double levelMultiplier = plugin.getWeaponProgressionManager().getStatMultiplier(
            plugin.getWeaponProgressionManager().getWeaponLevel(player.getInventory().getItemInMainHand()));
        
        return baseDamage * attack.getDamageMultiplier() * rarityMultiplier * levelMultiplier;
    }
    
    private double getRarityMultiplier(WeaponRarity rarity) {
        switch (rarity) {
            case COMMON: return 1.0;
            case UNCOMMON: return 1.1;
            case RARE: return 1.2;
            case EPIC: return 1.3;
            case LEGENDARY: return 1.5;
            case MYTHIC: return 1.8;
            default: return 1.0;
        }
    }
    
    // Attack patterns
    private void circleAttackPattern(AttackContext context) {
        Player player = context.getPlayer();
        Location center = context.getLocation();
        AreaAttack attack = context.getAttack();
        
        new BukkitRunnable() {
            int step = 0;
            final int totalSteps = 20;
            final double radius = attack.getRadius();
            
            @Override
            public void run() {
                if (step >= totalSteps) {
                    this.cancel();
                    return;
                }
                
                double angle = step * (2 * Math.PI / totalSteps);
                
                for (int i = 0; i < 8; i++) {
                    double particleAngle = angle + (i * Math.PI / 4);
                    double x = radius * Math.cos(particleAngle);
                    double z = radius * Math.sin(particleAngle);
                    
                    Location particleLoc = center.clone().add(x, 0.5, z);
                    center.getWorld().spawnParticle(
                        attack.getParticle(),
                        particleLoc,
                        5,
                        0.1, 0.1, 0.1,
                        0.05
                    );
                }
                
                step++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
    
    private void waveAttackPattern(AttackContext context) {
        Player player = context.getPlayer();
        Location center = context.getLocation();
        AreaAttack attack = context.getAttack();
        
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        
        new BukkitRunnable() {
            int step = 0;
            final int totalSteps = 10;
            final double distance = attack.getRadius();
            
            @Override
            public void run() {
                if (step >= totalSteps) {
                    this.cancel();
                    return;
                }
                
                double progress = (double) step / totalSteps;
                double currentDistance = progress * distance;
                
                Vector offset = direction.clone().multiply(currentDistance);
                Location waveFront = center.clone().add(offset);
                
                double width = 3.0;
                double halfWidth = width / 2;
                
                Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
                
                for (int i = 0; i < 10; i++) {
                    double t = -halfWidth + (width * i / 9);
                    Vector sideOffset = perpendicular.clone().multiply(t);
                    Location particleLoc = waveFront.clone().add(sideOffset).add(0, 0.5, 0);
                    
                    center.getWorld().spawnParticle(
                        attack.getParticle(),
                        particleLoc,
                        3,
                        0.05, 0.05, 0.05,
                        0.01
                    );
                }
                
                step++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }
    
    private void explosionAttackPattern(AttackContext context) {
        Player player = context.getPlayer();
        Location center = context.getLocation();
        AreaAttack attack = context.getAttack();
        
        center.getWorld().spawnParticle(
            attack.getParticle(),
            center.clone().add(0, 0.5, 0),
            40,
            attack.getRadius() / 2, 0.5, attack.getRadius() / 2,
            0.1
        );
        
        center.getWorld().spawnParticle(
            Particle.EXPLOSION_NORMAL,
            center.clone().add(0, 0.5, 0),
            10,
            attack.getRadius() / 3, 0.3, attack.getRadius() / 3,
            0.05
        );
        
        new BukkitRunnable() {
            int step = 0;
            final int totalSteps = 5;
            
            @Override
            public void run() {
                if (step >= totalSteps) {
                    this.cancel();
                    return;
                }
                
                double radius = (step + 1) * (attack.getRadius() / totalSteps);
                
                for (int i = 0; i < 20; i++) {
                    double angle = i * (2 * Math.PI / 20);
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    
                    Location particleLoc = center.clone().add(x, 0.2, z);
                    center.getWorld().spawnParticle(
                        Particle.SMOKE_NORMAL,
                        particleLoc,
                        2,
                        0.05, 0.05, 0.05,
                        0.01
                    );
                }
                
                step++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }
    
    private void coneAttackPattern(AttackContext context) {
        Player player = context.getPlayer();
        Location center = context.getLocation();
        AreaAttack attack = context.getAttack();
        
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        double coneAngle = Math.PI / 2; // 90 degrees cone
        
        new BukkitRunnable() {
            int step = 0;
            final int totalSteps = 8;
            
            @Override
            public void run() {
                if (step >= totalSteps) {
                    this.cancel();
                    return;
                }
                
                double distanceStep = attack.getRadius() * ((double) (step + 1) / totalSteps);
                double angleSpread = coneAngle * ((double) (step + 1) / totalSteps);
                
                for (int i = 0; i < 6; i++) {
                    double particleAngle = -angleSpread / 2 + (angleSpread * i / 5);
                    
                    // Rotate the direction vector by particleAngle
                    double cos = Math.cos(particleAngle);
                    double sin = Math.sin(particleAngle);
                    double x = direction.getX() * cos - direction.getZ() * sin;
                    double z = direction.getX() * sin + direction.getZ() * cos;
                    Vector particleDir = new Vector(x, 0, z).normalize();
                    
                    Vector offset = particleDir.multiply(distanceStep);
                    Location particleLoc = center.clone().add(offset).add(0, 0.5, 0);
                    
                    center.getWorld().spawnParticle(
                        attack.getParticle(),
                        particleLoc,
                        3,
                        0.05, 0.05, 0.05,
                        0.01
                    );
                }
                
                step++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }
    
    private void sphereAttackPattern(AttackContext context) {
        Player player = context.getPlayer();
        Location center = context.getLocation().add(0, 1, 0);
        AreaAttack attack = context.getAttack();
        
        new BukkitRunnable() {
            int step = 0;
            final int totalSteps = 10;
            
            @Override
            public void run() {
                if (step >= totalSteps) {
                    this.cancel();
                    return;
                }
                
                double radius = attack.getRadius() * ((double) (step + 1) / totalSteps);
                
                for (int i = 0; i < 20; i++) {
                    double theta = Math.random() * Math.PI * 2; // Random angle around the sphere
                    double phi = Math.random() * Math.PI; // Random elevation angle
                    
                    double x = radius * Math.sin(phi) * Math.cos(theta);
                    double y = radius * Math.sin(phi) * Math.sin(theta);
                    double z = radius * Math.cos(phi);
                    
                    Location particleLoc = center.clone().add(x, y, z);
                    center.getWorld().spawnParticle(
                        attack.getParticle(),
                        particleLoc,
                        1,
                        0.05, 0.05, 0.05,
                        0
                    );
                }
                
                step++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }
    
    private void rainAttackPattern(AttackContext context) {
        Player player = context.getPlayer();
        Location center = context.getLocation();
        AreaAttack attack = context.getAttack();
        
        new BukkitRunnable() {
            int step = 0;
            final int totalSteps = 15;
            
            @Override
            public void run() {
                if (step >= totalSteps) {
                    this.cancel();
                    return;
                }
                
                for (int i = 0; i < 10; i++) {
                    double x = (Math.random() - 0.5) * 2 * attack.getRadius();
                    double z = (Math.random() - 0.5) * 2 * attack.getRadius();
                    
                    Location particleLoc = center.clone().add(x, 10, z);
                    
                    center.getWorld().spawnParticle(
                        attack.getParticle(),
                        particleLoc,
                        1,
                        0, 0, 0,
                        0.2
                    );
                    
                    if (Math.random() < 0.3) {
                        center.getWorld().spawnParticle(
                            Particle.FALLING_WATER,
                            particleLoc.clone().add(0, -2, 0),
                            3,
                            0.1, 0.1, 0.1,
                            0
                        );
                    }
                }
                
                step++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }
    
    private class AttackContext {
        private final Player player;
        private final Location location;
        private final AreaAttack attack;
        
        public AttackContext(Player player, Location location, AreaAttack attack) {
            this.player = player;
            this.location = location;
            this.attack = attack;
        }
        
        public Player getPlayer() {
            return player;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public AreaAttack getAttack() {
            return attack;
        }
    }
    
    public class AreaAttack {
        private final String name;
        private final double radius;
        private final double damageMultiplier;
        private final Particle particle;
        private final Sound sound;
        private final Consumer<AttackContext> attackPattern;
        
        public AreaAttack(String name, double radius, double damageMultiplier, Particle particle, 
                         Sound sound, Consumer<AttackContext> attackPattern) {
            this.name = name;
            this.radius = radius;
            this.damageMultiplier = damageMultiplier;
            this.particle = particle;
            this.sound = sound;
            this.attackPattern = attackPattern;
        }
        
        public String getName() {
            return name;
        }
        
        public double getRadius() {
            return radius;
        }
        
        public double getDamageMultiplier() {
            return damageMultiplier;
        }
        
        public Particle getParticle() {
            return particle;
        }
        
        public Sound getSound() {
            return sound;
        }
        
        public Consumer<AttackContext> getAttackPattern() {
            return attackPattern;
        }
    }
}