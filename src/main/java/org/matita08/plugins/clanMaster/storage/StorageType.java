package org.matita08.plugins.clanMaster.storage;

import org.matita08.plugins.clanMaster.storage.database.DatabaseManager;

public enum StorageType {
   LocalH2(DatabaseManager::createLocalH2,"H2",  "Local"),
   RemoteH2(DatabaseManager::createRemoteH2),
   MariaDB(DatabaseManager::createMariaDB),
   ;
   
   public final StorageBuilder builder;
   private final String[] aliases;
   
   StorageType(StorageBuilder b){
      builder = b;
      aliases = new String[0];
   }
   
   StorageType(StorageBuilder b, String... aliases) {
      builder = b;
      this.aliases = aliases;
   }
   
   public static StorageType get(String name) {
      if(name == null || name.isBlank()) return LocalH2;
      
      name = name.trim();
      
      for (StorageType db : values()) {
         if(db.name().equalsIgnoreCase(name)) return db;
         for (String alias : db.aliases) {
            if(alias.equalsIgnoreCase(name)) return db;
         }
      }
      
      return LocalH2;
   }
}
