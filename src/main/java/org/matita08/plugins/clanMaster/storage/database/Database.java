package org.matita08.plugins.clanMaster.storage.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.matita08.plugins.clanMaster.ClanPlugin;
import org.matita08.plugins.clanMaster.data.Clan;
import org.matita08.plugins.clanMaster.data.Member;
import org.matita08.plugins.clanMaster.data.Rank;
import org.matita08.plugins.clanMaster.storage.StorageMethod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Parent class for all database types
 */
public abstract class Database extends StorageMethod {
   
   protected PreparedStatement getClansByNameStatement;
   protected PreparedStatement getClanByCIDStatement;
   protected PreparedStatement getMembersByUUIDStatement;
   protected PreparedStatement getMembersByCIDStatement;
   protected PreparedStatement addClanStatement;
   protected PreparedStatement addMemberStatement;
   protected PreparedStatement updateClanStatement;
   protected PreparedStatement updateClanHomeStatement;
   protected PreparedStatement updateMemberStatement;
   protected PreparedStatement deleteClanStatement;
   protected PreparedStatement deleteMemberStatement;
   
   public abstract Connection getConnection();
   
   @Override
   public void saveClan(Clan clan) {
      try {
         getClansByNameStatement.setString(1, clan.getName());
         ResultSet rs = getClansByNameStatement.executeQuery();
         
         if(rs.next()) {
            updateClanStatement.setString(1, clan.getTag());
            updateClanStatement.setString(2, clan.getName());
            updateClanStatement.executeUpdate();
         } else {
            String cid = clan.getName() + "+" + clan.getTag();
            addClanStatement.setString(1, cid);
            addClanStatement.setString(2, clan.getName());
            addClanStatement.setString(3, clan.getTag());
            addClanStatement.setString(4, clan.getOwner().getId().toString());
            addClanStatement.executeUpdate();
         }
      } catch (SQLException e) {
         log("An exception occurred while saving clan " + clan.getName() + "(" + clan.getName() + "+" + clan.getTag() + ") to the db", e);
         throw new InternalSQLException(e);
      }
   }
   
   @Override
   public void saveClanHome(Clan clan) {
      Location home = clan.getHome();
      try {
         if(home == null) {
            updateClanHomeStatement.setInt(1, 0);
            updateClanHomeStatement.setNull(2, java.sql.Types.VARCHAR);
            updateClanHomeStatement.setNull(3, java.sql.Types.INTEGER);
            updateClanHomeStatement.setNull(4, java.sql.Types.INTEGER);
            updateClanHomeStatement.setNull(5, java.sql.Types.INTEGER);
            updateClanHomeStatement.setNull(6, java.sql.Types.INTEGER);
            updateClanHomeStatement.setNull(7, java.sql.Types.INTEGER);
         } else {
            updateClanHomeStatement.setInt(1, 1);
            updateClanHomeStatement.setString(2, home.getWorld().getName());
            updateClanHomeStatement.setInt(3, home.getBlockX());
            updateClanHomeStatement.setInt(4, home.getBlockY());
            updateClanHomeStatement.setInt(5, home.getBlockZ());
            updateClanHomeStatement.setInt(6, (int)(home.getYaw() * 100));
            updateClanHomeStatement.setInt(7, (int)(home.getPitch() * 100));
         }
         updateClanHomeStatement.setString(8, clan.getName() + "+" + clan.getTag());
         updateClanHomeStatement.executeUpdate();
      } catch (SQLException e) {
         log("An exception occurred while saving home location " + home + " for clan " +
             clan.getName() + " to the db", e);
         throw new InternalSQLException(e);
      }
   }
   
   @Override
   public Clan fetchClan(String name) {
      try {
         getClansByNameStatement.setString(1, name);
         ResultSet rs = getClansByNameStatement.executeQuery();
         
         if(rs.next()) {
            String clanName = rs.getString("name");
            String tag = rs.getString("tag");
            Clan clan = Clan.createClan(clanName, tag, fetchMember(rs.getString("owner")));
            if(rs.getInt("home") == 1) {
               float yaw = rs.getInt("jh")/100f;
               float pitch = rs.getInt("ph")/100f;
               Location loc = new Location(Bukkit.getWorld(rs.getString("wh")), rs.getInt("xh"), rs.getInt("yh"), rs.getInt("zh"), yaw, pitch);
               clan.setHome(loc);
            }
            return clan;
         }
      } catch (SQLException e) {
         log("An exception occurred while fetching clan " + name + " from the db", e);
         throw new InternalSQLException(e);
      }
      return null;
   }
   
   @Override
   public CompletableFuture<Clan> fetchFullClanAsync(String name) {
      CompletableFuture<Clan> future = new CompletableFuture<>();
      Bukkit.getScheduler().runTaskAsynchronously(ClanPlugin.getInstance(), () -> fetchFullClanImpl(name, future));
      return future;
   }
   
   public void fetchFullClanImpl(String name, CompletableFuture<Clan> future) {
      if(Clan.isCached(name) && Clan.getClan(name).isMembersCached()) {
         future.complete(Clan.getClan(name));
         return;
      }
      Clan clan;
      if(Clan.isCached(name)) clan = Clan.getClan(name);
      else clan = fetchClan(name);
      String cid = clan.getName() + "+" + clan.getTag();
      
      try {
         getMembersByCIDStatement.setString(1, cid);
         ResultSet rs = getMembersByCIDStatement.executeQuery();
         while(rs.next()) {
            String uuid = rs.getString("id");
            Rank rank = Rank.valueOf(rs.getString("rank"));
            Member member = Member.createMember(UUID.fromString(uuid), clan, rank);
            clan.addMember(member);
         }
         clan.setMembersCached(true);
         future.complete(clan);
      } catch (SQLException e) {
         log("An exception occurred while fetching members of clan " + name + "(" + cid + ") from the db", e);
         throw new InternalSQLException(e);
      }
      
   }
   
   @Override
   public void saveMember(Member member) {
      try {
         getMembersByUUIDStatement.setString(1, member.getId().toString());
         ResultSet rs = getMembersByUUIDStatement.executeQuery();
         
         String cid;
         if(member.getClan() != null) cid = member.getClan().getName() + "+" + member.getClan().getTag();
         else cid = "";
         System.out.println(cid);
         if(rs.next()) {
            updateMemberStatement.setString(1, cid);
            updateMemberStatement.setInt(2, member.getRank().ordinal());
            updateMemberStatement.setInt(3, 0);// Unused
            updateMemberStatement.setString(4, member.getId().toString());
            updateMemberStatement.executeUpdate();
         } else {
            addMemberStatement.setString(1, member.getId().toString());
            addMemberStatement.setString(2, cid);
            addMemberStatement.setInt(3, member.getRank().ordinal());
            addMemberStatement.executeUpdate();
         }
      } catch (SQLException e) {
         log("An exception occurred while saving member " + member.getId() + " to the db", e);
         throw new InternalSQLException(e);
      }
   }
   
   @Override
   public Member fetchMember(String uuid) {
      try {
         getMembersByCIDStatement.setString(1, uuid);
         ResultSet rs = getMembersByCIDStatement.executeQuery();
         if(rs.next()) {
            String cid = rs.getString("cid");
            Rank rank = Rank.valueOf(rs.getString("rank"));
            String clanName = cid.split("\\+")[0];
            Clan clan = null;
            if(Clan.isCached(clanName)) clan = Clan.getClan(clanName);
            return Member.createMember(UUID.fromString(uuid), clan, rank);
         } else log("player " + uuid + " is not a valid member", null, Level.SEVERE);
      } catch (SQLException e) {
         log("An exception occurred while fetching a player (uuid = " + uuid + ") from the db", e);
         throw new InternalSQLException(e);
      }
      return null;
   }
      
   @Override
   public void init() {
      try {
         getClansByNameStatement = getConnection().prepareStatement(QueryList.GET_CLAN_BY_NAME.query());
         getClanByCIDStatement = getConnection().prepareStatement(QueryList.GET_CLAN_BY_CID.query());
         getMembersByUUIDStatement = getConnection().prepareStatement(QueryList.GET_MEMBER_BY_UUID.query());
         getMembersByCIDStatement = getConnection().prepareStatement(QueryList.GET_MEMBER_BY_CID.query());
         addClanStatement = getConnection().prepareStatement(QueryList.ADD_CLAN.query());
         addMemberStatement = getConnection().prepareStatement(QueryList.ADD_MEMBER.query());
         updateClanStatement = getConnection().prepareStatement(QueryList.UPDATE_CLAN.query());
         updateClanHomeStatement = getConnection().prepareStatement(QueryList.UPDATE_CLAN_HOME.query());
         updateMemberStatement = getConnection().prepareStatement(QueryList.UPDATE_MEMBER.query());
         deleteClanStatement = getConnection().prepareStatement(QueryList.DELETE_CLAN.query());
         deleteMemberStatement = getConnection().prepareStatement(QueryList.DELETE_MEMBER.query());
         
         ClanPlugin.logger().info(" [DB] " + this.getClass().getSimpleName() + " loaded");
      } catch (SQLException e) {
         log("An exception occurred while initializing the database, so the plugin will disable to prevent missing data", e, Level.SEVERE);
         Bukkit.getPluginManager().disablePlugin(ClanPlugin.getInstance());
         throw new InternalSQLException(e);
      }
   }
   
   private static void log(String message, Exception e) {
      log(message, e, Level.WARNING);
   }
   
   private static void log(String message, Exception e, Level level) {
      ClanPlugin.logger().log(level, " [DB] " + message, e);
   }
}
