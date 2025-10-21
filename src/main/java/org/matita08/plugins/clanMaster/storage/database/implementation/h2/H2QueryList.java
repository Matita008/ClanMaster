package org.matita08.plugins.clanMaster.storage.database.implementation.h2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.matita08.plugins.clanMaster.storage.database.QueryList;

@RequiredArgsConstructor
public enum H2QueryList {
   CREATE_CLAN_TABLE(QueryList.CREATE_CLAN_TABLE.format("INTEGER AUTO_INCREMENT", "UUID")),
   CREATE_MEMBERS_TABLE(QueryList.CREATE_MEMBERS_TABLE.format("INTEGER AUTO_INCREMENT", "UUID")),
   ;
   
   @Getter private final String query;
   
}
