package me.sytex.stylize;

import io.github.miniplaceholders.api.MiniPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

  public void sendMessage(@NotNull Audience audience, @NotNull String string, @Nullable TagResolver... resolvers) {
    audience.forEachAudience(recipient -> {
      Component component = translate(string, (CommandSender) recipient, resolvers);
      recipient.sendMessage(component);
    });
  }

  public void sendMessages(@NotNull Audience audience, @NotNull List<String> strings, @Nullable TagResolver... resolvers) {
    audience.forEachAudience(recipient -> {
      List<Component> messages = strings.stream()
          .map(string -> translate(string, (CommandSender) recipient, resolvers))
          .toList();

      messages.forEach(recipient::sendMessage);
    });
  }

  public void sendActionBar(@NotNull Audience audience, @NotNull String string, @Nullable TagResolver... resolvers) {
    audience.forEachAudience(recipient -> {
      Component component = translate(string, (CommandSender) recipient, resolvers);
      recipient.sendActionBar(component);
    });
  }

  public void sendTitle(@NotNull Audience audience, @NotNull String title, @NotNull String subTitle, @Nullable TagResolver... resolvers) {
    sendTitle(audience, title, subTitle, Duration.ofMillis(1000), Duration.ofMillis(3500), Duration.ofMillis(500), resolvers);
  }

  public void sendTitle(
      @NotNull Audience audience,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    sendTitle(audience, title, subTitle, fadeIn, fadeOut, Duration.ofMillis(3500), resolvers);
  }

  public void sendTitle(
      @NotNull Audience audience,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration stay,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    audience.forEachAudience(recipient -> {
      Times times = Times.times(fadeIn, stay, fadeOut);

      recipient.sendTitlePart(TitlePart.TIMES, times);
      recipient.sendTitlePart(TitlePart.TITLE, translate(title, (CommandSender) recipient, resolvers));
      recipient.sendTitlePart(TitlePart.SUBTITLE, translate(subTitle, (CommandSender) recipient, resolvers));
    });
  }

  public void sendTitlePart(
      @NotNull Audience audience,
      @NotNull TitlePart titlePart,
      @NotNull String string,
      @Nullable TagResolver... resolvers) {
    sendTitlePart(audience, titlePart, string, Duration.ofMillis(1000), Duration.ofMillis(3500), Duration.ofMillis(500), resolvers);
  }

  public void sendTitlePart(
      @NotNull Audience audience,
      @NotNull TitlePart titlePart,
      @NotNull String string,
      @NotNull Duration fadeIn,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    sendTitlePart(audience, titlePart, string, fadeIn, Duration.ofMillis(3500), fadeOut, resolvers);
  }

  public void sendTitlePart(
      @NotNull Audience audience,
      @NotNull TitlePart titlePart,
      @NotNull String string,
      @NotNull Duration fadeIn,
      @NotNull Duration stay,
      @NotNull Duration fadeOut,
      @Nullable TagResolver... resolvers) {
    audience.forEachAudience(recipient -> {
      Times times = Times.times(fadeIn, stay, fadeOut);

      recipient.sendTitlePart(TitlePart.TIMES, times);
      recipient.sendTitlePart(titlePart, translate(string, (CommandSender) recipient, resolvers));
    });
  }

  public void clearTitle(@NotNull Audience audience) {
    audience.clearTitle();
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
