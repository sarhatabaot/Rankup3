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
import sh.okx.rankup.ranks.Rank;
import sh.okx.rankup.ranks.RankElement;
import sh.okx.rankup.ranks.Rankups;

import java.util.Map;
import java.util.WeakHashMap;

@CommandAlias("rankup")
public class RankupCommand extends BaseCommand {
    private final RankupPlugin plugin;
    private final Map<Player, Long> confirming = new WeakHashMap<>();

    public RankupCommand(final RankupPlugin plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onRankup(final Player player){
        if (plugin.error(player)) {
            return;
        }

        Rankups rankups = plugin.getRankups();
        if (!plugin.getHelper().checkRankup(player)) {
            return;
        }
        RankElement<Rank> rankElement = rankups.getByPlayer(player);

        FileConfiguration config = plugin.getConfig();
        String confirmationType = config.getString("confirmation-type").toLowerCase();

        // if they are on text confirming, rank them up
        // clicking on the gui cannot confirm a rankup
        if (confirmationType.equals("text") && confirming.containsKey(player)
                /*&& !(args.length > 0 && args[0].equalsIgnoreCase("gui"))*/
        ) {
            long time = System.currentTimeMillis() - confirming.remove(player);
            if (time < config.getInt("text.timeout") * 1000L) {
                plugin.getHelper().rankup(player);
                return;
            }
        }

        switch (confirmationType) {
            case "text" -> {
                confirming.put(player, System.currentTimeMillis());
                plugin.getMessage(rankElement.getRank(), Message.CONFIRMATION)
                        .replacePlayer(player)
                        .replaceOldRank(rankElement.getRank())
                        .replaceRank(rankElement.getNext().getRank())
                        .send(player);
            }
            case "gui" -> {
                Gui gui = Gui.of(player, rankElement.getRank(), rankElement.getNext().getRank(), plugin, true/*args.length > 0 && args[0].equalsIgnoreCase("gui")*/);
                if (gui == null) {
                    player.sendMessage(ChatColor.RED + "GUI is not available. Check console for more information.");
                    return;
                }
                gui.open(player);
            }
            case "none" -> plugin.getHelper().rankup(player);
            default -> throw new IllegalArgumentException("Invalid confirmation type " + confirmationType);
        }
    }
}
