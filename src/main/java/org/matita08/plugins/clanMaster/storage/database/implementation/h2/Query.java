package org.matita08.plugins.clanMaster.storage.database.implementation.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;

public interface Query {
   PreparedStatement prepare(Connection connection);
}
