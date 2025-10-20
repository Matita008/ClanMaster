package org.matita08.plugins.clanMaster.storage.implementation.h2;

import lombok.*;
import org.bukkit.*;
import org.jetbrains.annotations.NotNull;
import org.matita08.plugins.clanMaster.*;
import org.matita08.plugins.clanMaster.data.Clan;
import org.matita08.plugins.clanMaster.storage.*;

import java.sql.*;
import java.util.logging.Level;

public class H2Database extends Database {
   private final String connectionUrl;
   @Getter
   private boolean hasErrored = false;
   
   public H2Database(String jdbcUrl) {
      connectionUrl = jdbcUrl;
   }
   
   @Override
   public void init() {
      try {
         Class.forName("org.h2.Driver");
      } catch (ClassNotFoundException e) {
         ClanPlugin.logger().log(Level.SEVERE, " [H2] Impossible to load H2 jdbc driver class\nPlease report it to " + Constants.reportUrl, e);
         ClanPlugin.logger().severe(ChatColor.RED + " [H2] Disabling plugin... ");
         Bukkit.getPluginManager().disablePlugin(ClanPlugin.getInstance());
      }
   }
   @Override
   public Clan getClan(String name) {
      return null;
   }
   
   @Override
   public void addClan(Clan clan) {
   
   }
   
   @SneakyThrows(SQLException.class)
   public void executeUpdate(@NotNull StatementProvider stmt) {
      Connection conn = newConn();
      stmt.run(conn);
      conn.close();
   }
   
   private @NotNull Connection newConn(){
      try {
         return DriverManager.getConnection(connectionUrl);
      } catch (SQLException e) {
         if(!hasErrored) {
            ClanPlugin.logger().log(Level.SEVERE, " [H2] An exception occurred while creating a connection to the database\nPlease report it at " + Constants.reportUrl, e);
            hasErrored = true;
         } else ClanPlugin.logger().log(Level.WARNING, " [H2] An exception occurred while connecting to the db", e);
         throw new InternalSQLException(e);
      }
   }
}
