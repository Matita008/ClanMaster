package org.matita08.plugins.clanMaster.storage.database.implementation.mariaDB;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.matita08.plugins.clanMaster.ClanPlugin;
import org.matita08.plugins.clanMaster.Constants;
import org.matita08.plugins.clanMaster.storage.database.Database;
import org.matita08.plugins.clanMaster.storage.database.InternalSQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class MariaDBDatabase extends Database {
   private final String connectionUrl;
   private final String username;
   private final String password;
   @Getter private static boolean hasFailed = false;
   @Getter private Connection connection;
   
   public MariaDBDatabase(String jdbcUrl, String username, String password) {
      this.connectionUrl = jdbcUrl;
      this.username = username;
      this.password = password;
   }
   
   @Override
   public void init() {
      try {
         Class.forName("org.mariadb.jdbc.Driver");
         connection = DriverManager.getConnection(connectionUrl, username, password);
         Statement s = connection.createStatement();
         s.execute(MariaDBQueryList.CREATE_CLAN_TABLE.getQuery());
         s.execute(MariaDBQueryList.CREATE_MEMBERS_TABLE.getQuery());
         
         ClanPlugin.logger().info(" [DB] [MariaDB] Loaded");
         shouldSave = true;
      } catch (ClassNotFoundException e) {
         ClanPlugin.logger().log(Level.SEVERE, " [MariaDB] Impossible to load MariaDB jdbc driver class\nPlease report it to " + Constants.reportUrl, e);
         ClanPlugin.logger().severe(ChatColor.RED + " [DB] [MariaDB] Disabling plugin... ");
         Bukkit.getPluginManager().disablePlugin(ClanPlugin.getInstance());
      } catch (SQLException e) {
         handleException(e);
      }
   }
   
   /**
    * Handle logging of exception and rethrows as {@link InternalSQLException} to prevent impossible situations
    *
    * @param e the exception to rethrow as unchecked
    * @throws InternalSQLException always
    */
   private static void handleException(@NotNull SQLException e) {
      if(!hasFailed) {
         ClanPlugin.logger().log(Level.SEVERE, " [DB] [MariaDB] An exception occurred while creating a connection to the database\nPlease report it at " + Constants.reportUrl, e);
         hasFailed = true;
      } else ClanPlugin.logger().log(Level.WARNING, " [DB] [MariaDB] An exception occurred while connecting to the db", e);
      throw new InternalSQLException(e);
   }
}
