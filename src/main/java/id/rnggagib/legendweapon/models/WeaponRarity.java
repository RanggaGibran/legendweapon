package id.rnggagib.legendweapon.models;

public enum WeaponRarity {
    COMMON(1),
    UNCOMMON(2),
    RARE(3),
    EPIC(4),
    LEGENDARY(5),
    MYTHIC(6);
    
    private final int level;
    
    WeaponRarity(int level) {
        this.level = level;
    }
    
    public int getLevel() {
        return level;
    }
}