package org.matita08.plugins.clanMaster;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.matita08.plugins.clanMaster.storage.database.DatabaseManager;
import org.matita08.plugins.clanMaster.utils.PAPI;

import java.io.File;
import java.util.logging.Logger;

public final class ClanPlugin extends JavaPlugin {
   private static Logger LOG;
   @Getter
   private static ClanPlugin instance;
   @Getter
   private static YamlConfiguration configs;
   @Getter
   private static File dataDir;
   
   public static @NotNull Logger logger() {
      return LOG;
   }
   
   @Override
   public void onEnable() {
      configs = (YamlConfiguration) super.getConfig();
      
      if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         PAPI.init();
      }
      
      DatabaseManager.init();
      
      LOG.info( getClass().getCanonicalName() + " Enabled ClanMaster successfully!");
   }
   
   @Override
   public void onDisable() {
      saveConfig();
      
      LOG.info( getClass().getCanonicalName() + " Disabled ClanMaster successfully!");
   }
   
   @Override
   public void onLoad() {
      instance = this;
      LOG = getLogger();
      dataDir = getDataFolder();
      saveDefaultConfig();
      LOG.info( getClass().getCanonicalName() + " Loaded ClanMaster");
   }
}
