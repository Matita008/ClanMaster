package org.matita08.plugins.clanMaster.storage;

import com.google.common.base.Verify;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.matita08.plugins.clanMaster.ClanPlugin;
import org.matita08.plugins.clanMaster.storage.implementation.h2.H2Database;

import java.io.File;

public class DatabaseManager {
   
   @Getter
   private static Database instance;
   
   public static void init() {
      ConfigurationSection config = ClanPlugin.getConfig().getConfigurationSection("database");
      instance = DatabaseType.get(Verify.verifyNotNull(config).getString("type","H2")).builder.get(config);
      instance.init();
   }
   
   
   //-------------------------------------------------------------------------
   //            Database Builders
   //-------------------------------------------------------------------------
   
   public static H2Database createLocalH2(ConfigurationSection ignored) {
      File dir = new File(ClanPlugin.getDataDir(), "db/H2");
      return new H2Database("jdbc:h2:" + dir.getAbsolutePath());
   }
   
   public static H2Database createRemoteH2(ConfigurationSection config) {
      return new H2Database("jdbc:h2:file:" + Verify.verifyNotNull(config.get("url")));
   }
}
