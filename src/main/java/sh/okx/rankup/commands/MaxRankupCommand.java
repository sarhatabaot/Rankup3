package sh.okx.rankup.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.entity.Player;
import sh.okx.rankup.RankupHelper;
import sh.okx.rankup.RankupPlugin;
import sh.okx.rankup.ranks.Rank;
import sh.okx.rankup.ranks.RankElement;

@CommandAlias("maxrankup")
public class MaxRankupCommand extends BaseCommand {
    private final RankupPlugin plugin;

    public MaxRankupCommand(final RankupPlugin plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onMaxRankup(Player player) {
        RankupHelper helper = plugin.getHelper();
        if (!helper.checkRankup(player)) {
            return;
        }

        do {
            RankElement<Rank> rank = plugin.getRankups().getByPlayer(player);
            rank.getRank().applyRequirements(player);

            helper.doRankup(player, rank);

            // if the individual-messages setting is disabled, only send the "well done you ranked up"
            // messages if they can't rank up any more.
            if (plugin.getConfig().getBoolean("max-rankup.individual-messages")
                    || !helper.checkRankup(player, false)) {
                helper.sendRankupMessages(player, rank);
            }
        } while (helper.checkRankup(player, false));
    }
}
