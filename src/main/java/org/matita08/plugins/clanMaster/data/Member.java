package org.matita08.plugins.clanMaster.data;

import lombok.Getter;
import lombok.NonNull;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class Member {
   public static final Map<UUID, Member> members = Collections.synchronizedMap(new WeakHashMap<UUID, Member>());
   //public static final Map<String, UUID> nameToUUID = Collections.synchronizedMap(new WeakHashMap<String, UUID>());  //TODO: how can i do this in a good way?
   @Getter @NonNull private final UUID id;
   @Getter private Clan clan;
   @Getter @NonNull private Rank rank = Rank.None;
   private WeakReference<OfflinePlayer> underlyingPlayer;
   
   private Member(@NonNull OfflinePlayer p){
      id = p.getUniqueId();
      members.put(p.getUniqueId(), this);
      underlyingPlayer = new WeakReference<>(p);
   }
   
   private Member(@NonNull UUID id, Clan clan, @NonNull Rank rank) {
      this.id = id;
      this.clan = clan;
      this.rank = rank;
      members.put(id, this);
   }
   
   public static Member getMember(OfflinePlayer p){
      if(!members.containsKey(p.getUniqueId())) members.put(p.getUniqueId(), new Member(p));
      return members.get(p.getUniqueId());
   }
   
   public static void createMember(UUID id, Clan clan, Rank rank){
      members.put(id, new Member(id, clan, rank == null ? Rank.None : rank));
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
   
   @SuppressWarnings("DataFlowIssue") //impossible
   public String getName() {
      return Bukkit.getPlayer(id).getName();
   }
   
   public boolean isCached() {
      return underlyingPlayer.get() != null;
   }
   
   public OfflinePlayer getPlayer() {
      if(isCached()) return underlyingPlayer.get();
      return Bukkit.getPlayer(id);
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
}
