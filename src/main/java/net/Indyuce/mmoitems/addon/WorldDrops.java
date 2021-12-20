package net.Indyuce.mmoitems.addon;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.droptable.DropTable;
import net.Indyuce.mmoitems.api.event.ItemDropEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldDrops extends JavaPlugin implements Listener, CommandExecutor {
    public static WorldDrops plugin;

    private Map<String, DropData> worlds = new HashMap<>();

    public void onEnable() {
        plugin = this;

        getCommand("mmoitemsworlddrops").setExecutor(this);

        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        for (World world : Bukkit.getWorlds())
            new ConfigFile(MMOItems.plugin, "/drops", world.getName()).setup();

        reloadDrops();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mmoitems.op"))
            return true;

        reloadDrops();
        sender.sendMessage(MMOItems.plugin.getPrefix() + "Drop data reloaded.");
        return true;
    }

    private void reloadDrops() {
        worlds.clear();
        for (World world : Bukkit.getWorlds())
            worlds.put(world.getName(), new DropData(new ConfigFile(MMOItems.plugin, "/drops", world.getName()).getConfig()));
    }

    public DropData getDropData(World world) {
        return worlds.containsKey(world.getName()) ? worlds.get(world.getName()) : null;
    }

    @EventHandler
    public void a(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        DropData dropData = getDropData(entity.getWorld());
        if (dropData == null)
            return;

        DropTable dropTable = dropData.getDropTable(entity.getType());
        if (dropTable == null)
            return;

        List<ItemStack> drops = dropTable.read(entity.getKiller() == null ? null : PlayerData.get(entity.getKiller()), false);

        ItemDropEvent dropEvent = new ItemDropEvent(entity.getKiller(), drops, entity);
        Bukkit.getPluginManager().callEvent(dropEvent);
        if (dropEvent.isCancelled())
            return;

        event.getDrops().addAll(drops);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void b(BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        if (player == null || player.getGameMode() == GameMode.CREATIVE)
            return;

        Block block = event.getBlock();
        DropData dropData = getDropData(block.getWorld());
        if (dropData == null)
            return;

        DropTable dropTable = dropData.getDropTable(block.getType());
        if (dropTable == null)
            return;

        List<ItemStack> drops = dropTable.read(PlayerData.get(player), hasSilkTouchTool(player));

        ItemDropEvent dropEvent = new ItemDropEvent(player, drops, block);
        Bukkit.getPluginManager().callEvent(dropEvent);
        if (dropEvent.isCancelled())
            return;

        for (ItemStack drop : drops)
            block.getWorld().dropItemNaturally(block.getLocation().add(.5, .5, .5), drop);
    }

    @SuppressWarnings("deprecation")
    private boolean hasSilkTouchTool(Player player) {
        ItemStack item = player.getItemInHand();
        return item != null && item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH);
    }
}