package sh.okx.rankup.placeholders;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import sh.okx.rankup.RankupPlugin;

@RequiredArgsConstructor
public class RankupPlaceholderExpansion extends PlaceholderExpansion {
  private final RankupPlugin plugin;
  private final RankupExpansion expansion;

  @Override
  public String onPlaceholderRequest(Player player, @NotNull String params) {
    return expansion.placeholder(player, params);
  }

  @Override
  public @NotNull String getIdentifier() {
    return "rankup";
  }

  @Override
  public @NotNull String getAuthor() {
    return String.join(", ", plugin.getDescription().getAuthors());
  }

  @Override
  public @NotNull String getVersion() {
    return plugin.getDescription().getVersion();
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public boolean canRegister() {
    return true;
  }
}
