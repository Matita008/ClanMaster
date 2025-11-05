package org.matita08.plugins.clanMaster.storage.database.implementation.mariaDB;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.matita08.plugins.clanMaster.storage.database.QueryList;

@RequiredArgsConstructor
public enum MariaDBQueryList {
   CREATE_CLAN_TABLE(QueryList.CREATE_CLAN_TABLE.format("INT AUTO_INCREMENT", "CHAR(36)")),
   CREATE_MEMBERS_TABLE(QueryList.CREATE_MEMBERS_TABLE.format("INT AUTO_INCREMENT", "CHAR(36)")),
   ;
   
   @Getter private final String query;
   
}
