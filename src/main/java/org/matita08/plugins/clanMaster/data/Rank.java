package org.matita08.plugins.clanMaster.data;

public enum Rank {
   Owner,
   Admin,
   Senior,
   Member,
   None;
   
   public static Rank getRank(int pos) {
      return values()[pos];
   }
   
   public static Rank getNextRank(Rank rank) {
      return switch(rank) {
         case None -> None;
         case Member -> Senior;
         case Senior -> Admin;
         default -> null;
      };
   }
   
   public static Rank getPreviousRank(Rank rank) {
      return switch(rank) {
         case Admin -> Senior;
         case Senior -> Member;
         case None -> None;
         default -> null;
      };
   }
}
