package org.matita08.plugins.clanMaster.i18n;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

@RequiredArgsConstructor
public enum I18nKey {
   // PAPI
   NO_CLAN("papi.no-clan"),
   
   // General errors
   COMMAND_NO_CONSOLE("error.no-console"),
   COMMAND_WRONG_USAGE("error.wrong-usage"),
   COMMAND_NOT_FOUND("error.command-not-found"),
   PLAYER_NOT_FOUND("error.player-not-found"),
   
   // Clan errors
   NOT_IN_CLAN("clan.error.not-in-clan"),
   ALREADY_IN_CLAN("clan.error.already-in-clan"),
   CLAN_ALREADY_EXISTS("clan.error.already-exists"),
   CLAN_NOT_EXISTS("clan.error.not-exists"),
   CLAN_NAME_INVALID("clan.error.name-invalid"),
   HOME_ERROR("clan.error.home"),
   
   // Permission errors
   NO_PERMISSION("permission.no-permission"),
   NOT_ENABLED("permission.not-enabled"),
   
   // Clan actions
   CLAN_CREATED("clan.created"),
   CLAN_DISBANDED("clan.disbanded"),
   CANNOT_KICK_SELF("clan.error.cannot-kick-self"),
   CANNOT_PROMOTE_SELF("clan.error.cannot-promote-self"),
   CANNOT_DEMOTE_SELF("clan.error.cannot-demote-self"),
   MEMBER_NOT_IN_CLAN("clan.error.member-not-in-clan"),
   MEMBER_IN_SAME_CLAN("clan.error.member-in-same-clan"),
   CANNOT_PROMOTE("clan.error.cannot-promote"),
   CANNOT_DEMOTE("clan.error.cannot-demote"),
   OWNER_CANNOT_LEAVE("clan.error.owner-cannot-leave"),
   HOME_SUCCESFULL("clan.home"),
   
   // Invites
   INVITED_TO_CLAN("invite.received"),
   INVITE_ACCEPT_PROMPT("invite.accept-prompt"),
   INVITE_DECLINE_PROMPT("invite.decline-prompt"),
   INVITE_SENT("invite.sent"),
   INVITE_ALREADY_EXISTS("invite.error.already-exists"),
   NO_INVITE("invite.error.no-invite"),
   INVITE_JOINED("invite.joined"),
   INVITE_DECLINED("invite.declined"),
   INVITE_EXPIRED("invite.expired"),
   
   // Clan info
   CLAN_INFO_HEADER("info.header"),
   CLAN_INFO_NAME("info.name"),
   CLAN_INFO_TAG("info.tag"),
   CLAN_INFO_MEMBERS("info.members"),
   CLAN_INFO_OWNER("info.owner"),
   CLAN_INFO_ADMINS("info.admins"),
   CLAN_INFO_SENIORS("info.seniors"),
   CLAN_INFO_MEMBERS_LIST("info.members-list"),
   
   // Help - stored as a list in bundle
   HELP("help"),
   
   // Territory
   TERRITORY_ALREADY_CLAIMED("territory.error.already-claimed"),
   TERRITORY_CLAIMED("territory.claimed"),
   TERRITORY_UNCLAIMED("territory.deleted"),
   TERRITORY_REGION_MANAGER_ERROR("territory.error.region-manager"),
   TERRITORY_ALREADY_PRESENT("territory.error.already-have-territory"),
   TERRITORY_NOT_PRESENT("territory.error.no-territory"),
   
   // Create clan prompts
   CREATE_CLAN_INFO_PROMPT("clan.create.info-prompt"),
   ;
   
   private final String key;
   private String value;
   
   public String toString() {
      return ChatColor.translateAlternateColorCodes('&', value);
   }
   
   public void send(CommandSender s){
      s.sendMessage(toString().split("\n"));
   }
   
   public void sendFormatted(CommandSender s, String... replacement){
      s.sendMessage(toString().formatted((Object[])replacement).split("\n"));
   }
   
   public static void importBundle(YamlConfiguration bundle) {
      for (I18nKey i18nKey : values()) {
         if(!bundle.contains(i18nKey.key)) continue;
         
         // Check if it's a list or a single value
         if(bundle.isList(i18nKey.key)) {
            List<String> list = bundle.getStringList(i18nKey.key);
            i18nKey.value = String.join("\n", list);
         } else {
            i18nKey.value = bundle.getString(i18nKey.key);
         }
         
         if(i18nKey.value != null) i18nKey.value = i18nKey.value.replaceAll("\\n", "\n");
      }
   }
}
