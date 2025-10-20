package org.matita08.plugins.clanMaster.storage.implementation.h2;

import java.sql.Connection;

public interface StatementProvider {
   void run(Connection connection);
}
