package org.matita08.plugins.clanMaster.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;
import org.matita08.plugins.clanMaster.ClanPlugin;

public class PAPI extends PlaceholderExpansion {
   @Override
   public @NotNull String getIdentifier() {
      return "ClanMaster";
   }
   
   @Override
   public @NotNull String getAuthor() {
      return ClanPlugin.getInstance().getDescription().getAuthors().toString();
   }
   
   @Override
   public @NotNull String getVersion() {
      return ClanPlugin.getInstance().getDescription().getVersion();
   }
   
   private static PAPI instance;
   
   public static synchronized void init() {
      if(instance != null) return;
      instance = new PAPI();
      instance.register();
   }
}
