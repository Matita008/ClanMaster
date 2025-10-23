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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

import static org.matita08.plugins.clanMaster.commands.ClanCommandExtension.acknowledgeInvite;
import static org.matita08.plugins.clanMaster.commands.ClanCommandExtension.inviteClan;

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
         case "promote" -> changeRank(sender, args, Rank::getNextRank, "promote");
         case "demote" -> changeRank(sender, args, Rank::getPreviousRank, "demote");
         case "chat" -> Member.getMember((Player)sender).getClan().message(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
         case "leave" -> leaveClan(sender);
         case "invite" -> inviteClan(sender, args);
         case "accept" -> acknowledgeInvite(sender, true);
         case "decline" -> acknowledgeInvite(sender, false);
         case "kick" -> kickMember(sender, args);
         default -> {
            sender.sendMessage(ChatColor.RED + "/clan " + args[0] + " not found");
            helpClan(sender);
         }
      }
      return true;
   }
   
   private static void kickMember(@NotNull CommandSender sender, @NotNull String[] args) {
      if(checkNoConsole(sender) || checkLength(sender, args, 2)) return;
      
      Member admin = Member.getMember((Player) sender);
      if(!admin.isInClan()) {
         sender.sendMessage(ChatColor.DARK_RED + "You are not in a clan!");
         return;
      }
      if(!(admin.getRank() == Rank.Admin || admin.getRank() == Rank.Owner)) {
         sender.sendMessage(ChatColor.DARK_RED + "You are not enabled to to this");
      }
      
      Player player = Bukkit.getPlayer(args[1]);
      if(player == null) {
         sender.sendMessage(ChatColor.DARK_RED + "Player not found!");
         return;
      }
      if(player.getUniqueId().equals(admin.getId())) {
         sender.sendMessage(ChatColor.DARK_RED + "You cannot kick yourself!");
         return;
      }
      
      Member member = Member.getMember(player);
      if(!member.isPartOf(admin.getClan())) {
         sender.sendMessage(ChatColor.DARK_RED + "The selected member is not in your same clan");
      }
      
      member.setClan(null);
      admin.getClan().removeMember(member);
   }
   
   public static void createClan(CommandSender sender, String[] args) {
      if(checkNoConsole(sender) || checkLength(sender, args, 3)) return;
      String name = args[1];
      String tag = args[2];
      
      if(name.contains(" ") || name.contains("+")) {
         sender.sendMessage(ChatColor.DARK_RED + "Clan name cannot contain spaces or '+'!");
         return;
      }
      if(Clan.getClan(name) != null) {
         sender.sendMessage(ChatColor.DARK_RED + "Clan " + name + " already exists!");
         sender.sendMessage(ChatColor.RED + "Do /clan info <clan name> for more info");
         return;
      }
      
      Player p = (Player) sender;
      Member m = Member.getMember(p);
      
      if(m.isInClan()) {
         sender.sendMessage(ChatColor.DARK_RED + "You are already in a clan!");
         return;
      }
      
      Clan.createClan(name, tag, m);
      p.sendMessage(ChatColor.GREEN + "Clan " + name + " has been created successfully!");
   }
   
   public static void clanInfo(CommandSender sender, String[] args) {
      String clanName = args[1];
      Clan clan = Clan.getClan(clanName);
      if(clan == null) {
         sender.sendMessage(ChatColor.DARK_RED + "Clan " + clanName + " does not exist!");
         sender.sendMessage(ChatColor.BLUE + "Do /clan create <clan name> <clan tag> to create your clan");
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
   
   public static void changeRank(CommandSender sender, String[] args, UnaryOperator<Rank> transformer, String transformerName) {
      if(checkNoConsole(sender) || checkLength(sender, args, 2)) return;
      
      Member admin = Member.getMember((Player) sender);
      
      if(!admin.isInClan()) {
         sender.sendMessage(ChatColor.DARK_RED + "You are not in a clan!");
         return;
      }
      if(!(admin.getRank() == Rank.Admin || admin.getRank() == Rank.Owner)) {
         sender.sendMessage(ChatColor.DARK_RED + "You are not enabled to to this");
         return;
      }
      
      Player player = Bukkit.getPlayer(args[1]);
      if(player == null) {
         sender.sendMessage(ChatColor.DARK_RED + "Player not found!");
         return;
      }
      if(player.getUniqueId().equals(admin.getId())) {
         sender.sendMessage(ChatColor.DARK_RED + "You cannot " + transformerName + " yourself!");
         return;
      }
      
      Member member = Member.getMember(player);
      if(!member.isPartOf(admin.getClan())) {
         sender.sendMessage(ChatColor.DARK_RED + "The selected member is not in your same clan");
         return;
      }
      
      Rank newRank = transformer.apply(member.getRank());
      if(newRank == null) {
         sender.sendMessage(ChatColor.DARK_RED + "Admins and Owners can't be " + transformerName + "d");
         return;
      }
      member.setRank(newRank);
   }
   
   public static void disbandClan(CommandSender sender) {
      if(checkNoConsole(sender)) return;
      
      Member admin = Member.getMember((Player) sender);
      
      if(!admin.isInClan()) {
         sender.sendMessage(ChatColor.DARK_RED + "You are not in a clan!");
         return;
      }
      
      if(!(admin.getRank() == Rank.Owner)) {
         sender.sendMessage(ChatColor.DARK_RED + "You are not enabled to to this");
         return;
      }
      
      admin.getClan().getMembers().forEach(m -> m.setClan(null));
      admin.getClan().removeMember(admin);
      Clan.removeClan(admin.getClan());
   }
   
   public static void leaveClan(CommandSender sender) {
      if(checkNoConsole(sender)) return;
      
      Member member = Member.getMember((Player)sender);
      if(!member.isInClan()) {
         sender.sendMessage(ChatColor.DARK_RED + "You are not in a clan!");
         return;
      }
      if(member.getRank() == Rank.Owner) {
         sender.sendMessage(ChatColor.DARK_RED + "You are the owner of the clan, you can't leave the clan!");
         sender.sendMessage(ChatColor.DARK_RED + "To disband the clan, use /clan disband");
         return;
      }
      
      member.getClan().removeMember(member);
   }
   
   /*
      Comandi Richiesti
       [X]  /clan create <nome> <tag> - Crea nuovo clan
       [X]  /clan disband - Sciogli clan (solo leader)
       [X]  /clan invite <player> - Invita giocatore
       [ ]  /clan kick <player> - Espelli membro
       [X]  /clan promote/demote <player> - Gestisci ruoli
       [X]  /clan chat <messaggio> - Chat clan
       [ ]  /clan claim - Reclama territorio attuale
       [ ]  /clan unclaim - Libera territorio
       [ ]  /clan home - Teletrasporto home
       [ ]  /clan sethome - Imposta home clan
       [x]  /clan info [clan] - Info clan
       [-]  /clan help
       
       [X]  /clan leave - Esci dal clan
    */
   public static void helpClan(CommandSender sender){
      sender.sendMessage("----- " + ChatColor.GOLD + "ClanMaster " + ChatColor.GREEN + " Master -----");
      sender.sendMessage(ChatColor.AQUA + "/clan help: shows the help guide");
      sender.sendMessage(ChatColor.AQUA + "/clan create <clan name>, <clan tag>: create a clan with the given name and tag");
      sender.sendMessage(ChatColor.AQUA + "/clan disband: disband your clan");
      sender.sendMessage(ChatColor.AQUA + "/clan promote <username>: promote a member to the next rank");
      sender.sendMessage(ChatColor.AQUA + "/clan demote <username>: demote a member to the previous rank");
      sender.sendMessage(ChatColor.AQUA + "/clan info <clan name>: shows info about the given clan");
      sender.sendMessage(ChatColor.AQUA + "/clan invite <username>: invite a player to your clan");
      sender.sendMessage(ChatColor.AQUA + "/clan kick <username>: kick a player from your clan");
      sender.sendMessage(ChatColor.AQUA + "/clan accept: accept an invitation");
      sender.sendMessage(ChatColor.AQUA + "/clan decline: decline an invitation");
   //   sender.sendMessage(ChatColor.AQUA + "/clan claim: claim your current territory");          //TODO
   //   sender.sendMessage(ChatColor.AQUA + "/clan unclaim: unclaim your current territory");      //TODO
   //   sender.sendMessage(ChatColor.AQUA + "/clan home: teleport to your clan's home");           //TODO
   //   sender.sendMessage(ChatColor.AQUA + "/clan sethome: set your clan's home");                //TODO
      sender.sendMessage(ChatColor.AQUA + "/clan leave: leave your clan");
      sender.sendMessage(ChatColor.AQUA + "/clan chat <message>: send a message to your clan");
      sender.sendMessage(ChatColor.GREEN + "----- End of help guide -----");
      sender.sendMessage(ChatColor.GREEN + "ClanMaster by Matita008");
      if(sender instanceof Player p) {
         Member.getMember(p).sendMessage("https://github.com/Matita08/ClanMaster" , "https://github.com/Matita08/ClanMaster", ClickEvent.Action.OPEN_URL);
      } else sender.sendMessage(ChatColor.BLUE + "https://github.com/Matita08/ClanMaster");
   }
   
   public static boolean checkNoConsole(CommandSender sender){
      if(sender instanceof Player) return false;
      sender.sendMessage(ChatColor.DARK_RED + "Command not available from the console");
      return true;
   }
   
   public static boolean checkLength(CommandSender sender, String[] args, int length) {
      if(args.length == length) return false;
      sender.sendMessage(ChatColor.DARK_RED + "Wrong usage\nDo /clan help for more info");
      return true;
   }
   
   @Override
   public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      //TODO: can be improved a lot
      if(sender instanceof Player) return List.of("help", "create", "disband", "info", "promote", "demote", "chat", "leave");
      return List.of("help", "info");
   }
}
