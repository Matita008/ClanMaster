package org.matita08.plugins.clanMaster.storage;

import org.matita08.plugins.clanMaster.data.Clan;
import org.matita08.plugins.clanMaster.data.Member;

import java.util.concurrent.CompletableFuture;

public abstract class StorageMethod {
   protected boolean shouldSave = false;
   
   public abstract void init();
   public void close() {
      if(!shouldSave) return;
      Member.saveAllMembers();
      Clan.saveAllClans();
   }
   
   public abstract Member fetchMember(String uuid);
   
   public abstract void saveMember(Member member);
   
   public abstract Clan fetchClan(String name);
   
   public abstract void saveClan(Clan clan);
   public abstract void saveClanHome(Clan clan);
   
   public abstract void deleteClan(Clan clan);
   
   public abstract CompletableFuture<Clan> fetchFullClanAsync(String name);
}
