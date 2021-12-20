package sh.okx.rankup.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sh.okx.rankup.RankupPlugin;
import sh.okx.rankup.messages.Message;
import sh.okx.rankup.messages.MessageBuilder;
import sh.okx.rankup.prestige.Prestige;
import sh.okx.rankup.prestige.Prestiges;
import sh.okx.rankup.ranks.RankElement;

@CommandAlias("prestiges")
public class PrestigesCommand extends BaseCommand {
    private final RankupPlugin plugin;

    public PrestigesCommand(final RankupPlugin plugin) {
        this.plugin = plugin;
    }

    public void onPrestiges(CommandSender sender){
        if (plugin.error(sender)) {
            return;
        }

        Prestiges prestiges = plugin.getPrestiges();
        Prestige playerRank = null;
        if (sender instanceof Player player) {
            playerRank = prestiges.getRankByPlayer(player);
        }

        plugin.sendHeaderFooter(sender, playerRank, Message.PRESTIGES_HEADER);

        Message message = playerRank == null ? Message.PRESTIGES_INCOMPLETE : Message.PRESTIGES_COMPLETE;
        RankElement<Prestige> prestige = prestiges.getTree().getFirst();
        while (prestige.hasNext()) {
            RankElement<Prestige> next = prestige.getNext();
            if (prestige.getRank().equals(playerRank)) {
                plugin.getMessage(sender, Message.PRESTIGES_CURRENT, prestige.getRank(), next.getRank())
                        .send(sender);
                message = Message.PRESTIGES_INCOMPLETE;
            } else {
                MessageBuilder builder = plugin
                        .getMessage(sender, message, prestige.getRank(), next.getRank());
                builder.send(sender);
            }
            prestige = next;
        }

        plugin.sendHeaderFooter(sender, playerRank, Message.PRESTIGES_FOOTER);

    }
}
