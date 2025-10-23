package org.matita08.plugins.clanMaster.storage.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.matita08.plugins.clanMaster.Constants;

//cid = Clan ID (name-tag)
//name and tag must be only [a-zA-Z0-9]+

@RequiredArgsConstructor
public enum QueryList {
   //     General Query
   CREATE_TABLE("CREATE TABLE IF NOT EXISTS %s (%s);"),
   INSERT("INSERT INTO %s (%s) VALUES (%s);"),
   SELECT("SELECT * FROM %s WHERE %s;"),
   UPDATE("UPDATE %s SET %s WHERE %s;"),
   DELETE("DELETE FROM %s WHERE %s;"),
   
   //     Query parameter specifications
   CLANS_TABLE_NAME(Constants.getDatabasePrefix() + "clans"),
   // %1 auto increment syntax, %2 UUID
   CLANS_TABLE_ARGS("id %s PRIMARY KEY, cid VARCHAR(255) NOT NULL, name VARCHAR(253) NOT NULL, tag VARCHAR(10)," +
       " x1 INTEGER, y1 INTEGER, z1 INTEGER, x2 INTEGER, y2 INTEGER, z2 INTEGER, owner %s NOT NULL"),
   MEMBERS_TABLE_NAME(Constants.getDatabasePrefix() + "players"),
   // %1 auto increment syntax, %2 UUID
   MEMBERS_TABLE_ARGS("id %s PRIMARY KEY, uuid %s NOT NULL, cid VARCHAR(255), role INTEGER, perms INTEGER"),
   
   //     Query ready
   CREATE_CLAN_TABLE(CREATE_TABLE.format(CLANS_TABLE_NAME.query, CLANS_TABLE_ARGS.query)),
   CREATE_MEMBERS_TABLE(CREATE_TABLE.format(MEMBERS_TABLE_NAME.query, MEMBERS_TABLE_ARGS.query)),
   
   ADD_CLAN(INSERT.format(CLANS_TABLE_NAME.query,
       "cid, name, tag, owner",
       "?, ?, ?, ?")),
   ADD_MEMBER(INSERT.format(MEMBERS_TABLE_NAME.query,
       "uuid, cid, role",
       "?, ?, ?")),
   
   // %1 condition, use 1=1 to short-circuit
   GET_CLAN(SELECT.format(CLANS_TABLE_NAME.query, "%s")),
   GET_CLAN_BY_CID(GET_CLAN.format("cid = ?")),
   GET_CLAN_BY_NAME(GET_CLAN.format("name = ?")),
   GET_MEMBER(SELECT.format(MEMBERS_TABLE_NAME.query, "%s")),
   GET_MEMBER_BY_UUID(GET_MEMBER.format("uuid = ?")),
   ;
   
   @Getter private final String query;
   
   public String format(String... args){
      return String.format(query, (Object[])args);
   }
}
