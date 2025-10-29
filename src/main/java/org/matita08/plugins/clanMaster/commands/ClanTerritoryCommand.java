package org.matita08.plugins.clanMaster.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.matita08.plugins.clanMaster.data.Clan;
import org.matita08.plugins.clanMaster.data.Member;

import static org.matita08.plugins.clanMaster.commands.ClanCommand.checkNoConsole;

public class ClanTerritoryCommand {
   public static void claim(CommandSender sender) {
      if(checkNoConsole(sender)) return;
      
      Player player = (Player)sender;
      Member member = Member.getMember(player);
      
      if(!member.isInClan()) {
         sender.sendMessage("You must be in a clan to claim territory!");
         return;
      }
      
      Clan clan = member.getClan();
      Chunk chunk = player.getLocation().getChunk();
      
      BlockVector3 min = BlockVector3.at(chunk.getX() * 16, 0, chunk.getZ() * 16);
      
      BlockVector3 max = BlockVector3.at((chunk.getX() * 16) + 15, player.getWorld().getMaxHeight(), (chunk.getZ() * 16) + 15);
      
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
      
      if(regions == null) {
         sender.sendMessage("Could not get region manager!");
         return;
      }
      
      String regionId = clan.getName() + "_" + clan.getTag() + "_" + chunk.getX() + "_" + chunk.getZ();
      if(regions.hasRegion(regionId)) {
         sender.sendMessage("This chunk is already claimed!");
         return;
      }
      
      ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionId, min, max);
      regions.addRegion(region);
      
      sender.sendMessage("Successfully claimed this chunk for clan " + clan.getName() + "!");
   }
}
