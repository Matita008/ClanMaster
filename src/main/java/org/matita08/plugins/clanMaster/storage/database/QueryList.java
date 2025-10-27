package org.matita08.plugins.clanMaster.storage.database;

import org.matita08.plugins.clanMaster.Constants;

//cid = Clan ID (name-tag)
//name and tag must be only [a-zA-Z0-9]+

public record QueryList(String query) {
   //     General Query
   public static final QueryList CREATE_TABLE = new QueryList("CREATE TABLE IF NOT EXISTS %s (%s);");
   public static final QueryList INSERT = new QueryList("INSERT INTO %s (%s) VALUES (%s);");
   public static final QueryList SELECT = new QueryList("SELECT * FROM %s WHERE %s;");
   public static final QueryList UPDATE = new QueryList("UPDATE %s SET %s WHERE %s;");
   public static final QueryList DELETE = new QueryList("DELETE FROM %s WHERE %s;");
   
   //     Query parameter specifications
   public static final QueryList CLANS_TABLE_NAME = new QueryList(Constants.getDatabasePrefix() + "clans");
   // %1 auto increment syntax, %2 UUID
   public static final QueryList CLANS_TABLE_ARGS = new QueryList("id %s PRIMARY KEY, cid VARCHAR(255) NOT NULL, name VARCHAR(253) NOT NULL, tag VARCHAR(10)," +
       " x1 INTEGER, y1 INTEGER, z1 INTEGER, x2 INTEGER, y2 INTEGER, z2 INTEGER, xh INTEGER, yh INTEGER, zh INTEGER, owner %s NOT NULL");
   public static final QueryList MEMBERS_TABLE_NAME = new QueryList(Constants.getDatabasePrefix() + "players");
   // %1 auto increment syntax, %2 UUID
   public static final QueryList MEMBERS_TABLE_ARGS = new QueryList("id %s PRIMARY KEY, uuid %s NOT NULL, cid VARCHAR(255), role INTEGER, perms INTEGER");
   
   //     Query ready
   public static final QueryList CREATE_CLAN_TABLE = new QueryList(CREATE_TABLE.format(CLANS_TABLE_NAME.query, CLANS_TABLE_ARGS.query));
   public static final QueryList CREATE_MEMBERS_TABLE = new QueryList(CREATE_TABLE.format(MEMBERS_TABLE_NAME.query, MEMBERS_TABLE_ARGS.query));
   
   public static final QueryList ADD_CLAN = new QueryList(INSERT.format(CLANS_TABLE_NAME.query, "cid, name, tag, owner", "?, ?, ?, ?"));
   public static final QueryList ADD_MEMBER = new QueryList(INSERT.format(MEMBERS_TABLE_NAME.query, "uuid, cid, role", "?, ?, ?"));
   
   // %1 condition, use 1=1 to short-circuit
   public static final QueryList GET_CLAN = new QueryList(SELECT.format(CLANS_TABLE_NAME.query, "%s"));
   public static final QueryList GET_CLAN_BY_CID = new QueryList(GET_CLAN.format("cid = ?"));
   public static final QueryList GET_CLAN_BY_NAME = new QueryList(GET_CLAN.format("name = ?"));
   public static final QueryList GET_MEMBER = new QueryList(SELECT.format(MEMBERS_TABLE_NAME.query, "%s"));
   public static final QueryList GET_MEMBER_BY_UUID = new QueryList(GET_MEMBER.format("uuid = ?"));
   public static final QueryList GET_MEMBER_BY_CID = new QueryList(GET_MEMBER.format("cid = ?"));
   
   public static final QueryList DELETE_CLAN = new QueryList(DELETE.format(CLANS_TABLE_NAME.query, "cid = ?"));
   public static final QueryList DELETE_MEMBER = new QueryList(DELETE.format(MEMBERS_TABLE_NAME.query, "uuid = ?"));
   
   public static final QueryList UPDATE_CLAN = new QueryList(UPDATE.format(CLANS_TABLE_NAME.query, "name = ?, tag = ?, owner = ?", "cid = ?"));
   public static final QueryList UPDATE_CLAN_HOME = new QueryList(UPDATE.format(CLANS_TABLE_NAME.query, "xh = ?, yh = ?, zh = ?", "cid = ?"));
   public static final QueryList UPDATE_CLAN_TERRITORY = new QueryList(UPDATE.format(CLANS_TABLE_NAME.query, "x1 = ?, y1 = ?, z1 = ?, x2 = ?, y2 = ?, z2 = ?", "cid = ?"));
   public static final QueryList UPDATE_MEMBER = new QueryList(UPDATE.format(MEMBERS_TABLE_NAME.query, "cid = ?, role = ?, perms = ?", "uuid = ?"));
   
   public String format(String... args) {
      return String.format(query, (Object[])args);
   }
}
