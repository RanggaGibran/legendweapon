package id.rnggagib.legendweapon.models.abilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Ability {
    String getId();
    String getName();
    String getDescription();
    boolean activate(Player player, Location location);
    long getCooldown();
    boolean isPassive();
}