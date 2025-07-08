package me.sytex.stylize;

import io.github.miniplaceholders.api.MiniPlaceholders;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility class for formatting and sending text using Adventure's <a href="https://docs.advntr.dev/minimessage/index.html">MiniMessage</a> format, as well as legacy color codes.
 * Provides support for both <a href="https://www.spigotmc.org/resources/placeholderapi.6245/">PlaceholderAPI</a> and <a href="https://modrinth.com/plugin/miniplaceholders">MiniPlaceholders</a> integrations.
 */
@SuppressWarnings("unused")
public final class Stylize {

  private final boolean parsePapi;
  private final boolean parseMini;
  private final TagResolver tagResolver;
  private final List<Character> characters;

  /**
   * Constructs a new Stylize instance with the specified configuration.
   *
   * @param parsePapi     Whether to parse PlaceholderAPI placeholders
   * @param parseMini     Whether to parse MiniPlaceholders
   * @param tagResolver   Default tag resolvers to apply
   * @param characters    Characters to be used for legacy formatting
   */
  private Stylize(boolean parsePapi, boolean parseMini, TagResolver tagResolver, List<Character> characters) {
    this.parsePapi = parsePapi && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    this.parseMini = parseMini && Bukkit.getPluginManager().isPluginEnabled("MiniPlaceholders");
    this.tagResolver = tagResolver;
    this.characters = characters;
  }

  /**
   * Creates a new Stylize instance with a default configuration.
   * Enables both PlaceholderAPI and MiniPlaceholders integration if the plugins are available.
   *
   * @return A new Stylize instance with default settings
   */
  @Contract(value = " -> new", pure = true)
  public static @NotNull Stylize stylize() {
    return new Stylize(true, true, TagResolver.empty(), Collections.emptyList());
  }

  /**
   * Returns a new builder for creating customized Stylize instances.
   *
   * @return A new StylizeBuilder instance
   */
  @Contract(value = " -> new", pure = true)
  public static @NotNull Stylize.StylizeBuilder builder() {
    return new StylizeBuilder();
  }

  /**
   * Deserializes a string into a Component using MiniMessage format.
   *
   * @param string The string to deserialize
   * @return The deserialized Component
   */
  public @NotNull Component deserialize(@NotNull String string) {
    return deserialize(string, TagResolver.empty());
  }

  /**
   * Deserializes a string into a Component using MiniMessage format with multiple additional tag resolvers.
   *
   * @param string The string to deserialize
   * @param resolvers Additional tag resolvers to use
   * @return The deserialized Component
   */
  public @NotNull Component deserialize(@NotNull String string, final @NotNull TagResolver... resolvers) {
    MiniMessage miniMessage = MiniMessage.builder()
        .tags(TagResolver.builder()
            .resolver(tagResolver)
            .resolvers(resolvers)
            .resolvers(getMiniPlaceholders())
            .build())
        .build();

    string = applyLegacyFormatting(string);

    return miniMessage.deserialize(string);
  }

  /**
   * Deserializes a string into a Component using MiniMessage format with player context.
   * Processes PlaceholderAPI and MiniPlaceholder placeholders if enabled.
   *
   * @param string The string to deserialize
   * @param player The player audience for context
   * @return The deserialized Component
   */
  public @NotNull Component deserialize(@NotNull String string, final @NotNull Audience player) {
    return deserialize(string, player, TagResolver.empty());
  }

  /**
   * Deserializes a string into a Component using MiniMessage format with player context and multiple additional tag resolvers.
   * Processes PlaceholderAPI and MiniPlaceholder placeholders if enabled.
   *
   * @param string The string to deserialize
   * @param player The player audience for context
   * @param resolvers Additional tag resolvers to use
   * @return The deserialized Component
   */
  public @NotNull Component deserialize(@NotNull String string, final @NotNull Audience player, final @NotNull TagResolver... resolvers) {
    MiniMessage miniMessage = MiniMessage.builder()
        .tags(TagResolver.builder()
            .resolver(tagResolver)
            .resolvers(resolvers)
            .resolvers(getMiniPlaceholders(player))
            .build())
        .build();

    string = applyPlaceholderAPI(string, player);
    string = applyLegacyFormatting(string);

    return miniMessage.deserialize(string, player);
  }

  /**
   * Deserializes a string into a Component using MiniMessage format with primary and secondary player context.
   * Processes PlaceholderAPI and MiniPlaceholder relational placeholders if enabled.
   *
   * @param string The string to deserialize
   * @param primary The primary player audience for context
   * @param secondary The secondary player audience for relational placeholders
   * @return The deserialized Component
   */
  public @NotNull Component deserialize(@NotNull String string, final @NotNull Audience primary, final @NotNull Audience secondary) {
    return deserialize(string, primary, secondary, TagResolver.empty());
  }

  /**
   * Deserializes a string into a Component using MiniMessage format with primary and secondary player context and multiple additional tag resolvers.
   * Processes PlaceholderAPI and MiniPlaceholder relational placeholders if enabled.
   *
   * @param string The string to deserialize
   * @param primary The primary player audience for context
   * @param secondary The secondary player audience for relational placeholders
   * @param resolvers Additional tag resolvers to use
   * @return The deserialized Component
   */
  public @NotNull Component deserialize(@NotNull String string, final @NotNull Audience primary, final @NotNull Audience secondary, final @NotNull TagResolver... resolvers) {
    MiniMessage miniMessage = MiniMessage.builder()
        .tags(TagResolver.builder()
            .resolver(tagResolver)
            .resolvers(resolvers)
            .resolvers(getMiniPlaceholders(primary, secondary))
            .build())
        .build();

    string = applyPlaceholderAPI(string, primary, secondary);
    string = applyLegacyFormatting(string);

    return miniMessage.deserialize(string, primary);
  }

  /**
   * Serializes a {@link Component} into a MiniMessage format string.
   *
   * <p>This method uses the default set of {@link TagResolver}s defined in the class
   * to serialize the given {@link Component} into a MiniMessage string representation.</p>
   *
   * @param component The {@link Component} to serialize
   * @return The serialized string in MiniMessage format
   */
  public @NotNull String serialize(@NotNull Component component) {
    MiniMessage miniMessage = MiniMessage.builder()
        .tags(tagResolver)
        .build();

    return miniMessage.serialize(component);
  }

  /**
   * Serializes a {@link Component} into a MiniMessage format string
   * using the provided {@link TagResolver}s.
   *
   * <p>This method allows customization of the serialization process by providing
   * specific {@link TagResolver}s to be used when serializing the given {@link Component}.</p>
   *
   * @param component The {@link Component} to serialize
   * @param resolvers An optional array of {@link TagResolver}s to use during serialization
   * @return The serialized string in MiniMessage format
   */
  public @NotNull String serialize(@NotNull Component component, @NotNull TagResolver... resolvers) {
    MiniMessage miniMessage = MiniMessage.builder()
        .tags(TagResolver.builder()
            .resolver(tagResolver)
            .resolvers(resolvers)
            .build())
        .build();

    return miniMessage.serialize(component);
  }

  /**
   * Applies legacy formatting characters to the string.
   * Converts legacy formatting codes (e.g., &amp;c) to MiniMessage format.
   *
   * @param string The string to convert
   * @return The converted string with legacy formatting applied
   */
  private @NotNull String applyLegacyFormatting(@NotNull String string) {
    final char standardLegacyChar = '&';

    for (Character legacyChar : characters) {
      if (legacyChar != standardLegacyChar) {
        string = string.replace(legacyChar, standardLegacyChar);
      }
    }

    LegacyComponentSerializer legacySerializer = LegacyComponentSerializer
        .builder()
        .character(standardLegacyChar)
        .build();

    return MiniMessage.miniMessage()
        .serialize(legacySerializer.deserialize(string))
        .replace("\\<", "<");
  }

  /**
   * Processes PlaceholderAPI placeholders for a single player if the plugin is enabled.
   *
   * @param string The string containing placeholders
   * @param player The player audience for context
   * @return The processed string with placeholders replaced
   */
  private @NotNull String applyPlaceholderAPI(@NotNull String string, @NotNull Audience player) {
    if (parsePapi) {
      if (Bukkit.getPlayer(player.getOrDefault(Identity.UUID, UUID.randomUUID())) != null) {
        return PlaceholderAPI.setPlaceholders((Player) player, string);
      }
    }

    return string;
  }

  /**
   * Processes PlaceholderAPI placeholders including relational placeholders for two players if the plugin is enabled.
   *
   * @param string The string containing placeholders
   * @param primary The primary player audience for context
   * @param secondary The secondary player audience for relational placeholders
   * @return The processed string with placeholders replaced
   */
  private @NotNull String applyPlaceholderAPI(@NotNull String string, @NotNull Audience primary, @NotNull Audience secondary) {
    if (parsePapi) {
      if (Bukkit.getPlayer(secondary.getOrDefault(Identity.UUID, UUID.randomUUID())) != null && Bukkit.getPlayer(primary.getOrDefault(Identity.UUID, UUID.randomUUID())) != null) {
        string = PlaceholderAPI.setPlaceholders((Player) primary, string);
        return PlaceholderAPI.setRelationalPlaceholders((Player) primary, (Player) secondary, string);
      }
    }

    return string;
  }

  /**
   * Gets the global MiniPlaceholders resolver if the plugin is enabled.
   *
   * @return The MiniPlaceholders tag resolver
   */
  private @NotNull TagResolver getMiniPlaceholders() {
    return buildMiniPlaceholderResolver(null, null);
  }

  /**
   * Gets the MiniPlaceholders resolver for a single player if the plugin is enabled.
   *
   * @param player The player audience for context
   * @return The MiniPlaceholders tag resolver
   */
  private @NotNull TagResolver getMiniPlaceholders(@NotNull Audience player) {
    return buildMiniPlaceholderResolver(player, null);
  }

  /**
   * Gets the MiniPlaceholders resolver for two players (including relational placeholders) if the plugin is enabled.
   *
   * @param primary   The primary player audience for context
   * @param secondary The secondary player audience for relational placeholders
   * @return The MiniPlaceholders tag resolver
   */
  private @NotNull TagResolver getMiniPlaceholders(@NotNull Audience primary, @NotNull Audience secondary) {
    return buildMiniPlaceholderResolver(primary, secondary);
  }

  /**
   * Builds a MiniPlaceholders tag resolver with the appropriate placeholders based on the provided audiences.
   *
   * @param primary   The primary player audience for context may be null
   * @param secondary The secondary player audience for relational placeholders may be null
   * @return The built MiniPlaceholders tag resolver
   */
  private @NotNull TagResolver buildMiniPlaceholderResolver(@Nullable Audience primary, @Nullable Audience secondary) {
    if (!parseMini) {
      return TagResolver.empty();
    }

    TagResolver.Builder resolverBuilder = TagResolver.builder();

    resolverBuilder.resolver(MiniPlaceholders.getGlobalPlaceholders());

    if (primary != null) {
      resolverBuilder.resolver(MiniPlaceholders.getAudiencePlaceholders(primary));

      if (secondary != null) {
        resolverBuilder.resolver(MiniPlaceholders.getRelationalPlaceholders(primary, secondary));
        resolverBuilder.resolver(MiniPlaceholders.getRelationalGlobalPlaceholders(primary, secondary));
      }
    }

    return resolverBuilder.build();
  }

  /**
   * Sends a message to each recipient in the audience, deserializing the message for each recipient.
   *
   * @param audience The audience to send the message to
   * @param string   The message to send
   */
  public void sendMessage(@NotNull Audience audience, @NotNull String string) {
    audience.forEachAudience(recipient -> recipient.sendMessage(deserialize(string, recipient)));
  }

  /**
   * Sends a message to each recipient in the audience with an additional tag resolver, deserializing the message for each recipient.
   *
   * @param audience The audience to send the message to
   * @param string   The message to send
   * @param resolver Additional tag resolver to use
   */
  public void sendMessage(@NotNull Audience audience, @NotNull String string, @NotNull TagResolver resolver) {
    audience.forEachAudience(recipient -> recipient.sendMessage(deserialize(string, recipient, resolver)));
  }

  /**
   * Sends a message to each recipient in the audience with multiple additional tag resolvers, deserializing the message for each recipient.
   *
   * @param audience  The audience to send the message to
   * @param string    The message to send
   * @param resolver  Additional tag resolvers to use
   */
  public void sendMessage(@NotNull Audience audience, @NotNull String string, @NotNull TagResolver... resolver) {
    audience.forEachAudience(recipient -> recipient.sendMessage(deserialize(string, recipient, resolver)));
  }

  /**
   * Sends multiple messages to each recipient in the audience, deserializing each message for each recipient.
   *
   * @param audience The audience to send the messages to
   * @param strings  The list of messages to send
   */
  public void sendMessage(@NotNull Audience audience, @NotNull List<String> strings) {
    audience.forEachAudience(recipient -> strings.forEach(string -> recipient.sendMessage(deserialize(string, recipient))));
  }

  /**
   * Sends multiple messages to each recipient in the audience with an additional tag resolver, deserializing each message for each recipient.
   *
   * @param audience The audience to send the messages to
   * @param strings  The list of messages to send
   * @param resolver Additional tag resolver to use
   */
  public void sendMessage(@NotNull Audience audience, @NotNull List<String> strings, @NotNull TagResolver resolver) {
    audience.forEachAudience(recipient -> strings.forEach(string -> recipient.sendMessage(deserialize(string, recipient, resolver))));
  }

  /**
   * Sends multiple messages to each recipient in the audience with multiple additional tag resolvers, deserializing each message for each recipient.
   *
   * @param audience  The audience to send the messages to
   * @param strings   The list of messages to send
   * @param resolver  Additional tag resolvers to use
   */
  public void sendMessage(@NotNull Audience audience, @NotNull List<String> strings, @NotNull TagResolver... resolver) {
    audience.forEachAudience(recipient -> strings.forEach(string -> recipient.sendMessage(deserialize(string, recipient, resolver))));
  }

  /**
   * Sends an action bar message to each recipient in the audience, deserializing the message for each recipient.
   *
   * @param audience The audience to send the action bar to
   * @param string   The action bar message to send
   */
  public void sendActionBar(@NotNull Audience audience, @NotNull String string) {
    audience.forEachAudience(recipient -> recipient.sendActionBar(deserialize(string, recipient)));
  }

  /**
   * Sends an action bar message to each recipient in the audience with an additional tag resolver, deserializing the message for each recipient.
   *
   * @param audience The audience to send the action bar to
   * @param string   The action bar message to send
   * @param resolver Additional tag resolver to use
   */
  public void sendActionBar(@NotNull Audience audience, @NotNull String string, @NotNull TagResolver resolver) {
    audience.forEachAudience(recipient -> recipient.sendActionBar(deserialize(string, recipient, resolver)));
  }

  /**
   * Sends an action bar message to each recipient in the audience with multiple additional tag resolvers, deserializing the message for each recipient.
   *
   * @param audience  The audience to send the action bar to
   * @param string    The action bar message to send
   * @param resolver  Additional tag resolvers to use
   */
  public void sendActionBar(@NotNull Audience audience, @NotNull String string, @NotNull TagResolver... resolver) {
    audience.forEachAudience(recipient -> recipient.sendActionBar(deserialize(string, recipient, resolver)));
  }


  /**
   * Creates a new {@link TitleBuilder} instance for building and sending titles.
   *
   * @return a new {@link TitleBuilder} instance
   */
  @Contract(value = " -> new", pure = true)
  public @NotNull TitleBuilder sendTitle() {
    return new TitleBuilder(this);
  }

  /**
   * Sends a title and subtitle to the specified audience.
   *
   * @param audience the audience to send the title to
   * @param title    the title text
   * @param subTitle the subtitle text
   */
  public void sendTitle(
      @NotNull Audience audience,
      @NotNull String title,
      @NotNull String subTitle
  ) {
    sendTitle()
        .audience(audience)
        .title(title)
        .subTitle(subTitle)
        .send();
  }

  /**
   * Sends a title and subtitle to the specified audience with a tag resolver.
   *
   * @param audience  the audience to send the title to
   * @param title     the title text
   * @param subTitle  the subtitle text
   * @param resolver  the tag resolver for the title
   */
  public void sendTitle(
      @NotNull Audience audience,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull TagResolver resolver
  ) {
    sendTitle()
        .audience(audience)
        .title(title)
        .subTitle(subTitle)
        .resolvers(resolver)
        .send();
  }

  /**
   * Sends a title and subtitle to the specified audience with multiple tag resolvers.
   *
   * @param audience   the audience to send the title to
   * @param title      the title text
   * @param subTitle   the subtitle text
   * @param resolvers  the tag resolvers for the title
   */
  public void sendTitle(
      @NotNull Audience audience,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull TagResolver... resolvers
  ) {
    sendTitle()
        .audience(audience)
        .title(title)
        .subTitle(subTitle)
        .resolvers(resolvers)
        .send();
  }

  /**
   * Sends a title and subtitle to the specified audience with custom fade-in and fade-out durations.
   *
   * @param audience  the audience to send the title to
   * @param title     the title text
   * @param subTitle  the subtitle text
   * @param fadeIn    the fade-in duration
   * @param fadeOut   the fade-out duration
   */
  public void sendTitle(
      @NotNull Audience audience,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration fadeOut
  ) {
    sendTitle()
        .audience(audience)
        .title(title)
        .subTitle(subTitle)
        .fadeIn(fadeIn)
        .fadeOut(fadeOut)
        .send();
  }

  /**
   * Sends a title and subtitle to the specified audience with custom fade-in, fade-out durations, and a tag resolver.
   *
   * @param audience  the audience to send the title to
   * @param title     the title text
   * @param subTitle  the subtitle text
   * @param fadeIn    the fade-in duration
   * @param fadeOut   the fade-out duration
   * @param resolver  the tag resolver for the title
   */
  public void sendTitle(
      @NotNull Audience audience,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration fadeOut,
      @NotNull TagResolver resolver
  ) {
    sendTitle()
        .audience(audience)
        .title(title)
        .subTitle(subTitle)
        .fadeIn(fadeIn)
        .fadeOut(fadeOut)
        .resolvers(resolver)
        .send();
  }

  /**
   * Sends a title and subtitle to the specified audience with custom fade-in, fade-out durations, and multiple tag resolvers.
   *
   * @param audience   the audience to send the title to
   * @param title      the title text
   * @param subTitle   the subtitle text
   * @param fadeIn     the fade-in duration
   * @param fadeOut    the fade-out duration
   * @param resolvers  the tag resolvers for the title
   */
  public void sendTitle(
      @NotNull Audience audience,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration fadeOut,
      @NotNull TagResolver... resolvers
  ) {
    sendTitle()
        .audience(audience)
        .title(title)
        .subTitle(subTitle)
        .fadeIn(fadeIn)
        .fadeOut(fadeOut)
        .resolvers(resolvers)
        .send();
  }

  /**
   * Sends a title and subtitle to the specified audience with custom fade-in, stay, and fade-out durations.
   *
   * @param audience  the audience to send the title to
   * @param title     the title text
   * @param subTitle  the subtitle text
   * @param fadeIn    the fade-in duration
   * @param stay      the stay duration
   * @param fadeOut   the fade-out duration
   */
  public void sendTitle(
      @NotNull Audience audience,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration stay,
      @NotNull Duration fadeOut
  ) {
    sendTitle()
        .audience(audience)
        .title(title)
        .subTitle(subTitle)
        .fadeIn(fadeIn)
        .stay(stay)
        .fadeOut(fadeOut)
        .send();
  }

  /**
   * Sends a title and subtitle to the specified audience with custom fade-in, stay, and fade-out durations, and a tag resolver.
   *
   * @param audience  the audience to send the title to
   * @param title     the title text
   * @param subTitle  the subtitle text
   * @param fadeIn    the fade-in duration
   * @param stay      the stay duration
   * @param fadeOut   the fade-out duration
   * @param resolver  the tag resolver for the title
   */
  public void sendTitle(
      @NotNull Audience audience,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration stay,
      @NotNull Duration fadeOut,
      @NotNull TagResolver resolver
  ) {
    sendTitle()
        .audience(audience)
        .title(title)
        .subTitle(subTitle)
        .fadeIn(fadeIn)
        .stay(stay)
        .fadeOut(fadeOut)
        .resolvers(resolver)
        .send();
  }

  /**
   * Sends a title and subtitle to the specified audience with custom fade-in, stay, and fade-out durations, and multiple tag resolvers.
   *
   * @param audience   the audience to send the title to
   * @param title      the title text
   * @param subTitle   the subtitle text
   * @param fadeIn     the fade-in duration
   * @param stay       the stay duration
   * @param fadeOut    the fade-out duration
   * @param resolvers  the tag resolvers for the title
   */
  public void sendTitle(
      @NotNull Audience audience,
      @NotNull String title,
      @NotNull String subTitle,
      @NotNull Duration fadeIn,
      @NotNull Duration stay,
      @NotNull Duration fadeOut,
      @NotNull TagResolver... resolvers
  ) {
    sendTitle()
        .audience(audience)
        .title(title)
        .subTitle(subTitle)
        .fadeIn(fadeIn)
        .stay(stay)
        .fadeOut(fadeOut)
        .resolvers(resolvers)
        .send();
  }

  /**
   * Builder class for constructing and sending titles.
   */
  public static final class TitleBuilder {

    private static final String EMPTY_TEXT = "";
    private static final Audience DEFAULT_AUDIENCE = Audience.audience();
    private static final Duration DEFAULT_FADE_IN = Duration.ofMillis(1000);
    private static final Duration DEFAULT_STAY = Duration.ofMillis(3500);
    private static final Duration DEFAULT_FADE_OUT = Duration.ofMillis(500);
    private static final TagResolver DEFAULT_RESOLVER = TagResolver.empty();

    private final Stylize stylize;

    private String title = EMPTY_TEXT;
    private String subTitle = EMPTY_TEXT;
    private Audience audience = DEFAULT_AUDIENCE;
    private Duration fadeIn = DEFAULT_FADE_IN;
    private Duration stay = DEFAULT_STAY;
    private Duration fadeOut = DEFAULT_FADE_OUT;
    private TagResolver resolver = DEFAULT_RESOLVER;

    TitleBuilder(Stylize stylize) {
      this.stylize = stylize;
    }

    /**
     * Sets the audience for the title.
     *
     * @param audience the audience to send the title to
     * @return this builder instance
     */
    public TitleBuilder audience(@NotNull Audience audience) {
      Objects.requireNonNull(audience, "Audience cannot be null!");
      this.audience = audience;
      return this;
    }

    /**
     * Sets the title text.
     *
     * @param title the title text
     * @return this builder instance
     */
    public TitleBuilder title(@NotNull String title) {
      Objects.requireNonNull(title, "Title cannot be null!");
      this.title = title;
      return this;
    }

    /**
     * Sets the subtitle text.
     *
     * @param subTitle the subtitle text
     * @return this builder instance
     */
    public TitleBuilder subTitle(@NotNull String subTitle) {
      Objects.requireNonNull(subTitle, "SubTitle cannot be null!");
      this.subTitle = subTitle;
      return this;
    }

    /**
     * Sets the fade-in duration for the title.
     *
     * @param fadeIn the fade-in duration
     * @return this builder instance
     */
    public TitleBuilder fadeIn(@NotNull Duration fadeIn) {
      Objects.requireNonNull(fadeIn, "FadeIn cannot be null!");
      this.fadeIn = fadeIn;
      return this;
    }

    /**
     * Sets the stay duration for the title.
     *
     * @param stay the stay duration
     * @return this builder instance
     */
    public TitleBuilder stay(@NotNull Duration stay) {
      Objects.requireNonNull(stay, "Stay cannot be null!");
      this.stay = stay;
      return this;
    }

    /**
     * Sets the fade-out duration for the title.
     *
     * @param fadeOut the fade-out duration
     * @return this builder instance
     */
    public TitleBuilder fadeOut(@NotNull Duration fadeOut) {
      Objects.requireNonNull(fadeOut, "FadeOut cannot be null!");
      this.fadeOut = fadeOut;
      return this;
    }

    /**
     * Sets the tag resolvers for the title.
     *
     * @param resolvers the tag resolvers for the title
     * @return this builder instance
     */
    public TitleBuilder resolvers(@NotNull TagResolver... resolvers) {
      Objects.requireNonNull(resolvers, "Resolvers cannot be null!");
      this.resolver = TagResolver.builder().resolvers(resolvers).build();
      return this;
    }

    /**
     * Sends the constructed title to the audience.
     */
    public void send() {
      Times times = Times.times(fadeIn, stay, fadeOut);

      audience.forEachAudience(recipient -> {
        Component titleComponent = stylize.deserialize(title, recipient, resolver);
        Component subTitleComponent = stylize.deserialize(subTitle, recipient, resolver);

        recipient.showTitle(Title.title(titleComponent, subTitleComponent, times));
      });
    }
  }

  /**
   * Builder class for constructing {@link Stylize} instances.
   */
  public static class StylizeBuilder {

    private boolean parsePapi = false;
    private boolean parseMini = false;
    private TagResolver tagResolver = TagResolver.empty();
    private List<Character> characters = Collections.emptyList();

    StylizeBuilder() { }

    /**
     * Sets whether to parse PlaceholderAPI placeholders.
     *
     * @param parsePapi whether to parse PlaceholderAPI placeholders
     * @return this builder instance
     */
    public StylizeBuilder parsePlaceholderAPI(boolean parsePapi) {
      this.parsePapi = parsePapi;
      return this;
    }

    /**
     * Sets whether to parse MiniPlaceholders placeholders.
     *
     * @param parseMini whether to parse MiniPlaceholders placeholders
     * @return this builder instance
     */
    public StylizeBuilder parseMiniPlaceholders(boolean parseMini) {
      this.parseMini = parseMini;
      return this;
    }

    /**
     * Sets the base tag resolver for the stylize instance.
     *
     * @param tagResolver the tag resolver
     * @return this builder instance
     */
    public StylizeBuilder tags(@NotNull TagResolver tagResolver) {
      this.tagResolver = tagResolver;
      return this;
    }

    /**
     * Sets the characters to be used for styling.
     *
     * @param characters the characters to be used for legacy styling
     * @return this builder instance
     */
    public StylizeBuilder legacy(@NotNull List<Character> characters) {
      this.characters = characters;
      return this;
    }

    /**
     * Builds the {@link Stylize} instance.
     *
     * @return the constructed {@link Stylize} instance
     */
    public Stylize build() {
      return new Stylize(parsePapi, parseMini, tagResolver, characters);
    }
  }
}
