package id.rnggagib.legendweapon.combat;

import java.util.LinkedList;

public class ComboData {
    private final String weaponId;
    private final String weaponType;
    private final LinkedList<Boolean> clicks;
    private long lastClickTime;
    
    public ComboData(String weaponId, String weaponType) {
        this.weaponId = weaponId;
        this.weaponType = weaponType;
        this.clicks = new LinkedList<>();
        this.lastClickTime = System.currentTimeMillis();
    }
    
    public void addClick(boolean isLeftClick, long time) {
        clicks.add(isLeftClick);
        lastClickTime = time;
        
        // Keep only the last 5 clicks
        while (clicks.size() > 5) {
            clicks.removeFirst();
        }
    }
    
    public String getComboString() {
        StringBuilder sb = new StringBuilder();
        
        for (Boolean isLeftClick : clicks) {
            sb.append(isLeftClick ? "L" : "R");
        }
        
        return sb.toString();
    }
    
    public void resetCombo() {
        clicks.clear();
    }
    
    public String getWeaponId() {
        return weaponId;
    }
    
    public String getWeaponType() {
        return weaponType;
    }
    
    public long getLastClickTime() {
        return lastClickTime;
    }
}