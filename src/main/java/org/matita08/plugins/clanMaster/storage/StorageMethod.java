package org.matita08.plugins.clanMaster.storage;

import org.matita08.plugins.clanMaster.data.Clan;

public abstract class StorageMethod {
   public abstract void init();
   
   public abstract Clan getClan(String name);
   
   public abstract void addClan(Clan clan);
}
