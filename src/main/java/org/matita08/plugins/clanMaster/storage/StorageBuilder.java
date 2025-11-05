package org.matita08.plugins.clanMaster.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.matita08.plugins.clanMaster.storage.database.Database;

public interface StorageBuilder {
   Database get(ConfigurationSection config);
}
