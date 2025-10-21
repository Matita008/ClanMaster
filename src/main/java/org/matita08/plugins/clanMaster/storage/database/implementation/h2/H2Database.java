package org.matita08.plugins.clanMaster.storage.database.implementation.h2;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.matita08.plugins.clanMaster.ClanPlugin;
import org.matita08.plugins.clanMaster.Constants;
import org.matita08.plugins.clanMaster.data.Clan;
import org.matita08.plugins.clanMaster.storage.database.Database;
import org.matita08.plugins.clanMaster.storage.database.InternalSQLException;
import org.matita08.plugins.clanMaster.storage.database.QueryList;

import java.sql.*;
import java.util.logging.Level;

public class H2Database extends Database {
   private final String connectionUrl;
   @Getter private static boolean hasFailed = false;
   @Getter private Connection connection;
   
   public H2Database(String jdbcUrl) {
      connectionUrl = jdbcUrl;
   }
   
   @Override
   public void init() {
      try {
         Class.forName("org.h2.Driver");
         connection = DriverManager.getConnection(connectionUrl);
         Statement s = connection.createStatement();
         s.execute(H2QueryList.CREATE_CLAN_TABLE.getQuery());
         s.execute(H2QueryList.CREATE_MEMBERS_TABLE.getQuery());
         
         ClanPlugin.logger().info(" [DB] [H2] Loaded");
      } catch (ClassNotFoundException e) {
         ClanPlugin.logger().log(Level.SEVERE, " [H2] Impossible to load H2 jdbc driver class\nPlease report it to " + Constants.reportUrl, e);
         ClanPlugin.logger().severe(ChatColor.RED + " [DB] [H2] Disabling plugin... ");
         Bukkit.getPluginManager().disablePlugin(ClanPlugin.getInstance());
      } catch (SQLException e) {
         handleException(e);
      }
   }
   
   @Override
   public Clan getClan(String name) {
      try {
         PreparedStatement ps = connection.prepareStatement(QueryList.GET_CLAN_BY_NAME.getQuery());
         ps.setString(1, name);
         ResultSet rs = ps.executeQuery();
         ClanPlugin.logger().info(rs.toString());
      } catch (SQLException e) {
         handleException(e);
      }
      return null;
   }
   
   @Override
   public Update addClanImp(Clan clan) {
      return s -> {
      
      };
   }
   
   @Override
   public @NonNull ResultSet execute(@NotNull Query query) {
      try {
         return query.prepare(connection).executeQuery();
      } catch (SQLException e) {
         handleException(e);
      }
      //noinspection DataFlowIssue // Unreacheable
      return null;
   }
   
   @Override
   public void execute(@NotNull Update... updates) {
      try {
         Statement statement = connection.createStatement();
         for (Update update : updates) {
            update.execute(statement);
         }
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
         ClanPlugin.logger().log(Level.SEVERE, " [DB] [H2] An exception occurred while creating a connection to the database\nPlease report it at " + Constants.reportUrl, e);
         hasFailed = true;
      } else ClanPlugin.logger().log(Level.WARNING, " [DB] [H2] An exception occurred while connecting to the db", e);
      throw new InternalSQLException(e);
   }
}
