package id.rnggagib.legendweapon.models;

import id.rnggagib.legendweapon.models.abilities.Ability;

import java.util.List;
import java.util.Map;

public class LegendaryWeapon {
    private final String id;
    private final String name;
    private final WeaponType type;
    private final WeaponRarity rarity;
    private final List<String> lore;
    private final Map<String, Double> attributes;
    private final Map<String, Ability> abilities;
    
    public LegendaryWeapon(String id, String name, WeaponType type, WeaponRarity rarity, 
                           List<String> lore, Map<String, Double> attributes, 
                           Map<String, Ability> abilities) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        this.lore = lore;
        this.attributes = attributes;
        this.abilities = abilities;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public WeaponType getType() {
        return type;
    }
    
    public WeaponRarity getRarity() {
        return rarity;
    }
    
    public List<String> getLore() {
        return lore;
    }
    
    public Map<String, Double> getAttributes() {
        return attributes;
    }
    
    public Map<String, Ability> getAbilities() {
        return abilities;
    }
}