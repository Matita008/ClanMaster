package org.matita08.plugins.clanMaster.data;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.matita08.plugins.clanMaster.ClanPlugin;
import org.matita08.plugins.clanMaster.storage.database.DatabaseManager;

import java.util.*;
import java.util.logging.Level;

public class Clan {
   private static final Map<String, Clan> clans = Collections.synchronizedMap(new HashMap<>());
   
   @Getter @NonNull private final String name;
   @Getter @NonNull private final String tag;
   @Getter @NonNull private final Set<Member> members;
   @SuppressWarnings("NotNullFieldNotInitialized") //it will always be != null, except for Clan(name, tag), which is used only when fetching from db, and will be added later to avoid issues
   @Getter @NonNull private Member owner;
   @Getter @Setter private boolean membersCached = true;
   @Getter @Setter private Location home = null;
   @Getter @Setter private boolean territoryPresent = false;
   
   public static void saveAllClans() {
      synchronized (clans) {
         clans.values().forEach(DatabaseManager.getInstance()::saveClan);
      }
   }
   
   public static void purgeAllClans() { clans.clear(); }
   
   private Clan(@NonNull String name, @NonNull String tag, Member... members) {
      this.name = name;
      this.tag = tag;
      this.members = new HashSet<>(List.of(members));
      if(Arrays.stream(members).filter(m -> m.getRank().equals(Rank.Owner)).count() > 1)
         throw new AssertionError("The member array passed " + Arrays.toString(members) + " Does not contain a single owner");
      
      if(Arrays.stream(members).filter(m -> m.getRank().equals(Rank.Owner)).count() == 1)
         //noinspection OptionalGetWithoutIsPresent // the above check ensure that there is present 1 owner
         this.owner = Arrays.stream(members).filter(m -> m.getRank().equals(Rank.Owner)).findFirst().get();
      else
         throw new AssertionError("The member array passed " + Arrays.toString(members) + " Does not contain an owner");
   }
   
   private Clan(@NonNull String name, @NonNull String tag, @NonNull Member owner) {
      this.name = name;
      this.tag = tag;
      this.owner = owner;
      members = new HashSet<>();
      members.add(owner);
      owner.setClan(this);
      owner.setRank(Rank.Owner);
   }
   
   private Clan(@NonNull String name, @NonNull String tag) {
      this.name = name;
      this.tag = tag;
      members = new HashSet<>();
   }
   
   public static Clan createClan(String name, String tag, Member... members) {
      if(!clans.containsKey(name)) clans.put(name, new Clan(name, tag, members));
      return clans.get(name);
   }
   
   public static Clan createClan(String name, String tag) {
      if(!clans.containsKey(name)) clans.put(name, new Clan(name, tag));
      return clans.get(name);
   }
   
   public static Clan createClan(String name, String tag, Member owner) {
      if(!clans.containsKey(name)) clans.put(name, new Clan(name, tag, owner));
      return clans.get(name);
   }
   
   public static boolean isCached(String name) {
      return clans.containsKey(name);
   }
   
   public static Clan getClan(String clanName) {
      if(!isCached(clanName)) DatabaseManager.getInstance().fetchClan(clanName);
      return clans.get(clanName);
   }
   
   public static void removeClan(Clan clan) {
      DatabaseManager.getInstance().deleteClan(clan);
      clans.remove(clan.getName());
      for (Member member: clan.members) {
         member.setClan(null);
         member.setRank(Rank.None);
      }
   }
   
   public void message(String message) {
      members.forEach(m -> m.sendMessage("[" + tag + "] " + message));
   }
   
   public boolean isOwner(UUID id) {
      return members.stream().anyMatch(m -> m.getId().equals(id) && m.getRank() == Rank.Owner);
   }
   
   public boolean isOwner(Member m) {
      return m.isPartOf(this) && m.getRank() == Rank.Owner;
   }
   
   public boolean isMember(Member m) {
      return m.isPartOf(this) && m.getRank() != Rank.None;
   }
   
   public boolean isMember(UUID id) {
      return members.stream().anyMatch(m -> m.getId().equals(id));
   }
   
   public void addMember(Member m){
      members.add(m);
      m.setClan(this);
      m.setRank(Rank.Member);
      if(territoryPresent) {
         try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(m.getPlayer().getPlayer().getWorld()));
            ProtectedRegion region = regions.getRegion(name + "+" + tag);
            DefaultDomain members = region.getMembers();
            members.addPlayer(m.getPlayer().getUniqueId());
            region.setMembers(members);
            
            regions.save();
         } catch (Throwable t) {
            ClanPlugin.logger().log(Level.WARNING, "Failed to add territory permission to new clan member", t);
         }
      }
   }
   
   public void removeMember(Member m) {
      members.remove(m);
      m.setClan(null);
      m.setRank(Rank.None);
      if(territoryPresent) {
         try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(m.getPlayer().getPlayer().getWorld()));
            ProtectedRegion region = regions.getRegion(name + "+" + tag);
            DefaultDomain members = region.getMembers();
            members.removePlayer(m.getPlayer().getUniqueId());
            region.setMembers(members);
            
            regions.save();
         } catch (Throwable t) {
            ClanPlugin.logger().log(Level.WARNING, "Failed to remove territory permission to old clan member", t);
         }
      }
   }
   
   public void setOwner(@NonNull Member owner) {
      Player old = this.owner.getPlayer().getPlayer();
      this.owner = owner;
      owner.setClan(this);
      owner.setRank(Rank.Owner);
      if(territoryPresent) {
         try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(old.getWorld()));
            ProtectedRegion region = regions.getRegion(name + "+" + tag);
            
            DefaultDomain members = region.getMembers();
            members.addPlayer(old.getUniqueId());
            members.removePlayer(owner.getPlayer().getUniqueId());
            region.setMembers(members);
            
            DefaultDomain owners = region.getOwners();
            owners.removePlayer(old.getUniqueId());
            owners.addPlayer(owner.getPlayer().getUniqueId());
            region.setOwners(owners);
            
            regions.save();
         } catch (Throwable t) {
            ClanPlugin.logger().log(Level.WARNING, "Failed Change territory permission", t);
         }
      }
   }
   
   @Override
   public final boolean equals(Object o) {
      if(!(o instanceof Clan clan)) return false;
      return name.equals(clan.name) && tag.equals(clan.tag) && Objects.equals(members, clan.members) && owner.equals(clan.owner);
   }
   
   @Override
   public int hashCode() {
      int result = name.hashCode();
      result = 31 * result + tag.hashCode();
      return result;
   }
   
   @Override
   public String toString() {
      return "org.matita08.plugins.clanMaster.data.Clan{" +
          "name='" + getName() + '\'' +
          ", tag='" + getTag() + '\'' +
          ", members=" + getMembers().parallelStream().map(Member::getName).toList() +
          ", owner=" + (getOwner() == null ? "null" : getOwner().getName()) +
          ", membersCached=" + isMembersCached() +
          '}';
   }
}
