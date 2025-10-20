package org.matita08.plugins.clanMaster.storage;

public enum DatabaseType {
   LocalH2(DatabaseManager::createLocalH2,"H2",  "Local"),
   RemoteH2(DatabaseManager::createRemoteH2);
   
   public final DatabaseBuilder builder;
   private final String[] aliases;
   
   DatabaseType(DatabaseBuilder b){
      builder = b;
      aliases = new String[0];
   }
   
   DatabaseType(DatabaseBuilder b, String... aliases) {
      builder = b;
      this.aliases = aliases;
   }
   
   public static DatabaseType get(String name) {
      if(name == null || name.isBlank()) return LocalH2;
      
      name = name.trim();
      
      for (DatabaseType db : values()) {
         if(db.name().equalsIgnoreCase(name)) return db;
         for (String alias : db.aliases) {
            if(alias.equalsIgnoreCase(name)) return db;
         }
      }
      
      return LocalH2;
   }
}
