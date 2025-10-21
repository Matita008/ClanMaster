package org.matita08.plugins.clanMaster.data;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Clan {
   @Getter private final String name;
   @Getter private final String tag;
   private final Set<Member> members;
   @Getter private Member owner;
   
   public Clan(String name, String tag, Member... members) {
      this.name = name;
      this.tag = tag;
      this.members = new HashSet<>(List.of(members));
      if(Arrays.stream(members).filter(m -> m.getRank().equals(Rank.Owner)).count() !=1 )
         throw new AssertionError("The member array passed " + Arrays.toString(members) + " Does not contain a single owner");
      
      //noinspection OptionalGetWithoutIsPresent // the above check ensure that there is 1 owner
      this.owner = Arrays.stream(members).filter(m -> m.getRank().equals(Rank.Owner)).findFirst().get();
   }
   
   public Clan(String name, String tag, Member owner) {
      this.name = name;
      this.tag = tag;
      this.owner = owner;
      members = new HashSet<>();
   }
   
   public void addMember(Member m){ members.add(m); }
   
   public void removeMember(Member m) { members.remove(m); }
   
}
