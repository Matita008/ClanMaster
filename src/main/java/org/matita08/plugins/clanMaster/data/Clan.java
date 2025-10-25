package org.matita08.plugins.clanMaster.data;

import lombok.Getter;
import lombok.Setter;
import org.matita08.plugins.clanMaster.storage.database.DatabaseManager;

import java.util.*;

public class Clan {
   private static final Map<String, Clan> clans = Collections.synchronizedMap(new HashMap<>());
   
   @Getter private final String name;
   @Getter private final String tag;
   @Getter private final Set<Member> members;
   @Getter @Setter private Member owner;
   @Getter @Setter private boolean membersCached = true;
   
   public static void saveAllClans() {
      synchronized (clans) {
         clans.values().forEach(DatabaseManager.getInstance()::saveClan);
      }
   }
   
   public static void purgeAllClans() { clans.clear(); }
   
   private Clan(String name, String tag, Member... members) {
      this.name = name;
      this.tag = tag;
      this.members = new HashSet<>(List.of(members));
      if(Arrays.stream(members).filter(m -> m.getRank().equals(Rank.Owner)).count() > 1)
         throw new AssertionError("The member array passed " + Arrays.toString(members) + " Does not contain a single owner");
      
      if(Arrays.stream(members).filter(m -> m.getRank().equals(Rank.Owner)).count() == 1)
         //noinspection OptionalGetWithoutIsPresent // the above check ensure that there is 1 owner
         this.owner = Arrays.stream(members).filter(m -> m.getRank().equals(Rank.Owner)).findFirst().get();
   }
   
   private Clan(String name, String tag, Member owner) {
      this.name = name;
      this.tag = tag;
      this.owner = owner;
      members = new HashSet<>();
      members.add(owner);
      owner.setClan(this);
      owner.setRank(Rank.Owner);
   }
   
   private Clan(String name, String tag) {
      this.name = name;
      this.tag = tag;
      members = new HashSet<>();
   }
   
   //TODO: implement in db
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
      //TODO: remove from database
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
   
   public void addMember(Member m){ members.add(m); }
   
   public void removeMember(Member m) { members.remove(m); }
}
