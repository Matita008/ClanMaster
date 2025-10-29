package org.matita08.plugins.clanMaster.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.matita08.plugins.clanMaster.ClanPlugin;

import java.util.logging.Logger;

public class PermsHelper {
   private static final Logger LOG = ClanPlugin.logger();
   private static LuckPerms luckPerms;
   
   public static void init() {
      var registration = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
      if(registration != null) {
         luckPerms = registration.getProvider();
         LOG.info(" [PermsAPI] LuckPerms found");
         LOG.info(" [PermsAPI] initialized successfully");
      } else {
         LOG.warning(" [PermsAPI] LuckPerms not found");
         LOG.severe(" [PermsAPI] Disabling ClanMaster");
         Bukkit.getPluginManager().disablePlugin(ClanPlugin.getInstance());
      }
   }
   
   public static void addPermission (Player player, String permission){
      try {
         luckPerms.getUserManager().modifyUser(player.getUniqueId(), user->{
            user.data().add(Node.builder(permission).build());
         });
      } catch (Exception e) {
         LOG.warning("Failed to add permission: " + e.getMessage());
      }
   }
   
   public static boolean hasPermission(Player player, String permission) {
      var user = luckPerms.getUserManager().getUser(player.getUniqueId());
      if(user == null) {
         return false;
      }
      return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
   }
   
}
