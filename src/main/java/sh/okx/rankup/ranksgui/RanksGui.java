package sh.okx.rankup.ranksgui;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sh.okx.rankup.RankupPlugin;
import sh.okx.rankup.gui.Gui;
import sh.okx.rankup.ranks.Rank;
import sh.okx.rankup.ranks.RankElement;
import sh.okx.rankup.util.Colour;

import java.util.List;
import java.util.function.BiFunction;

public class RanksGui {
    private final RankupPlugin plugin;
    @Getter
    private final Player player;

    private int rankupSlot;

    @Getter
    private Inventory inventory;

    public RanksGui(RankupPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        RankElement<Rank> playerRankElement = plugin.getRankups().getByPlayer(player);
        ConfigurationSection playerPath = playerRankElement == null ? null : plugin.getSection(playerRankElement.getRank(), "rankup.ranksgui");
        ConfigurationSection basePath = plugin.getMessages().getConfigurationSection("rankup.ranksgui");

        String title = get(ConfigurationSection::getString, "title", playerPath, basePath, "Ranks");
        int rows = get(Gui::getInt, "rows", playerPath, basePath, 3);
        int offset = get(Gui::getInt, "offset", playerPath, basePath, 10);
        int width = get(Gui::getInt, "width", playerPath, basePath, 7);

        inventory = Bukkit.createInventory(null, rows * 9, Colour.translate(title));


        ItemStack fill = get((section, path) -> Gui.getItem(plugin, section.getConfigurationSection(path), player, playerRankElement), "fill", playerPath, basePath, null);

        int index = offset;
        int rowIndex = offset + width;
        RankElement<Rank> rankElement = plugin.getRankups().getTree().getFirst();
        boolean complete = playerRankElement != null;
        while (rankElement.hasNext()) {
            ConfigurationSection rankPath = plugin.getSection(rankElement.getRank(), "rankup.ranksgui");

            String path;
            if (rankElement == playerRankElement) {
                path = "current";
                complete = false;
                rankupSlot = index;
            } else if (complete) {
                path = "complete";
            } else {
                path = "incomplete";
            }

            RankElement<Rank> rankElement0 = rankElement;
            ItemStack item = get((section, path0) -> Gui.getItem(plugin, section.getConfigurationSection(path0), player, rankElement0), path, rankPath, basePath, null);

            inventory.setItem(index++, item);
            if (index > rows * 9) {
                throw new IllegalArgumentException("Ranks GUI is too small for the number of ranks. Increase the number of rows on the ranks GUI.");
            }
            if (index == rowIndex) {
                rowIndex += 9;
                index += 9 - width;
            }
            rankElement = rankElement.getNext();
        }

        GuiItem infoItem = getItem("info");
        final int infoItemIndex = infoItem.getRow() * 9 + infoItem.getX();
        inventory.setItem(infoItemIndex, infoItem.getItemStack());

        if (fill != null) {
            for (int i = 0; i < rows * 9; i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null) {
                    inventory.setItem(i, fill);
                }
            }
        }

        player.openInventory(inventory);
    }

    private ItemStack getItemStackFromConfig(final ConfigurationSection section) {
        final String materialString = section.getString("material");
        if (materialString.startsWith("hdb")) {
            String hdbId = materialString.split(":")[1];
            final ItemStack head = plugin.getHeadDatabaseAPI().getItemHead(hdbId);
            if (head == null)
                return new ItemStack(Material.AIR);
            return head;
        }

        return new ItemStack(Material.matchMaterial(section.getString("material", "AIR")));
    }

    private GuiItem getItem(final String subPath) {
        final ConfigurationSection basePath = plugin.getMessages().getConfigurationSection("rankup.ranksgui");
        final ConfigurationSection section = basePath.getConfigurationSection(subPath);

        final int x = section.getInt("x");
        final int row = section.getInt("row");
        final String name = section.getString("name");
        final List<String> lore = section.getStringList("lore");

        final ItemStack item = getItemStackFromConfig(section).clone();
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);

        return new GuiItem(item, x, row);
    }

    private class GuiItem {
        private final ItemStack itemStack;
        private final int x;
        private final int row;

        public GuiItem(final ItemStack itemStack, final int x, final int row) {
            this.itemStack = itemStack;
            this.x = x;
            this.row = row;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public int getX() {
            return x;
        }

        public int getRow() {
            return row;
        }
    }

    private <T> T get(BiFunction<ConfigurationSection, String, T> function, String path, ConfigurationSection primary, ConfigurationSection secondary, T def) {
        T get = null;
        if (primary != null) {
            get = function.apply(primary, path);
        }
        if (get != null) {
            return get;
        }
        if (secondary != null) {
            get = function.apply(secondary, path);
        }
        if (get != null) {
            return get;
        }
        return def;
    }

    public void click(InventoryClickEvent event) {
        if (event.getClickedInventory() != event.getInventory()) {
            return;
        }
        int slot = event.getRawSlot();
        if (slot == rankupSlot) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                Bukkit.dispatchCommand(player, "rankup gui");
            });
        }
        //add check for info
    }

    public void close() {

    }
}
