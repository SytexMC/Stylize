package me.sytex.stylize;

import io.github.miniplaceholders.api.MiniPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class for styling text using MiniMessage, PlaceholderAPI and MiniPlaceholders.
 * <br>This class provides methods to convert strings into styled components and send them to players.</br>
 */
public final class Stylize {

  private final boolean usePlaceholderAPI;
  private final boolean useMiniPlaceholders;
  private final boolean useLegacyFormatting;
  private final List<Character> legacyCharacters;
  private final List<TagResolver> tagResolvers;

  private Stylize(boolean usePlaceholderAPI, boolean useMiniPlaceholders, boolean useLegacyFormatting, List<Character> legacyCharacters, List<TagResolver> tagResolvers) {
    this.usePlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && usePlaceholderAPI;
    this.useMiniPlaceholders = Bukkit.getPluginManager().isPluginEnabled("MiniPlaceholders") && useMiniPlaceholders;
    this.useLegacyFormatting = useLegacyFormatting;
    this.legacyCharacters = List.copyOf(legacyCharacters);
    this.tagResolvers = List.copyOf(tagResolvers);
  }

  /**
   * Creates a new instance of Stylize with default settings.
   *
   * @return a new Stylize instance
   */
  @Contract(value = " -> new", pure = true)
  public static @NotNull Stylize stylize() {
    return new Stylize(
        true,
        true,
        false,
        Collections.emptyList(),
        Collections.singletonList(StandardTags.defaults())
    );
  }

  /**
   * Returns a new builder for creating a Stylize instance.
   *
   * @return a new StylizeBuilder instance
   */
  @Contract(value = " -> new", pure = true)
  public static @NotNull StylizeBuilder builder() {
    return new StylizeBuilder();
  }

  /**
   * Converts a string to a styled Component using the configured resolvers.
   *
   * @param string    the input string
   * @param resolvers additional tag resolvers
   * @return the styled Component
   */
  public @NotNull Component translate(@NotNull String string, @Nullable TagResolver... resolvers) {
    return translate(string, null, null, resolvers);
  }

  /**
   * Converts a string to a styled Component using the configured resolvers and a player context.
   *
   * @param string    the input string
   * @param sender    the command sender context
   * @param resolvers additional tag resolvers
   * @return the styled Component
   */
  public @NotNull Component translate(@NotNull String string, @Nullable CommandSender sender, @Nullable TagResolver... resolvers) {
    return translate(string, sender, null, resolvers);
  }

  /**
   * Converts a string to a styled Component using the configured resolvers and player contexts.
   *
   * @param string    the input string
   * @param sender1   the first command sender context
   * @param sender2   the second command sender context
   * @param resolvers additional tag resolvers
   * @return the styled Component
   */
  public @NotNull Component translate(
      @NotNull String string,
      @Nullable CommandSender sender1,
      @Nullable CommandSender sender2,
      @Nullable TagResolver... resolvers) {

    List<TagResolver> combinedResolvers = new ArrayList<>(tagResolvers);
    if (resolvers != null) {
      combinedResolvers.addAll(Arrays.asList(resolvers));
    }

    if (usePlaceholderAPI) {
      if (sender1 instanceof Player) {
        string = sender2 != null
            ? PlaceholderAPI.setRelationalPlaceholders((Player) sender1, (Player) sender2, string)
            : PlaceholderAPI.setPlaceholders((Player) sender1, string);
      }
    }

    if (useMiniPlaceholders) {
      if (sender1 instanceof Player) {
        combinedResolvers.add(sender2 != null
            ? MiniPlaceholders.getRelationalPlaceholders(sender1, sender2)
            : MiniPlaceholders.getAudiencePlaceholders(sender1));
      } else {
        combinedResolvers.add(MiniPlaceholders.getGlobalPlaceholders());
      }
    }

    MiniMessage miniMessage = MiniMessage.builder()
        .tags(TagResolver.builder().resolvers(combinedResolvers).build())
        .build();

    if (useLegacyFormatting && !legacyCharacters.isEmpty()) {
      for (Character character : legacyCharacters) {
        string = miniMessage.serialize(LegacyComponentSerializer.builder().character(character).build().deserialize(string)).replace("\\", "");
      }
    }

    return miniMessage.deserialize(string);
  }

  /**
   * Converts a list of strings to a list of styled Components.
   *
   * @param strings   the list of input strings
   * @param resolvers additional tag resolvers
   * @return the list of styled Components
   */
  public @NotNull List<Component> translate(@NotNull List<String> strings, @Nullable TagResolver... resolvers) {
    return strings.stream()
        .map(string -> translate(string, resolvers))
        .collect(Collectors.toList());
  }

  /**
   * Converts a list of strings to a list of styled Components using a player context.
   *
   * @param strings   the list of input strings
   * @param sender    the command sender context
   * @param resolvers additional tag resolvers
   * @return the list of styled Components
   */
  public @NotNull List<Component> translate(@NotNull List<String> strings, @Nullable CommandSender sender, @Nullable TagResolver... resolvers) {
    return strings.stream()
        .map(string -> translate(string, sender, resolvers))
        .collect(Collectors.toList());
  }

  /**
   * Converts a list of strings to a list of styled Components using player contexts.
   *
   * @param strings   the list of input strings
   * @param sender1   the first command sender context
   * @param sender2   the second command sender context
   * @param resolvers additional tag resolvers
   * @return the list of styled Components
   */
  public @NotNull List<Component> translate(
      @NotNull List<String> strings,
      @Nullable CommandSender sender1,
      @Nullable CommandSender sender2,
      @Nullable TagResolver... resolvers) {
    return strings.stream()
        .map(string -> translate(string, sender1, sender2, resolvers))
        .collect(Collectors.toList());
  }

  /**
   * Sends a styled message to a recipient.
   *
   * @param recipient the recipient of the message
   * @param string    the message to send
   * @param resolvers additional tag resolvers
   */
  public void sendMessage(@NotNull CommandSender recipient, @NotNull String string, @Nullable TagResolver... resolvers) {
    Component component = translate(string, recipient, resolvers);
    recipient.sendMessage(component);
  }

  /**
   * Sends a list of styled messages to a recipient.
   *
   * @param recipient the recipient of the messages
   * @param strings   the messages to send
   * @param resolvers additional tag resolvers
   */
  public void sendMessage(@NotNull CommandSender recipient, @NotNull List<String> strings, @Nullable TagResolver... resolvers) {
    strings.forEach(string -> sendMessage(recipient, string, resolvers));
  }

  /**
   * Sends a styled message to a list of recipients.
   *
   * @param recipients the recipients of the message
   * @param string     the message to send
   * @param resolvers  additional tag resolvers
   */
  public void sendMessage(@NotNull List<CommandSender> recipients, @NotNull String string, @Nullable TagResolver... resolvers) {
    recipients.forEach(recipient -> sendMessage(recipient, string, resolvers));
  }

  /**
   * Sends a list of styled messages to a list of recipients.
   *
   * @param recipients the recipients of the messages
   * @param strings    the messages to send
   * @param resolvers  additional tag resolvers
   */
  public void sendMessage(@NotNull List<CommandSender> recipients, @NotNull List<String> strings, @Nullable TagResolver... resolvers) {
    recipients.forEach(recipient -> strings.forEach(string -> sendMessage(recipient, string, resolvers)));
  }

  /**
   * Sends a styled action bar message to a player.
   *
   * @param player    the player to send the action bar to
   * @param string    the message to send
   * @param resolvers additional tag resolvers
   */
  public void sendActionBar(@NotNull Player player, @NotNull String string, @Nullable TagResolver... resolvers) {
    Component component = translate(string, player, resolvers);
    player.sendActionBar(component);
  }

  /**
   * Sends a styled action bar message to a list of players.
   *
   * @param players   the players to send the action bar to
   * @param string    the message to send
   * @param resolvers additional tag resolvers
   */
  public void sendActionBar(@NotNull List<Player> players, @NotNull String string, @Nullable TagResolver... resolvers) {
    players.forEach(player -> sendActionBar(player, string, resolvers));
  }

  /**
   * Sends a styled title to a player.
   *
   * @param player    the player to send the title to
   * @param title     the title text
   * @param subTitle  the subtitle text
   * @param resolvers additional tag resolvers
   */
  public void sendTitle(@NotNull Player player, @NotNull String title, @NotNull String subTitle, @Nullable TagResolver... resolvers) {
    sendTitle(player, title, subTitle, Duration.ofMillis(1000), Duration.ofMillis(3500), Duration.ofMillis(500), resolvers);
  }

  /**
   * Sends a styled title to a player with custom fade-in and fade-out durations.
   *
   * @param player    the player to send the title to
   * @param title     the title text
   * @param subTitle  the subtitle text
   * @param fadeIn    the fade-in duration
   * @param fadeOut   the fade-out duration
   * @param resolvers additional tag resolvers
   */
  public void sendTitle(
      @NotNull Player player,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    sendTitle(player, title, subTitle, fadeIn, Duration.ofMillis(3500), fadeOut, resolvers);
  }

  /**
   * Sends a styled title to a player with custom fade-in, stay, and fade-out durations.
   *
   * @param player    the player to send the title to
   * @param title     the title text
   * @param subTitle  the subtitle text
   * @param fadeIn    the fade-in duration
   * @param stay      the stay duration
   * @param fadeOut   the fade-out duration
   * @param resolvers additional tag resolvers
   */
  public void sendTitle(
      @NotNull Player player,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration stay,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    Times times = Times.times(fadeIn, stay, fadeOut);

    player.sendTitlePart(TitlePart.TIMES, times);
    player.sendTitlePart(TitlePart.TITLE, translate(title, player, resolvers));
    player.sendTitlePart(TitlePart.SUBTITLE, translate(subTitle, player, resolvers));
  }

  /**
   * Sends a styled title to a list of players.
   *
   * @param players   the players to send the title to
   * @param title     the title text
   * @param subTitle  the subtitle text
   * @param resolvers additional tag resolvers
   */
  public void sendTitle(@NotNull List<Player> players, @NotNull String title, @NotNull String subTitle, @Nullable TagResolver... resolvers) {
    players.forEach(player -> sendTitle(player, title, subTitle, resolvers));
  }

  /**
   * Sends a styled title to a list of players with custom fade-in and fade-out durations.
   *
   * @param players   the players to send the title to
   * @param title     the title text
   * @param subTitle  the subtitle text
   * @param fadeIn    the fade-in duration
   * @param fadeOut   the fade-out duration
   * @param resolvers additional tag resolvers
   */
  public void sendTitle(
      @NotNull List<Player> players,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    players.forEach(player -> sendTitle(player, title, subTitle, fadeIn, fadeOut, resolvers));
  }

  /**
   * Sends a styled title to a list of players with custom fade-in, stay, and fade-out durations.
   *
   * @param players   the players to send the title to
   * @param title     the title text
   * @param subTitle  the subtitle text
   * @param fadeIn    the fade-in duration
   * @param stay      the stay duration
   * @param fadeOut   the fade-out duration
   * @param resolvers additional tag resolvers
   */
  public void sendTitle(
      @NotNull List<Player> players,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration stay,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    players.forEach(player -> sendTitle(player, title, subTitle, fadeIn, stay, fadeOut, resolvers));
  }

  /**
   * Sends a styled title part to a player.
   *
   * @param player    the player to send the title part to
   * @param titlePart the title part to send
   * @param string    the text to send
   * @param resolvers additional tag resolvers
   */
  public void sendTitlePart(
      @NotNull Player player,
      @NotNull TitlePart titlePart,
      @NotNull String string,
      @Nullable TagResolver... resolvers) {
    sendTitlePart(player, titlePart, string, Duration.ofMillis(1000), Duration.ofMillis(3500), Duration.ofMillis(500), resolvers);
  }

  /**
   * Sends a styled title part to a player with custom fade-in and fade-out durations.
   *
   * @param player    the player to send the title part to
   * @param titlePart the title part to send
   * @param string    the text to send
   * @param fadeIn    the fade-in duration
   * @param fadeOut   the fade-out duration
   * @param resolvers additional tag resolvers
   */
  public void sendTitlePart(
      @NotNull Player player,
      @NotNull TitlePart titlePart,
      @NotNull String string,
      @NotNull Duration fadeIn,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    sendTitlePart(player, titlePart, string, fadeIn, Duration.ofMillis(3500), fadeOut, resolvers);
  }

  /**
   * Sends a styled title part to a player with custom fade-in, stay, and fade-out durations.
   *
   * @param player    the player to send the title part to
   * @param titlePart the title part to send
   * @param string    the text to send
   * @param fadeIn    the fade-in duration
   * @param stay      the stay duration
   * @param fadeOut   the fade-out duration
   * @param resolvers additional tag resolvers
   */
  public void sendTitlePart(
      @NotNull Player player,
      @NotNull TitlePart titlePart,
      @NotNull String string,
      @NotNull Duration fadeIn,
      @NotNull Duration stay,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    Times times = Times.times(fadeIn, stay, fadeOut);

    player.sendTitlePart(TitlePart.TIMES, times);
    player.sendTitlePart(titlePart, translate(string, player, resolvers));
  }

  /**
   * Sends a styled title part to a list of players.
   *
   * @param players   the players to send the title part to
   * @param titlePart the title part to send
   * @param string    the text to send
   * @param resolvers additional tag resolvers
   */
  public void sendTitlePart(
      @NotNull List<Player> players,
      @NotNull TitlePart titlePart,
      @NotNull String string,
      @Nullable TagResolver... resolvers) {
    players.forEach(player -> sendTitlePart(player, titlePart, string, resolvers));
  }

  /**
   * Sends a styled title part to a list of players with custom fade-in and fade-out durations.
   *
   * @param players   the players to send the title part to
   * @param titlePart the title part to send
   * @param string    the text to send
   * @param fadeIn    the fade-in duration
   * @param fadeOut   the fade-out duration
   * @param resolvers additional tag resolvers
   */
  public void sendTitlePart(
      @NotNull List<Player> players,
      @NotNull TitlePart titlePart,
      @NotNull String string,
      @NotNull Duration fadeIn,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    players.forEach(player -> sendTitlePart(player, titlePart, string, fadeIn, fadeOut, resolvers));
  }

  /**
   * Sends a styled title part to a list of players with custom fade-in, stay, and fade-out durations.
   *
   * @param players   the players to send the title part to
   * @param titlePart the title part to send
   * @param string    the text to send
   * @param fadeIn    the fade-in duration
   * @param stay      the stay duration
   * @param fadeOut   the fade-out duration
   * @param resolvers additional tag resolvers
   */
  public void sendTitlePart(
      @NotNull List<Player> players,
      @NotNull TitlePart titlePart,
      @NotNull String string,
      @NotNull Duration fadeIn,
      @NotNull Duration stay,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    players.forEach(player -> sendTitlePart(player, titlePart, string, fadeIn, stay, fadeOut, resolvers));
  }

  /**
   * Clears the title for a player.
   *
   * @param player the player to clear the title for
   */
  public void clearTitle(@NotNull Player player) {
    player.clearTitle();
  }

  /**
   * Clears the title for a list of players.
   *
   * @param players the players to clear the title for
   */
  public void clearTitle(@NotNull List<Player> players) {
    players.forEach(this::clearTitle);
  }

  /**
   * Broadcasts a styled message to all online players.
   *
   * @param string    the message to broadcast
   * @param resolvers additional tag resolvers
   */
  public void broadcastMessage(@NotNull String string, @Nullable TagResolver... resolvers) {
    sendMessage(List.copyOf(Bukkit.getOnlinePlayers()), string, resolvers);
  }

  /**
   * Broadcasts a list of styled messages to all online players.
   *
   * @param strings   the messages to broadcast
   * @param resolvers additional tag resolvers
   */
  public void broadcastMessage(@NotNull List<String> strings, @Nullable TagResolver... resolvers) {
    strings.forEach(string -> broadcastMessage(string, resolvers));
  }

  /**
   * Broadcasts a styled action bar message to all online players.
   *
   * @param string    the message to broadcast
   * @param resolvers optional additional tag resolvers
   */
  public void broadcastActionBar(@NotNull String string, @Nullable TagResolver... resolvers) {
    sendActionBar(List.copyOf(Bukkit.getOnlinePlayers()), string, resolvers);
  }

  /**
   * Broadcasts a styled title to all online players.
   *
   * @param title     the title text to broadcast
   * @param subTitle  the subtitle text to broadcast
   * @param resolvers additional tag resolvers
   */
  public void broadcastTitle(
      @NotNull String title,
      @NotNull String subTitle,
      @Nullable TagResolver... resolvers) {
    sendTitle(List.copyOf(Bukkit.getOnlinePlayers()), title, subTitle, resolvers);
  }

  /**
   * Broadcasts a styled title to all online players with custom fade-in and fade-out durations.
   *
   * @param title     the title text to broadcast
   * @param subTitle  the subtitle text to broadcast
   * @param fadeIn    the fade-in duration
   * @param fadeOut   the fade-out duration
   * @param resolvers additional tag resolvers
   */
  public void broadcastTitle(
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    sendTitle(List.copyOf(Bukkit.getOnlinePlayers()), title, subTitle, fadeIn, fadeOut, resolvers);
  }

  /**
   * Broadcasts a styled title to all online players with custom fade-in, stay, and fade-out durations.
   *
   * @param title     the title text to broadcast
   * @param subTitle  the subtitle text to broadcast
   * @param fadeIn    the fade-in duration
   * @param stay      the stay duration
   * @param fadeOut   the fade-out duration
   * @param resolvers additional tag resolvers
   */
  public void broadcastTitle(
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration stay,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    sendTitle(List.copyOf(Bukkit.getOnlinePlayers()), title, subTitle, fadeIn, stay, fadeOut, resolvers);
  }

  /**
   * Broadcasts a styled title part to all online players.
   *
   * @param titlePart the title part to broadcast
   * @param string    the text to broadcast
   * @param resolvers additional tag resolvers
   */
  public void broadcastTitlePart(
      @NotNull TitlePart titlePart,
      @NotNull String string,
      @Nullable TagResolver... resolvers) {
    sendTitlePart(List.copyOf(Bukkit.getOnlinePlayers()), titlePart, string, resolvers);
  }

  /**
   * Broadcasts a styled title part to all online players with custom fade-in and fade-out durations.
   *
   * @param titlePart the title part to broadcast
   * @param string    the text to broadcast
   * @param fadeIn    the fade-in duration
   * @param fadeOut   the fade-out duration
   * @param resolvers additional tag resolvers
   */
  public void broadcastTitlePart(
      @NotNull TitlePart titlePart,
      @NotNull String string,
      @NotNull Duration fadeIn,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    sendTitlePart(List.copyOf(Bukkit.getOnlinePlayers()), titlePart, string, resolvers);
  }

  /**
   * Broadcasts a styled title part to all online players with custom fade-in, stay, and fade-out durations.
   *
   * @param titlePart the title part to broadcast
   * @param string    the text to broadcast
   * @param fadeIn    the fade-in duration
   * @param stay      the stay duration
   * @param fadeOut   the fade-out duration
   * @param resolvers additional tag resolvers
   */
  public void broadcastTitlePart(
      @NotNull TitlePart titlePart,
      @NotNull String string,
      @NotNull Duration fadeIn,
      @NotNull Duration stay,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    sendTitlePart(List.copyOf(Bukkit.getOnlinePlayers()), titlePart, string, resolvers);
  }

  /**
   * Builder for creating a Stylize instance.
   */
  public static class StylizeBuilder {

    private boolean usePlaceholderAPI;
    private boolean useMiniPlaceholders;
    private boolean useLegacyFormatting;
    private final List<Character> legacyCharacters;
    private final List<TagResolver> tagResolvers;

    StylizeBuilder() {
      this.legacyCharacters = new ArrayList<>();
      this.tagResolvers = new ArrayList<>();
    }

    public StylizeBuilder usePlaceholderAPI(boolean usePlaceholderAPI) {
      this.usePlaceholderAPI = usePlaceholderAPI;
      return this;
    }

    public StylizeBuilder useMiniPlaceholders(boolean useMiniPlaceholders) {
      this.useMiniPlaceholders = useMiniPlaceholders;
      return this;
    }

    public StylizeBuilder useLegacyFormatting(boolean useLegacyFormatting) {
      this.useLegacyFormatting = useLegacyFormatting;
      return this;
    }

    public StylizeBuilder addLegacyCharacter(Character character) {
      this.legacyCharacters.add(character);
      return this;
    }

    public StylizeBuilder addLegacyCharacters(Collection<? extends Character> characters) {
      this.legacyCharacters.addAll(characters);
      return this;
    }

    public StylizeBuilder addTagResolver(TagResolver tagResolver) {
      this.tagResolvers.add(tagResolver);
      return this;
    }

    public StylizeBuilder addTagResolvers(Collection<? extends TagResolver> tagResolvers) {
      this.tagResolvers.addAll(tagResolvers);
      return this;
    }

    public Stylize build() {
      return new Stylize(usePlaceholderAPI, useMiniPlaceholders, useLegacyFormatting, legacyCharacters,  tagResolvers);
    }
  }
}
