package id.rnggagib;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * legendweapon java plugin
 */
public class LegendWeapon extends JavaPlugin
{
  private static final Logger LOGGER=Logger.getLogger("legendweapon");

  public void onEnable()
  {
    LOGGER.info("legendweapon enabled");
  }

  public void onDisable()
  {
    LOGGER.info("legendweapon disabled");
  }
}
