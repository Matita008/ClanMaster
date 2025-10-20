package org.matita08.plugins.clanMaster.storage;

import org.bukkit.configuration.ConfigurationSection;

public interface DatabaseBuilder {
   Database get(ConfigurationSection config);
}
