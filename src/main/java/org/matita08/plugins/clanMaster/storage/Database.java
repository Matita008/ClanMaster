package org.matita08.plugins.clanMaster.storage;

import org.bukkit.Location;
import org.matita08.plugins.clanMaster.data.Clan;

/**
 * Parent class for all database types
 * <br /><br /><br />
 * Database structure:
 * <br /><br />
 * name and tag are only [a-zA-Z1-9]+
 * <br /><br />
 * db configs:<br />
 * Id (autoincrement) | cid (unique) [tag+'-'+name] || name | tag | spawn [{@link Location}] | owner uuid
 * <br />
 * db members
 * uId (autoincrement) | uuid || id | cid | role ?|perms?
 */
public abstract class Database {
   
   public abstract void init();
   
   public abstract Clan getClan(String name);
   
   public abstract void addClan(Clan clan);
}
