package org.matita08.plugins.clanMaster.storage;

public class InternalSQLException extends RuntimeException {
   
   public InternalSQLException(String message) {
      super(message);
   }
   
   public InternalSQLException(Throwable cause) {
      super(cause);
   }
   
   public InternalSQLException(String message, Throwable cause) {
      super(message, cause);
   }
}
