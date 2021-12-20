package sh.okx.rankup.commands;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import sh.okx.rankup.RankupPlugin;
import sh.okx.rankup.prestige.Prestige;
import sh.okx.rankup.prestige.Prestiges;
import sh.okx.rankup.ranks.Rank;
import sh.okx.rankup.ranks.RankElement;
import sh.okx.rankup.ranks.Rankups;
import sh.okx.rankup.util.UpdateNotifier;

@CommandAlias("rankup3|pru")
public class InfoCommand extends BaseCommand {
    private final RankupPlugin plugin;
    private final UpdateNotifier notifier;

    public InfoCommand(final RankupPlugin plugin, final UpdateNotifier notifier) {
        this.plugin = plugin;
        this.notifier = notifier;
    }

    @HelpCommand
    public void onHelp(CommandHelp help, CommandSender sender) {
        PluginDescriptionFile description = plugin.getDescription();
        String version = description.getVersion();
        sender.sendMessage(
                ChatColor.GREEN + "" + ChatColor.BOLD + description.getName() + " " + version +
                        ChatColor.YELLOW + " by " + ChatColor.BLUE + ChatColor.BOLD + String.join(", ", description.getAuthors()));
        help.showHelp();
        if (sender.hasPermission("rankup.checkversion")) {
            notifier.notify(sender, false);
        }
    }
    @Subcommand("reload")
    @CommandPermission("rankup.reload")
    @Description("Reloads configuration files.")
    public void onReload(final CommandSender sender) {
        plugin.reload(false);
        if (!plugin.error(sender)) {
            sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Rankup " + ChatColor.YELLOW + "Reloaded configuration files.");
        }
    }

    @Subcommand("forcerankup")
    @CommandPermission("rankup.force")
    @Description("Force a player to prestige, bypassing requirements.")
    public void onForceRankup(final CommandSender sender, final Player player) {
        Rankups rankups = plugin.getRankups();
        RankElement<Rank> rankElement = rankups.getByPlayer(player);
        if (rankElement == null) {
            sender.sendMessage(ChatColor.YELLOW + "That player is not in any rankup groups.");
            return;
        }
        if (!rankElement.hasNext()) {
            sender.sendMessage(ChatColor.YELLOW + "That player is at the last rank.");
            return;
        }

        Rank rank = rankElement.getRank();

        plugin.getHelper().doRankup(player, rankElement);
        plugin.getHelper().sendRankupMessages(player, rankElement);
        sender.sendMessage(ChatColor.GREEN + "Successfully forced "
                + ChatColor.GOLD + player.getName()
                + ChatColor.GREEN + " to rankup from " + ChatColor.GOLD + rank.getRank()
                + ChatColor.GREEN + " to " + ChatColor.GOLD + rank.getNext());
    }

    @Subcommand("forceprestige")
    @CommandPermission("rankup.force")
    public void onForcePrestige(final CommandSender sender, final Player player) {
        if (plugin.getPrestiges() == null) {
            sender.sendMessage(ChatColor.RED + "Prestige is disabled.");
            return;
        }

        Prestiges prestiges = plugin.getPrestiges();
        RankElement<Prestige> rankElement = prestiges.getByPlayer(player);
        if (!rankElement.hasNext()) {
            sender.sendMessage(ChatColor.YELLOW + "That player is at the last prestige.");
            return;
        }

        Prestige prestige = rankElement.getRank();
        if (prestige == null) {
            sender.sendMessage(ChatColor.YELLOW + "That player is not in any prestige groups.");
            return;
        }

        plugin.getHelper().doPrestige(player, rankElement);
        plugin.getHelper().sendPrestigeMessages(player, rankElement);
        sender.sendMessage(ChatColor.GREEN + "Successfully forced "
                + ChatColor.GOLD + player.getName()
                + ChatColor.GREEN + " to prestige "
                + ChatColor.GOLD + prestige.getRank()
                + ChatColor.GREEN + " from " + ChatColor.GOLD + prestige.getFrom()
                + ChatColor.GREEN + " to " + ChatColor.GOLD + prestige.getTo());
    }

    @Subcommand("rankdown")
    @CommandPermission("rankup.force")
    @Description("Force a player to move down one rank.")
    public void onRankDown(final CommandSender sender, final Player player) {
        RankElement<Rank> currentRankElement = plugin.getRankups().getByPlayer(player);
        if (currentRankElement == null) {
            sender.sendMessage(ChatColor.YELLOW + "That player is not in any rankup groups.");
            return;
        }
        Rank currentRank = currentRankElement.getRank();

        if (currentRankElement.isRootNode()) {
            sender.sendMessage(ChatColor.YELLOW + "That player is in the first rank and cannot be ranked down.");
            return;
        }

        RankElement<Rank> prevRankElement = plugin.getRankups().getTree().getFirst();
        while(prevRankElement.hasNext() && !prevRankElement.getNext().getRank().equals(currentRank)) {
            prevRankElement = prevRankElement.getNext();
        }

        if (!prevRankElement.hasNext()) {
            sender.sendMessage(ChatColor.YELLOW + "Could not match previous rank.");
            return;
        }
        Rank prevRank = prevRankElement.getRank();

        if (prevRankElement.getRank() != null) {
            plugin.getPermissions().removeGroup(player.getUniqueId(), currentRank.getRank());
        }
        plugin.getPermissions().addGroup(player.getUniqueId(), prevRank.getRank());

        sender.sendMessage(ChatColor.GREEN + "Successfully forced "
                + ChatColor.GOLD + player.getName()
                + ChatColor.GREEN + " to rank down from " + ChatColor.GOLD + currentRank.getRank()
                + ChatColor.GREEN + " to " + ChatColor.GOLD + prevRank.getRank());
    }


    @Subcommand("placeholders")
    @CommandPermission("rankup.admin")
    public void onPlaceholders(CommandSender sender){
        sender.sendMessage("--- Rankup placeholders ---");

        String[] placeholders = new String[] {
                "prestige_money_formatted",
                "prestige_percent_left_formatted",
                "prestige_percent_done_formatted",
                "money_formatted",
                "money_left_formatted",
                "percent_left_formatted",
                "percent_done_formatted",
                "current_prestige",
                "next_prestige",
                "current_rank",
                "next_rank",
        };
        for (String placeholder : placeholders) {
            String result;
            try {
                result = plugin.getPlaceholders().getExpansion().placeholder(sender instanceof Player ? (Player) sender : null, placeholder);
            } catch (Exception e) {
                result = e.getClass().getSimpleName() + ", " + e.getMessage();
            }
            sender.sendMessage(placeholder + ": " + result);
        }
    }

    @Subcommand("placeholders status")
    @CommandPermission("rankup.admin")
    public void onPlaceholdersStatus(CommandSender sender) {
        sender.sendMessage("--- Rankup placeholders ---");
        for (Rank rank : plugin.getRankups().getTree()) {
            String placeholder = "status_" + rank.getRank();
            sender.sendMessage(placeholder + ": " + plugin.getPlaceholders().getExpansion().placeholder(sender instanceof Player ? (Player) sender : null, placeholder));
        }
    }

    @Subcommand("tree")
    @CommandPermission("rankup.admin")
    public void onTree(CommandSender sender){
        RankElement<Rank> element = plugin.getRankups().getTree().getFirst();
        while (element.hasNext()) {
            Rank rank = element.getRank();
            RankElement<Rank> next = element.getNext();
            Rank nextRank = next.getRank();
            sender.sendMessage(rank.getRank() + " (" + rank.getNext() + ") -> " + nextRank.getRank() + " (" + nextRank.getNext() + ")");
            element = next;
        }
    }

    @Subcommand("playtime")
    @CommandPermission("rankup.playtime")
    @Description("View your playtime.")
    public static class PlayTimeCommand extends BaseCommand {
        private Statistic playOneTick = Statistic.PLAY_ONE_MINUTE;

        public PlayTimeCommand() {
            try {
                playOneTick = Statistic.valueOf("PLAY_ONE_MINUTE");
            } catch (IllegalArgumentException e) {
                // statistic was changed in 1.13.
                playOneTick = Statistic.valueOf("PLAY_ONE_TICK");
            }
        }

        @HelpCommand
        public void onHelp(CommandHelp help) {
            help.showHelp();
        }

        @Subcommand("add")
        @CommandPermission("rankup.playtime.add")
        @Description("Increase the playtime statistic for a player")
        public void onAdd(CommandSender sender, Player player, final int minutes){
            int oldMinutes = player.getStatistic(playOneTick) / 20 / 60;
            if (minutes > 0) {
                player.incrementStatistic(playOneTick, minutes * 20 * 60);
            } else if (minutes < 0) {
                if (oldMinutes + minutes < 0) {
                    sender.sendMessage(ChatColor.GRAY + "Playtime cannot be negative");
                    return;
                }
                player.decrementStatistic(playOneTick, -minutes * 20 * 60);
            }
            int newMinutes = oldMinutes + minutes;
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Increased playtime for " + player.getName() + " to " + oldMinutes + (minutes >= 0 ? "+" : "") + minutes + "=" + newMinutes + " minutes");
        }

        @Subcommand("set")
        @CommandPermission("rankup.playtime.set")
        @Description("Update the playtime statistic for a player")
        public void onSet(CommandSender sender, Player player, int minutes){
            player.setStatistic(playOneTick, minutes * 20 * 60);
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Updated playtime for " + player.getName() + " to " + minutes + " minutes");
        }

        @Subcommand("get")
        @CommandPermission("rankup.playtime.get")
        @Description("Get amount of minutes played.")
        public void onGet(CommandSender sender, @Optional Player player) {
            if(player == null && sender instanceof Player senderPlayer) {
                int ticks = senderPlayer.getStatistic(playOneTick);
                long minutes = getMinutesFromTicks(ticks);
                senderPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "You have played for " + minutes + " minutes.");
                return;
            }
            if(player != null) {
                int ticks = player.getStatistic(playOneTick);
                long minutes = getMinutesFromTicks(ticks);
                sender.sendMessage(player.getName() + "has played for " + minutes + "minutes");
            }
        }

        private long getMinutesFromTicks(int ticks) {
            return (long) (ticks / 20D / 60);
        }
    }


}
