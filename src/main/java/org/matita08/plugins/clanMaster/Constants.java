package org.matita08.plugins.clanMaster;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.file.YamlConfiguration;

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
   }
}
