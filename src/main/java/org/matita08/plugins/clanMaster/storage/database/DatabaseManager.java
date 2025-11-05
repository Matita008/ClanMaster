package org.matita08.plugins.clanMaster.storage.database;

import com.google.common.base.Verify;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.matita08.plugins.clanMaster.ClanPlugin;
import org.matita08.plugins.clanMaster.storage.StorageType;
import org.matita08.plugins.clanMaster.storage.database.implementation.h2.H2Database;
import org.matita08.plugins.clanMaster.storage.database.implementation.mariaDB.MariaDBDatabase;

import java.io.File;

public class DatabaseManager {
   
   @Getter
   private static Database instance;
   
   public static void init() {
      ConfigurationSection config = ClanPlugin.getConfigs().getConfigurationSection("database");
      instance = StorageType.get(Verify.verifyNotNull(config).getString("type","H2")).builder.get(config);
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
   
   public static MariaDBDatabase createMariaDB(ConfigurationSection config) {
      String host = Verify.verifyNotNull(config.getString("host", "localhost"));
      String port = config.getString("port", "3306");
      String database = Verify.verifyNotNull(config.getString("database"));
      String username = Verify.verifyNotNull(config.getString("username"));
      String password = Verify.verifyNotNull(config.getString("password"));
      return new MariaDBDatabase(String.format("jdbc:mariadb://%s:%s/%s", host, port, database), username, password);
   }
   
}
