package sh.okx.rankup.ranksgui;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.entity.Player;
import sh.okx.rankup.RankupPlugin;

/**
 * @author sarhatabaot
 */
@CommandAlias("ranks|ranksgui")
public class RanksGuiCommand extends BaseCommand {
    private final RankupPlugin plugin;
    private final RanksGuiListener listener;

    public RanksGuiCommand(final RankupPlugin plugin, final RanksGuiListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    @Default
    public void onGui(final Player player) {
        listener.open(new RanksGui(plugin, player));
    }
}
