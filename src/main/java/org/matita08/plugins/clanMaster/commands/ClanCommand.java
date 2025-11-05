package org.matita08.plugins.clanMaster.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.matita08.plugins.clanMaster.data.Clan;
import org.matita08.plugins.clanMaster.data.Member;
import org.matita08.plugins.clanMaster.data.Rank;
import org.matita08.plugins.clanMaster.i18n.I18nKey;
import org.matita08.plugins.clanMaster.utils.PermsHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

import static org.matita08.plugins.clanMaster.commands.ClanCommandExtension.*;
import static org.matita08.plugins.clanMaster.commands.ClanTerritoryCommand.claim;
import static org.matita08.plugins.clanMaster.commands.ClanTerritoryCommand.unclaim;

public class ClanCommand implements CommandExecutor, TabCompleter {
   
   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if(args.length == 0){
         helpClan(sender);
         return true;
      }
      
      switch(args[0].toLowerCase(Locale.ROOT)) {
         case "help", "h" -> helpClan(sender);
         case "create" -> createClan(sender, args);
         case "disband" -> disbandClan(sender);
         case "info" -> clanInfo(sender, args);
         case "promote" -> changeRank(sender, args, Rank::getNextRank, I18nKey.CANNOT_PROMOTE_SELF, I18nKey.CANNOT_PROMOTE);
         case "demote" -> changeRank(sender, args, Rank::getPreviousRank, I18nKey.CANNOT_DEMOTE_SELF, I18nKey.CANNOT_DEMOTE);
         case "chat" -> Member.getMember((Player)sender).getClan().message(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
         case "leave" -> leaveClan(sender);
         case "invite" -> inviteClan(sender, args);
         case "accept" -> acknowledgeInvite(sender, true);
         case "decline" -> acknowledgeInvite(sender, false);
         case "kick" -> kickMember(sender, args);
         case "home" -> home(sender);
         case "sethome" -> setHome(sender);
         case "claim" -> claim(sender);
         case "unclaim" -> unclaim(sender);
         default -> {
            I18nKey.COMMAND_NOT_FOUND.sendFormatted(sender, args[0]);
            helpClan(sender);
         }
      }
      return true;
   }
   
   private static void kickMember(@NotNull CommandSender sender, @NotNull String[] args) {
      if(checkNoConsole(sender) || checkLength(sender, args, 2)) return;
      
      Member admin = Member.getMember((Player) sender);
      if(!admin.isInClan()) {
          I18nKey.NOT_IN_CLAN.send(sender);
         return;
      }
      if(!(admin.getRank() == Rank.Admin || admin.getRank() == Rank.Owner)) {
          I18nKey.NOT_ENABLED.send(sender);
      }
      
      Player player = Bukkit.getPlayer(args[1]);
      if(player == null) {
          I18nKey.PLAYER_NOT_FOUND.send(sender);
         return;
      }
      if(player.getUniqueId().equals(admin.getId())) {
         I18nKey.CANNOT_KICK_SELF.send(sender);
         return;
      }
      
      Member member = Member.getMember(player);
      if(!member.isPartOf(admin.getClan())) {
         I18nKey.MEMBER_NOT_IN_CLAN.send(sender);
      }
      
      member.setClan(null);
      admin.getClan().removeMember(member);
   }
   
   public static void createClan(CommandSender sender, String[] args) {
      if(checkNoConsole(sender) || checkLength(sender, args, 3)) return;
      Player p = (Player)sender;
      
      if(!PermsHelper.hasPermission(p, "clans.create")) {
         I18nKey.NO_PERMISSION.send(sender);
         return;
      }
      
      String name = args[1];
      String tag = args[2];
      
      if(name.contains(" ") || name.contains("+")) {
         I18nKey.CLAN_NAME_INVALID.send(sender);
         return;
      }
      if(Clan.getClan(name) != null) {
         I18nKey.CLAN_ALREADY_EXISTS.sendFormatted(sender, name);
         return;
      }
      
      Member m = Member.getMember(p);
      
      if(m.isInClan()) {
         I18nKey.ALREADY_IN_CLAN.send(sender);
         return;
      }
      Clan.createClan(name, tag, m);
      
      PermsHelper.addPermission(p, "clans.owner");
      
      I18nKey.CLAN_CREATED.sendFormatted(sender, name);
   }
   
   public static void clanInfo(CommandSender sender, String[] args) {
      if(checkLength(sender, args, 2)) return;
      String clanName = args[1];
      Clan clan = Clan.getClan(clanName);
      if(clan == null) {
         I18nKey.CLAN_NOT_EXISTS.sendFormatted(sender, clanName);
         return;
      }
      sender.sendMessage(ChatColor.GREEN + "Clan " + clanName + " info: ");
      
      sender.sendMessage(ChatColor.DARK_GREEN + "Name: " + clan.getName());
      sender.sendMessage(ChatColor.DARK_GREEN + "Tag: " + clan.getTag());
      
      sender.sendMessage(ChatColor.DARK_GREEN + "Members: " + clan.getMembers().size());
      sender.sendMessage(ChatColor.GOLD + "Owner: " + clan.getOwner().getName());
      
      sender.sendMessage(ChatColor.LIGHT_PURPLE + "Admins: ");
      clan.getMembers().stream().filter(m -> m.getRank().equals(Rank.Admin)).forEach(m -> sender.sendMessage(" - " + m.getName()));
      
      sender.sendMessage(ChatColor.DARK_BLUE + "Seniors: ");
      clan.getMembers().stream().filter(m -> m.getRank().equals(Rank.Senior)).forEach(m -> sender.sendMessage(" - " + m.getName()));
      
      sender.sendMessage(ChatColor.YELLOW + "Members: ");
      clan.getMembers().stream().filter(m -> m.getRank().equals(Rank.Member)).forEach(m -> sender.sendMessage(" - " + m.getName()));
   }
   
   public static void changeRank(CommandSender sender, String[] args, UnaryOperator<Rank> transformer, I18nKey selfError, I18nKey error) {
      if(checkNoConsole(sender) || checkLength(sender, args, 2)) return;
      
      Member admin = Member.getMember((Player) sender);
      
      if(!admin.isInClan()) {
          I18nKey.NOT_IN_CLAN.send(sender);
         return;
      }
      if(!(admin.getRank() == Rank.Admin || admin.getRank() == Rank.Owner)) {
          I18nKey.NOT_ENABLED.send(sender);
         return;
      }
      
      Player player = Bukkit.getPlayer(args[1]);
      if(player == null) {
          I18nKey.PLAYER_NOT_FOUND.send(sender);
         return;
      }
      if(player.getUniqueId().equals(admin.getId())) {
         selfError.send(sender);
         return;
      }
      
      Member member = Member.getMember(player);
      if(!member.isPartOf(admin.getClan())) {
         I18nKey.MEMBER_NOT_IN_CLAN.send(sender);
         return;
      }
      
      Rank newRank = transformer.apply(member.getRank());
      if(newRank == null) {
         error.send(sender);
         return;
      }
      PermsHelper.removePermission(player, member.getRank().getPermission());
      member.setRank(newRank);
      PermsHelper.addPermission(player, newRank.getPermission());
   }
   
   public static void disbandClan(CommandSender sender) {
      if(checkNoConsole(sender)) return;
      
      Member admin = Member.getMember((Player) sender);
      
      if(!admin.isInClan()) {
         I18nKey.NOT_IN_CLAN.send(sender);
         return;
      }
      
      if(!(admin.getRank() == Rank.Owner)) {
          I18nKey.NOT_ENABLED.send(sender);
         return;
      }
      
      Clan clan = admin.getClan();
      clan.getMembers().forEach(m -> m.setClan(null));
      clan.removeMember(admin);
      Clan.removeClan(clan);
      
      I18nKey.CLAN_DISBANDED.send(sender);
   }
   
   public static void leaveClan(CommandSender sender) {
      if(checkNoConsole(sender)) return;
      
      Member member = Member.getMember((Player)sender);
      if(!member.isInClan()) {
         I18nKey.NOT_IN_CLAN.send(sender);
         return;
      }
      if(member.getRank() == Rank.Owner) {
         I18nKey.OWNER_CANNOT_LEAVE.send(sender);
         return;
      }
      
      member.getClan().removeMember(member);
   }
   
   public static void helpClan(CommandSender sender){
      I18nKey.HELP.send(sender);
      if(sender instanceof Player p) {
         Member.getMember(p).sendMessage(ChatColor.UNDERLINE + "Report an issue" , "https://github.com/Matita08/ClanMaster/issues/new", ClickEvent.Action.OPEN_URL);
      } else sender.sendMessage(ChatColor.BLUE + "https://github.com/Matita08/ClanMaster");
   }
   
   public static boolean checkNoConsole(CommandSender sender){
      if(sender instanceof Player) return false;
      I18nKey.COMMAND_NO_CONSOLE.send(sender);
      return true;
   }
   
   public static boolean checkLength(CommandSender sender, String[] args, int length) {
      if(args.length == length) return false;
      I18nKey.COMMAND_WRONG_USAGE.send(sender);
      return true;
   }
   
   @Override
   public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      //TODO: can be improved a lot
      if(sender instanceof Player) return List.of("help", "create", "disband", "info", "promote", "demote", "chat", "leave");
      return List.of("help", "info");
   }
}
