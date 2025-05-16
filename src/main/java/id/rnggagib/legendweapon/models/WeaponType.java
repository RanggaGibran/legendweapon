package id.rnggagib.legendweapon.models;

import org.bukkit.Material;

public class WeaponType {
    private final String id;
    private final Material material;
    private final double attackSpeed;
    private final double baseDamage;
    
    public WeaponType(String id, Material material, double attackSpeed, double baseDamage) {
        this.id = id;
        this.material = material;
        this.attackSpeed = attackSpeed;
        this.baseDamage = baseDamage;
    }
    
    public String getId() {
        return id;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public double getAttackSpeed() {
        return attackSpeed;
    }
    
    public double getBaseDamage() {
        return baseDamage;
    }
}