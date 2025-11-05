package org.matita08.plugins.clanMaster.commands;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.matita08.plugins.clanMaster.ClanPlugin;
import org.matita08.plugins.clanMaster.Constants;
import org.matita08.plugins.clanMaster.data.Clan;
import org.matita08.plugins.clanMaster.data.Member;
import org.matita08.plugins.clanMaster.data.Rank;
import org.matita08.plugins.clanMaster.i18n.I18nKey;
import org.matita08.plugins.clanMaster.storage.database.DatabaseManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static org.matita08.plugins.clanMaster.commands.ClanCommand.checkLength;
import static org.matita08.plugins.clanMaster.commands.ClanCommand.checkNoConsole;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClanCommandExtension {
   private static final Map<Player, Invite> invites = new HashMap<>();
   
   public static void setHome(CommandSender sender) {
      if(checkNoConsole(sender)) return;
      
      Member member = Member.getMember((Player) sender);
      if(!member.isInClan()) {
         I18nKey.NOT_IN_CLAN.send(sender);
         return;
      }
      
      if(member.getRank() == Rank.Admin || member.getRank() == Rank.Owner) {
         member.getClan().setHome(((Player)sender).getLocation());
         try {
            DatabaseManager.getInstance().saveClanHome(member.getClan());
            I18nKey.HOME_SUCCESFULL.send(sender);
         } catch (Exception e) {
            member.getClan().setHome(null);
            I18nKey.HOME_ERROR.send(sender);
            ClanPlugin.logger().log(Level.WARNING, "Could n ot save clan home for " + member.getClan().getName(), e);
         }
      } else I18nKey.NOT_ENABLED.send(sender);
   }
   
   public static void home(CommandSender sender) {
      if(checkNoConsole(sender)) return;
      
      Member member = Member.getMember((Player) sender);
      if(!member.isInClan()) {
         I18nKey.NOT_IN_CLAN.send(sender);
         return;
      }
      
      if(member.getClan().getHome() == null) {
         return;
      }
      
      ((Player) sender).teleport(member.getClan().getHome());
   }
   
   public static void inviteClan(CommandSender sender, String[] args) {
      if(checkNoConsole(sender) || checkLength(sender, args, 2)) return;
      
      Member admin = Member.getMember((Player) sender);
      
      if(!admin.isInClan()) {
         sender.sendMessage(ChatColor.DARK_RED + "You are not in a clan!");
         return;
      }
      if(admin.getRank() == Rank.Member) {
         sender.sendMessage(ChatColor.DARK_RED + "You are not enabled to to this");
         return;
      }
      
      Player player = Bukkit.getPlayer(args[1]);
      if(player == null) {
         sender.sendMessage(ChatColor.DARK_RED + "Player not found!");
         return;
      }
      if(player.getUniqueId().equals(admin.getId())) {
         sender.sendMessage(ChatColor.DARK_RED + "You cannot invite yourself!");
         return;
      }
      
      if(invites.containsKey(player)) {
         sender.sendMessage(ChatColor.DARK_RED + "The selected player has already been invited to a clan!");
         return;
      }
      
      Member member = Member.getMember(player);
      if(!member.isInClan()) {
         sender.sendMessage(ChatColor.DARK_RED + "The selected member is not in your same clan");
         sender.sendMessage(ChatColor.DARK_RED + "To see the clan members, use /clan info");
      }
      
      if(member.isPartOf(admin.getClan())) {
         sender.sendMessage(ChatColor.DARK_RED + "The selected member is in your same clan");
         sender.sendMessage(ChatColor.DARK_RED + "To see the clan members, use /clan info");
         sender.sendMessage(ChatColor.DARK_RED + "To promote a member, use /clan promote <username>");
         return;
      }
      
      invites.put(player, new Invite(member, admin.getClan()));
      member.sendMessage(ChatColor.GREEN + "You have been invited to join the clan " + admin.getClan().getName() + " by " + admin.getName());
      member.sendMessage(ChatColor.GREEN + "To accept the invitation, use /clan accept", "/clan accept");
      member.sendMessage(ChatColor.RED + "To decline the invitation, use /clan decline", "/clan decline");
      sender.sendMessage(ChatColor.GREEN + "The invitation has been sent to " + player.getName());
   }
   
   public static void acknowledgeInvite(CommandSender sender, boolean state) {
      if(checkNoConsole(sender)) return;
      Player player = (Player) sender;
      Invite invite = invites.get(player);
      
      if(invite == null) {
         player.sendMessage(ChatColor.DARK_RED + "You have not been invited to a clan!");
         return;
      }
      invite.acknowledge(state);
   }
   
   @RequiredArgsConstructor
   private static class Invite {
      private final Member player;
      private final Clan clan;
      private final Date inviteDate = new Date();
      private final BukkitTask expireTask =
          Bukkit.getScheduler().runTaskLater(ClanPlugin.getInstance(), this::expire, Constants.getInviteExpirationTime());
      
      public void acknowledge(boolean accepted) {
         if(expireTask.isCancelled()) return;
         expireTask.cancel();
         invites.remove(player.getPlayer().getPlayer());
         if(accepted) {
            clan.addMember(player);
            player.sendMessage(ChatColor.GREEN + "You have joined the clan " + clan.getName());
            clan.message(ChatColor.WHITE + player.getName() + " has joined the clan " + clan.getName());
         } else player.sendMessage(ChatColor.GREEN + "You have declined the invitation");
      }
      
      private void expire() {
         if(expireTask.isCancelled()) return;
         expireTask.cancel();
         invites.remove(player.getPlayer().getPlayer());
         player.sendMessage(ChatColor.DARK_RED + "The invitation has expired!");
      }
   }
}
