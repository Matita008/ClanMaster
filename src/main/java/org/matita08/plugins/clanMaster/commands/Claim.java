package org.matita08.plugins.clanMaster.commands;

import com.google.common.base.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Claim implements CommandExecutor {

   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if(!(sender instanceof Player p)) {
         sender.sendMessage("Command not available from the console");
         return true;
      }
      
      RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(p.getWorld()));
      
      Verify.verifyNotNull(manager);
      String nome = args[0];
      String tag = args[1];
      String rid = tag;
      
      if(manager.hasRegion(rid)) {
         return true;
      }
      
      return true;
   }
}
