package org.matita08.plugins.clanMaster.storage.database;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.matita08.plugins.clanMaster.data.Clan;
import org.matita08.plugins.clanMaster.storage.StorageMethod;
import org.matita08.plugins.clanMaster.storage.database.implementation.h2.Query;
import org.matita08.plugins.clanMaster.storage.database.implementation.h2.Update;

import java.sql.ResultSet;

/**
 * Parent class for all database types
 */
public abstract class Database extends StorageMethod {
   
   @Override
   public void addClan(Clan clan) { execute(addClanImp(clan)); }
   
   public abstract @NonNull ResultSet execute(@NotNull Query query);
   
   public abstract void execute(@NotNull Update... updates);
   
   protected abstract Update addClanImp(Clan clan);
   
}
