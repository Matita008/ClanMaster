package org.matita08.plugins.clanMaster.storage.database.implementation.h2;

import java.sql.Statement;

public interface Update {
   void execute(Statement statement);
   
   default Update add(Update other) { return stmt -> {execute(stmt); other.execute(stmt);};}
}
