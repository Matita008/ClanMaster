package org.matita08.plugins.clanMaster.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.matita08.plugins.clanMaster.ClanPlugin;
import org.matita08.plugins.clanMaster.data.Member;
import org.matita08.plugins.clanMaster.i18n.I18nKey;

public class PAPI extends PlaceholderExpansion {
   static final ClanPlugin pl = ClanPlugin.getInstance();
   private static PAPI instance;
   
   @Override
   public @NotNull String getIdentifier() {
      return "clans";
   }
   
   @Override
   public @NotNull String getAuthor() {
      return pl.getDescription().getAuthors().toString();
   }
   
   @Override
   public @NotNull String getVersion() {
      return pl.getDescription().getVersion();
   }
   
   @Override
   public boolean persist() {
      return true;
   }
   
   @Override
   public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
      Member member = Member.getMember(player);
      if(member == null) return null;
      if(!member.isInClan()) return I18nKey.NO_CLAN.toString();
      
      return switch(params) {
         case "clan", "player_clan" -> member.getClan().getName();
         case "tag", "player_tag" -> member.getClan().getTag();
         case "rank", "player_rank", "role", "player_role" -> member.getRank().toString();
         case "clan_members_online", "members_online" -> member.getClan().getMembers().stream().filter(m -> m.getPlayer().isOnline()).count() + " online";
         default -> null;
      };
   }
   
   public static synchronized void init() {
      if(instance != null) return;
      instance = new PAPI();
      instance.register();
   }
}
