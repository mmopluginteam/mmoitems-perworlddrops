package net.Indyuce.mmoitems.addon;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import net.Indyuce.mmoitems.api.droptable.DropTable;

public class DropData {
	private final Map<EntityType, DropTable> monsters = new HashMap<>();
	private final Map<Material, DropTable> blocks = new HashMap<>();

	public DropData(FileConfiguration config) {
		if (config.contains("monsters"))
			for (String s : config.getConfigurationSection("monsters").getKeys(false))
				monsters.put(EntityType.valueOf(s.toUpperCase().replace("-", "_")), new DropTable(config.getConfigurationSection("monsters." + s)));

		if (config.contains("blocks"))
			for (String s : config.getConfigurationSection("blocks").getKeys(false))
				blocks.put(Material.valueOf(s.toUpperCase().replace("-", "_")), new DropTable(config.getConfigurationSection("blocks." + s)));

	}

	public DropTable getDropTable(EntityType type) {
		return monsters.containsKey(type) ? monsters.get(type) : null;
	}

	public DropTable getDropTable(Material material) {
		return blocks.containsKey(material) ? blocks.get(material) : null;
	}
}
