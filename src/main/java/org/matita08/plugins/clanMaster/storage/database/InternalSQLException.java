package org.matita08.plugins.clanMaster.storage.database;

public class InternalSQLException extends RuntimeException {
   
   /** {@inheritDoc} */
   public InternalSQLException(String message) {
      super(message);
   }
   
   /** {@inheritDoc} */
   public InternalSQLException(Throwable cause) {
      super(cause);
   }
   
   /** {@inheritDoc} */
   public InternalSQLException(String message, Throwable cause) {
      super(message, cause);
   }
}
