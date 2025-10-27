package org.matita08.plugins.clanMaster.data;

import lombok.Getter;
import lombok.NonNull;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.matita08.plugins.clanMaster.storage.database.DatabaseManager;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;

public class Member {
   private static final ReferenceQueue<Member> refQueue = new ReferenceQueue<>();
   private static final Map<UUID, WeakReference<Member>> members = Collections.synchronizedMap(new WeakHashMap<>());
   
   @Getter @NonNull private final UUID id;
   @Getter private Clan clan;
   @Getter @NonNull private Rank rank = Rank.None;
   private  @NonNull WeakReference<OfflinePlayer> underlyingPlayer;
   
   static {
      Thread cleanupThread = new Thread(()->{
         while(!Thread.currentThread().isInterrupted()) {
            try {
               WeakReference<Member> ref = (WeakReference<Member>)refQueue.remove();
               if(ref != null) {
                  Member member = ref.get();
                  if(member != null) {
                     DatabaseManager.getInstance().saveMember(member);
                  }
               } else Thread.yield();
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }
         }
      }, "Member-Cleanup-Thread");
      cleanupThread.setDaemon(true);
      cleanupThread.start();
   }
   
   public static void saveAllMembers() {
      synchronized (members) {
         members.values().stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .forEach(m -> DatabaseManager.getInstance().saveMember(m));
      }
   }
   
   public static void clearCache() { members.clear(); }
   
   public static void purgeCache() {
      synchronized (members) {
         members.values().removeIf(ref -> ref.get() == null);
      }
   }
   
   private Member(@NonNull OfflinePlayer p){
      id = p.getUniqueId();
      members.put(p.getUniqueId(), new WeakReference<>(this, refQueue));
      underlyingPlayer = new WeakReference<>(p);
   }
   
   private Member(@NonNull UUID id, Clan clan, @NonNull Rank rank) {
      this.id = id;
      this.clan = clan;
      this.rank = rank;
      underlyingPlayer = new WeakReference<>(Bukkit.getOfflinePlayer(id));
   }
   
   public static Member getMember(OfflinePlayer p){
      WeakReference<Member> ref = members.get(p.getUniqueId());
      Member member = ref != null? ref.get() : null;
      if(member == null) {
         member = new Member(p);
         members.put(p.getUniqueId(), new WeakReference<>(member, refQueue));
      }
      return member;
   }
   
   public static Member createMember(UUID id, Clan clan, Rank rank){
      Member m = new Member(id, clan, rank);
      members.put(id, new WeakReference<>(m, refQueue));
      return m;
   }
   
   public boolean isInClan() { return clan != null; }
   public boolean isPartOf(Clan c) { return isInClan() && clan.equals(c); }
   
   public void setRank(Rank rank) {
      if(rank == null || rank == Rank.None) {
         this.rank = Rank.None;
         clan = null;
      } else this.rank = rank;
   }
   
   public void setClan(Clan clan) {
      if(clan == null) rank = Rank.None;
      else if(this.clan == null && rank == Rank.None) rank = Rank.Member;
      this.clan = clan;
   }
   
   public String getName() {
      return getPlayer().getName();
   }
   
   public boolean isCached() {
      return underlyingPlayer.get() != null;
   }
   
   public OfflinePlayer getPlayer() {
      if(!isCached()) underlyingPlayer = new WeakReference<>(Bukkit.getPlayer(id));
      return underlyingPlayer.get();
   }
   
   public OfflinePlayer getPlayerOrNull() {
      return underlyingPlayer.get();
   }
   
   public void sendMessage(String message) {
      if(!isCached()) return;
      Player p = (Player)getPlayer();
      p.sendMessage(message);
   }
   
   public void sendMessage(String message, String command) {
      sendMessage(message, command, ClickEvent.Action.RUN_COMMAND);
   }
   
   public void sendMessage(String message, String command, ClickEvent.Action action) {
      if(!isCached()) return;
      TextComponent tc = new TextComponent(TextComponent.fromLegacyText(message));
      tc.setClickEvent(new ClickEvent(action, command));
      ((Player) getPlayer()).spigot().sendMessage(tc);
   }
   
   @Override
   public final boolean equals(Object o) {
      if(this == o) return true;
      if(!(o instanceof Member member)) return false;
      
      return id.equals(member.id);
   }
   
   @Override
   public int hashCode() {
      return id.hashCode();
   }
   
   @Override
   public String toString() {
      return "org.matita08.plugins.clanMaster.data.Member{" +
          "id=" + getId() +
          ", clan=" + (getClan() == null ? "null" : getClan().getName()) +
          ", underlyingPlayer=" + (isCached() ? getPlayer() : "null") +
          '}';
   }
   
}
