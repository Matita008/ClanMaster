package org.matita08.plugins.clanMaster.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.matita08.plugins.clanMaster.storage.database.DatabaseManager;

import java.util.*;

public class Clan {
   private static final Map<String, Clan> clans = Collections.synchronizedMap(new HashMap<>());
   
   @Getter @NonNull private final String name;
   @Getter @NonNull private final String tag;
   @Getter @NonNull private final Set<Member> members;
   @SuppressWarnings("NotNullFieldNotInitialized") //it will always be != null
   @Getter @NonNull private Member owner;
   @Getter @Setter private boolean membersCached = true;
   
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
         //noinspection OptionalGetWithoutIsPresent // the above check ensure that there is 1 owner
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
   
   public void addMember(Member m){
      members.add(m);
      m.setClan(this);
      m.setRank(Rank.Member);
   }
   
   public void removeMember(Member m) {
      members.remove(m);
      m.setClan(null);
      m.setRank(Rank.None);
   }
   
   public void setOwner(@NonNull Member owner) {
      this.owner = owner;
      owner.setClan(this);
      owner.setRank(Rank.Owner);
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
