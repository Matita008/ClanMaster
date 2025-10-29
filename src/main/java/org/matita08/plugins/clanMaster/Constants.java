package org.matita08.plugins.clanMaster;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.matita08.plugins.clanMaster.i18n.I18nKey;

import java.io.File;
import java.io.InputStreamReader;
import java.util.logging.Level;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class Constants {
   public static final String reportUrl = "https://github.com/Matita008/ClanMaster/issues/new/choose";
   @Getter private static String databasePrefix = "Clan_";
   @Getter private static String databaseUser = "root";
   @Getter private static String databasePassword = "";
   @Getter private static String databaseName = "clanmaster";
   @Getter private static int databasePort = 3306;
   @Getter private static int inviteExpirationTime = 20 * 60 * 2;// 2 minutes
   
   public static void init(YamlConfiguration config) {
      if(config.contains("database")) {
         if(config.contains("database.username")) databaseUser = config.getString("database.username");
         if(config.contains("database.password")) databasePassword = config.getString("database.password");
         if(config.contains("database.dbname")) databaseName = config.getString("database.dbname");
         if(config.contains("database.port")) databasePort = config.getInt("database.port");
         if(config.contains("database.prefix")) databasePrefix = config.getString("database.prefix");
      }
      if(config.contains("inviteExpirationTime")) inviteExpirationTime = config.getInt("inviteExpirationTime");
      
      ClanPlugin.getInstance().saveResource("translations\\bundle.properties", false);
      
      YamlConfiguration defaultBundle = new YamlConfiguration();
      try {
         defaultBundle.load(new InputStreamReader(ClanPlugin.getInstance().getResource("translations\\bundle.properties")));
      } catch (Throwable t) {
         ClanPlugin.logger().log(Level.SEVERE, "An error occurred while loading default translation", t);
         return;
      }
      I18nKey.importBundle(defaultBundle); // Load bundled bundle (last fallback)
      
      loadBundle(""); //Load default bundle
      loadBundle(config.getString("language"));//Load current bundle, if exists
   }
   
   public static void loadBundle(String name) {
      File folder = new File(ClanPlugin.getDataDir(), "translations");
      File file = new File(folder, "bundle_" + name + ".properties");
      if(!file.exists()) return;
      YamlConfiguration bundle = new YamlConfiguration();
      try {
         bundle.load(file);
      } catch (Throwable t) {
         ClanPlugin.logger().log(Level.SEVERE, "An error occurred while loading translations", t);
         return;
      }
      I18nKey.importBundle(bundle);
   }
}
