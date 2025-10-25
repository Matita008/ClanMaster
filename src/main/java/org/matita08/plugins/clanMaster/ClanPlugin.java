package org.matita08.plugins.clanMaster;

import com.google.common.base.Verify;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.matita08.plugins.clanMaster.commands.ClanCommand;
import org.matita08.plugins.clanMaster.storage.database.DatabaseManager;
import org.matita08.plugins.clanMaster.utils.PAPI;

import java.io.File;
import java.util.logging.Logger;

public class ClanPlugin extends JavaPlugin {
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
      
      Constants.init(configs);
      
      if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         PAPI.init();
      }
      
      ClanCommand cc = new ClanCommand();
      Verify.verifyNotNull(getCommand("clan")).setExecutor(cc);
      Verify.verifyNotNull(getCommand("clan")).setTabCompleter(cc);
      
      DatabaseManager.init();
      
      LOG.info( getClass().getCanonicalName() + " Enabled ClanMaster successfully!");
   }
   
   @Override
   public void onDisable() {
      saveConfig();
      
      DatabaseManager.getInstance().close();
      
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
