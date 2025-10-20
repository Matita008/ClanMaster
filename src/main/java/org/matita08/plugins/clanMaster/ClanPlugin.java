package org.matita08.plugins.clanMaster;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.matita08.plugins.clanMaster.storage.DatabaseManager;
import org.matita08.plugins.clanMaster.utils.PAPI;

import java.io.File;
import java.util.logging.Logger;

public final class ClanPlugin extends JavaPlugin {
   private static Logger LOG;
   @Getter
   private static ClanPlugin instance;
   @Getter
   private static YamlConfiguration config;
   @Getter
   private static File dataDir;
   
   public static @NotNull Logger logger() {
      return LOG;
   }
   
   @Override
   public void onEnable() {
      instance = this;
      config = (YamlConfiguration) super.getConfig();
      LOG = getLogger();
      dataDir = getDataFolder();
      
      if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         PAPI.init();
      }
      
      DatabaseManager.init();
   }
   
   @Override
   public void onDisable() {
      instance = null;
      saveConfig();
   }
}
