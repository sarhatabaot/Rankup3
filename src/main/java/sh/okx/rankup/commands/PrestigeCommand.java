package sh.okx.rankup.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import sh.okx.rankup.RankupPlugin;
import sh.okx.rankup.gui.Gui;
import sh.okx.rankup.messages.Message;
import sh.okx.rankup.prestige.Prestige;
import sh.okx.rankup.prestige.Prestiges;
import sh.okx.rankup.ranks.Rank;
import sh.okx.rankup.ranks.RankElement;

import java.util.Map;
import java.util.WeakHashMap;

@CommandAlias("prestige")
public class PrestigeCommand extends BaseCommand {
    private final Map<Player, Long> confirming = new WeakHashMap<>();
    private final RankupPlugin plugin;

    public PrestigeCommand(final RankupPlugin plugin) {
        this.plugin = plugin;
    }


    @Default
    public void onPrestige(final Player player){
        if (plugin.error(player)) {
            return;
        }

        Prestiges prestiges = plugin.getPrestiges();
        if (!plugin.getHelper().checkPrestige(player)) {
            return;
        }
        RankElement<Prestige> rankElement = prestiges.getByPlayer(player);
        Prestige prestige = rankElement.getRank();

        FileConfiguration config = plugin.getConfig();
        String confirmationType = config.getString("confirmation-type").toLowerCase();
        if (confirmationType.equals("text") && confirming.containsKey(player)) {
            long time = System.currentTimeMillis() - confirming.remove(player);
            if (time < config.getInt("text.timeout") * 1000L) {
                plugin.getHelper().prestige(player);
                return;
            }
        }

        switch (confirmationType) {
            case "text" -> {
                confirming.put(player, System.currentTimeMillis());
                Prestige next = rankElement.getNext().getRank();
                Rank nextRank = next == null ? prestiges.getTree().last().getRank() : next;
                plugin.getMessage(prestige, Message.PRESTIGE_CONFIRMATION)
                        .replacePlayer(player)
                        .replaceOldRank(prestige)
                        .replaceRank(nextRank)
                        .send(player);
            }
            case "gui" -> {
                Gui gui = Gui.of(player, prestige, rankElement.getNext().getRank(), plugin, false);
                if (gui == null) {
                    player.sendMessage(ChatColor.RED + "GUI is not available. Check console for more information.");
                    return;
                }
                gui.open(player);
            }
            case "none" -> plugin.getHelper().prestige(player);
            default -> throw new IllegalArgumentException("Invalid confirmation type " + confirmationType);
        }
    }
}
