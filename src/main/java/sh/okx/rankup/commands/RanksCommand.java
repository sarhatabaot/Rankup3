package sh.okx.rankup.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sh.okx.rankup.RankupPlugin;
import sh.okx.rankup.messages.Message;
import sh.okx.rankup.ranks.Rank;
import sh.okx.rankup.ranks.RankElement;
import sh.okx.rankup.ranks.Rankups;

@CommandAlias("ranks")
public class RanksCommand extends BaseCommand {
    private final RankupPlugin plugin;

    public RanksCommand(final RankupPlugin plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onRanks(CommandSender sender) {
        Rankups rankups = plugin.getRankups();
        RankElement<Rank> playerRank = null;
        Rank pRank = null;
        if (sender instanceof Player) {
            playerRank = rankups.getByPlayer((Player) sender);
            pRank = playerRank == null ? null : playerRank.getRank();
        }

        plugin.sendHeaderFooter(sender, pRank, Message.RANKS_HEADER);

        Message message = !(sender instanceof Player && !(playerRank != null && playerRank.hasNext()))
                && playerRank == null ? Message.RANKS_INCOMPLETE : Message.RANKS_COMPLETE;
        RankElement<Rank> rank = rankups.getTree().getFirst();
        while (rank.hasNext()) {
            RankElement<Rank> next = rank.getNext();
            if (rank.getRank().equals(pRank)) {
                plugin.getMessage(sender, Message.RANKS_CURRENT, rank.getRank(), next.getRank()).failIfEmpty().send(sender);
                message = Message.RANKS_INCOMPLETE;
            } else {
                plugin.getMessage(sender, message, rank.getRank(), next.getRank()).failIfEmpty().send(sender);
            }
            rank = next;
        }
        plugin.sendHeaderFooter(sender, pRank, Message.RANKS_FOOTER);
    }
}
