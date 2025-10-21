package org.matita08.plugins.clanMaster.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class Member {
   @Getter @NonNull private final UUID id;
   @Getter private Clan clan;
   @Getter @Setter @NonNull private Rank rank;
   
   public Member(@NonNull OfflinePlayer p){
      id = p.getUniqueId();
   }
   
   public Member(@NonNull UUID id, Clan clan, @NonNull Rank rank) {
      this.id = id;
      this.clan = clan;
      this.rank = rank;
   }
   
   public boolean isInClan() { return clan != null; }
   public boolean isPartOf(Clan c) { return isInClan() && clan.equals(c); }
   
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
