package org.matita08.plugins.clanMaster.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.matita08.plugins.clanMaster.ClanPlugin;
import org.matita08.plugins.clanMaster.data.Clan;
import org.matita08.plugins.clanMaster.data.Member;
import org.matita08.plugins.clanMaster.data.Rank;
import org.matita08.plugins.clanMaster.i18n.I18nKey;

import java.util.Set;
import java.util.logging.Level;

import static org.matita08.plugins.clanMaster.commands.ClanCommand.checkNoConsole;

public class ClanTerritoryCommand {
   public static void claim(CommandSender sender) {
      if(checkNoConsole(sender)) return;
      
      Player player = (Player)sender;
      Member member = Member.getMember((Player)sender);
      
      if(!member.isInClan()) {
         I18nKey.NOT_IN_CLAN.send(sender);
         return;
      }
      
      if (!(member.getRank() == Rank.Owner || member.getRank() == Rank.Admin)) {
         I18nKey.NOT_ENABLED.send(sender);
         return;
      }
      
      Clan clan = member.getClan();
      
      if(clan.isTerritoryPresent()) {
         I18nKey.TERRITORY_ALREADY_CLAIMED.send(sender);
         return;
      }
      
      Chunk chunk = player.getLocation().getChunk();
      
      BlockVector3 min = BlockVector3.at(chunk.getX() * 16, 0, chunk.getZ() * 16);
      
      BlockVector3 max = BlockVector3.at((chunk.getX() * 16) + 15, player.getWorld().getMaxHeight(), (chunk.getZ() * 16) + 15);
      
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
      
      if(regions == null) {
         I18nKey.TERRITORY_REGION_MANAGER_ERROR.send(sender);
         return;
      }
      
      String regionId = clan.getName() + "+" + clan.getTag();
      if(regions.hasRegion(regionId)) {
         I18nKey.TERRITORY_ALREADY_PRESENT.send(sender);
         return;
      }
      
      for (ProtectedRegion r : regions.getRegions().values()) {
         if (regionsOverlap(r, min, max)) {
            I18nKey.TERRITORY_ALREADY_CLAIMED.send(sender);
            return;
         }
      }
      
      ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionId, min, max);
      
      // Set region owner to clan owner
      DefaultDomain owners = new DefaultDomain();
      owners.addPlayer(clan.getOwner().getId());
      region.setOwners(owners);
      
      // Add all members as region members
      DefaultDomain membersDomain = new DefaultDomain();
      clan.getMembers().forEach(m -> membersDomain.addPlayer(m.getId()));
      region.setMembers(membersDomain);
      
      // Set WorldGuard flags
      region.setFlag(Flags.BUILD, StateFlag.State.ALLOW);                // Members can build
      region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
      region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.ALLOW);
      region.setFlag(Flags.INTERACT, StateFlag.State.ALLOW);
      region.setFlag(Flags.USE, StateFlag.State.ALLOW);
      
      region.setFlag(Flags.PVP, StateFlag.State.DENY);                   // Disable PvP inside territory
      region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);         // Allow general mob spawning first
      
      region.setFlag(Flags.DENY_SPAWN, Set.of(
          BukkitAdapter.adapt(EntityType.CREEPER),
          BukkitAdapter.adapt(EntityType.ZOMBIE),
          BukkitAdapter.adapt(EntityType.SKELETON),
          BukkitAdapter.adapt(EntityType.SPIDER),
          BukkitAdapter.adapt(EntityType.CAVE_SPIDER),
          BukkitAdapter.adapt(EntityType.ENDERMAN),
          BukkitAdapter.adapt(EntityType.WITCH),
          BukkitAdapter.adapt(EntityType.PHANTOM),
          BukkitAdapter.adapt(EntityType.BLAZE),
          BukkitAdapter.adapt(EntityType.MAGMA_CUBE),
          BukkitAdapter.adapt(EntityType.HUSK),
          BukkitAdapter.adapt(EntityType.STRAY),
          BukkitAdapter.adapt(EntityType.SLIME),
          BukkitAdapter.adapt(EntityType.WITHER_SKELETON),
          BukkitAdapter.adapt(EntityType.ZOMBIE_VILLAGER)));
      
      region.setFlag(Flags.TNT, StateFlag.State.DENY);
      region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
      region.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.DENY);
      region.setFlag(Flags.FIRE_SPREAD, StateFlag.State.DENY);
      region.setFlag(Flags.LAVA_FIRE, StateFlag.State.DENY);
      region.setFlag(Flags.LIGHTER, StateFlag.State.DENY);
      region.setFlag(Flags.GHAST_FIREBALL, StateFlag.State.DENY);
      region.setFlag(Flags.ENDERDRAGON_BLOCK_DAMAGE, StateFlag.State.DENY);
      region.setFlag(Flags.PISTONS, StateFlag.State.DENY);
      region.setFlag(Flags.ENTITY_ITEM_FRAME_DESTROY, StateFlag.State.DENY);
      region.setFlag(Flags.ENTITY_PAINTING_DESTROY, StateFlag.State.DENY);
      
      regions.addRegion(region);
      clan.setTerritoryPresent(true);
      try {
         regions.save();
      } catch (StorageException e) {
         handle(e);
      }
      
      I18nKey.TERRITORY_CLAIMED.sendFormatted(sender, clan.getName());
   }
   
   public static void unclaim(CommandSender sender) {
      if(checkNoConsole(sender)) return;
      
      Player player = (Player)sender;
      Member member = Member.getMember((Player)sender);
      
      if(!member.isInClan()) {
         I18nKey.NOT_IN_CLAN.send(sender);
         return;
      }
      
      if (!(member.getRank() == Rank.Owner || member.getRank() == Rank.Admin)) {
         I18nKey.NOT_ENABLED.send(sender);
         return;
      }
      
      Clan clan = member.getClan();
      
      if(!clan.isTerritoryPresent()) {
         I18nKey.TERRITORY_NOT_PRESENT.send(sender);
         return;
      }
      
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
      
      if(regions == null) {
         I18nKey.TERRITORY_REGION_MANAGER_ERROR.send(sender);
         return;
      }
      
      String regionId = clan.getName() + "+" + clan.getTag();
      if(!regions.hasRegion(regionId)) {
         I18nKey.TERRITORY_NOT_PRESENT.send(sender);
         return;
      }
      
      regions.removeRegion(regionId);
      try {
         regions.save();
      } catch (StorageException e) {
         handle(e);
      }
      
      clan.setTerritoryPresent(false);
      I18nKey.TERRITORY_UNCLAIMED.send(sender);
   }
   
   private static boolean regionsOverlap(ProtectedRegion existing, BlockVector3 min, BlockVector3 max) {
      BlockVector3 eMin = existing.getMinimumPoint();
      BlockVector3 eMax = existing.getMaximumPoint();
      
      // Bounding box intersection check (no overlap allowed)
      return !(eMax.getBlockX() < min.getBlockX() ||
          eMin.getBlockX() > max.getBlockX() ||
          eMax.getBlockZ() < min.getBlockZ() ||
          eMin.getBlockZ() > max.getBlockZ());
   }
   
   private static void handle(StorageException e) {
      ClanPlugin.logger().log(Level.SEVERE, "An exception was thrown by WorldGuard while saving", e);
   }
   
   
}
