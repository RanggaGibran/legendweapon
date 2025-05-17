package id.rnggagib.legendweapon.combat;

import java.util.HashMap;
import java.util.Map;

public class ComboPattern {
    private final String weaponType;
    private final Map<String, ComboAction> combos;
    
    public ComboPattern(String weaponType) {
        this.weaponType = weaponType;
        this.combos = new HashMap<>();
    }
    
    public void addCombo(String comboString, ComboAction action) {
        combos.put(comboString, action);
    }
    
    public ComboAction getComboAction(String comboString) {
        for (Map.Entry<String, ComboAction> entry : combos.entrySet()) {
            if (comboString.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    public String getWeaponType() {
        return weaponType;
    }
}