package org.matita08.plugins.clanMaster;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Constants {
   public static final String reportUrl = "https://github.com/Matita008/ClanMaster/issues/new/choose";
   @Getter private static String databasePrefix = "Clan_";
   
   public static void init(YamlConfiguration config) {
      if(config.contains("database.prefix")) databasePrefix = config.getString("database.prefix");
   }
}
