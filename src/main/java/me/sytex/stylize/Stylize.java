/*
 * This file is part of Stylize, licensed under GPL v3.
 *
 * Copyright (c) 2025 Sytex <sytex@duck.com>
 * Copyright (c) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.sytex.stylize;

import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.miniplaceholders.api.types.RelationalAudience;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
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

@With
@Getter
@Builder
@SuppressWarnings("unused")
public final class Stylize {

  @Builder.Default
  private final boolean parsePapi = false;

  @Builder.Default
  private final boolean parseMini = false;

  @NotNull
  @Builder.Default
  private final TagResolver tagResolver = TagResolver.empty();

  @NotNull
  @Builder.Default
  private final List<Character> characters = List.of();

  public static @NotNull Stylize stylize() {
    return Holder.INSTANCE;
  }

  private static final class Holder {
    private static final Stylize INSTANCE = Stylize.builder()
        .parsePapi(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
        .parseMini(Bukkit.getPluginManager().isPluginEnabled("MiniPlaceholders"))
        .tagResolver(TagResolver.standard())
        .characters(List.of('&'))
        .build();
  }

  // --------------------------------------------------------------------------------- //
  //                                  Deserialization                                  //
  // --------------------------------------------------------------------------------- //

  public @NotNull Component deserialize(@NotNull String string) {
    return deserialize(string, TagResolver.empty());
  }

  public @NotNull Component deserialize(@NotNull String string, @NotNull TagResolver resolver) {
    MiniMessage miniMessage = MiniMessage.builder()
        .tags(TagResolver.builder()
            .resolver(tagResolver)
            .resolver(resolver)
            .resolver(parseMini ? MiniPlaceholders.globalPlaceholders() : TagResolver.empty())
            .build())
        .build();

    string = applyLegacyFormatting(string);

    return miniMessage.deserialize(string);
  }

  public @NotNull Component deserialize(@NotNull String string, @NotNull Audience player) {
    return deserialize(string, player, TagResolver.empty());
  }

  public @NotNull Component deserialize(@NotNull String string, @NotNull Audience player, @NotNull TagResolver resolver) {
    MiniMessage miniMessage = MiniMessage.builder()
        .tags(TagResolver.builder()
            .resolver(tagResolver)
            .resolver(resolver)
            .resolver(parseMini ? MiniPlaceholders.audienceGlobalPlaceholders() : TagResolver.empty())
            .build())
        .build();

    string = applyLegacyFormatting(string);
    string = applyPlaceholderAPI(string, player);

    return miniMessage.deserialize(string, player);
  }

  public @NotNull Component deserialize(@NotNull String string, @NotNull Audience primary, @NotNull Audience secondary) {
    return deserialize(string, primary, secondary, TagResolver.empty());
  }

  public @NotNull Component deserialize(@NotNull String string, @NotNull Audience primary, @NotNull Audience secondary, @NotNull TagResolver resolver) {
    MiniMessage miniMessage = MiniMessage.builder()
        .tags(TagResolver.builder()
            .resolver(tagResolver)
            .resolver(resolver)
            .resolver(parseMini ? MiniPlaceholders.relationalGlobalPlaceholders() : TagResolver.empty())
            .build())
        .build();

    string = applyPlaceholderAPI(string, primary, secondary);
    string = applyLegacyFormatting(string);

    return miniMessage.deserialize(string, new RelationalAudience<>(primary, secondary));
  }

  // --------------------------------------------------------------------------------- //
  //                                   Serialization                                   //
  // --------------------------------------------------------------------------------- //

  public static String serialize(Component component) {
    return MiniMessage.miniMessage().serialize(component);
  }

  public static String serialize(Component component, TagResolver resolver) {
    return MiniMessage.builder()
        .tags(resolver)
        .build()
        .serialize(component);
  }

  // --------------------------------------------------------------------------------- //
  //                                 Legacy Formatting                                 //
  // --------------------------------------------------------------------------------- //

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

  // --------------------------------------------------------------------------------- //
  //                                   PlaceholderAPI                                  //
  // --------------------------------------------------------------------------------- //

  private @NotNull String applyPlaceholderAPI(@NotNull String string, @NotNull Audience player) {
    if (parsePapi) {
      if (Bukkit.getPlayer(player.getOrDefault(Identity.UUID, UUID.randomUUID())) != null) {
        return PlaceholderAPI.setPlaceholders((Player) player, string);
      }
    }

    return string;
  }

  private @NotNull String applyPlaceholderAPI(@NotNull String string, @NotNull Audience primary, @NotNull Audience secondary) {
    if (parsePapi) {
      if (Bukkit.getPlayer(secondary.getOrDefault(Identity.UUID, UUID.randomUUID())) != null
          && Bukkit.getPlayer(primary.getOrDefault(Identity.UUID, UUID.randomUUID())) != null) {
        string = PlaceholderAPI.setPlaceholders((Player) primary, string);
        return PlaceholderAPI.setRelationalPlaceholders((Player) primary, (Player) secondary, string);
      }
    }

    return string;
  }

  // --------------------------------------------------------------------------------- //
  //                                     Messages                                      //
  // --------------------------------------------------------------------------------- //

  public void sendMessage(@NotNull Audience audience, @NotNull String string) {
    audience.forEachAudience(recipient -> recipient.sendMessage(deserialize(string, recipient)));
  }

  public void sendMessage(@NotNull Audience audience, @NotNull String string, @NotNull TagResolver resolver) {
    audience.forEachAudience(recipient -> recipient.sendMessage(deserialize(string, recipient, resolver)));
  }

  public void sendMessage(@NotNull Audience audience, @NotNull List<String> strings) {
    audience.forEachAudience(recipient -> strings.forEach(string -> recipient.sendMessage(deserialize(string, recipient))));
  }

  public void sendMessage(@NotNull Audience audience, @NotNull List<String> strings, @NotNull TagResolver resolver) {
    audience.forEachAudience(recipient -> strings.forEach(string -> recipient.sendMessage(deserialize(string, recipient, resolver))));
  }

  public void clearChat(@NotNull Audience audience) {
    audience.sendMessage(deserialize("<br> ".repeat(100), audience));
  }

  public void clearChat(@NotNull Audience audience, int lines) {
    audience.sendMessage(deserialize("<br> ".repeat(Math.max(0, lines - 1)), audience));
  }

  // --------------------------------------------------------------------------------- //
  //                                    ActionBars                                     //
  // --------------------------------------------------------------------------------- //

  public void sendActionBar(@NotNull Audience audience, @NotNull String string) {
    audience.forEachAudience(recipient -> recipient.sendActionBar(deserialize(string, recipient)));
  }

  public void sendActionBar(@NotNull Audience audience, @NotNull String string, @NotNull TagResolver resolver) {
    audience.forEachAudience(recipient -> recipient.sendActionBar(deserialize(string, recipient, resolver)));
  }

  public void clearActionBar(@NotNull Audience audience) {
    audience.sendActionBar(Component.empty());
  }

  // --------------------------------------------------------------------------------- //
  //                                      Titles                                       //
  // --------------------------------------------------------------------------------- //

  @Contract(value = " -> new", pure = true)
  public @NotNull TitleBuilder sendTitle() {
    return new TitleBuilder(this);
  }

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

    public TitleBuilder audience(@NotNull Audience audience) {
      this.audience = audience;
      return this;
    }

    public TitleBuilder title(@NotNull String title) {
      this.title = title;
      return this;
    }

    public TitleBuilder subTitle(@NotNull String subTitle) {
      this.subTitle = subTitle;
      return this;
    }

    public TitleBuilder fadeIn(@NotNull Duration fadeIn) {
      this.fadeIn = fadeIn;
      return this;
    }

    public TitleBuilder stay(@NotNull Duration stay) {
      this.stay = stay;
      return this;
    }

    public TitleBuilder fadeOut(@NotNull Duration fadeOut) {
      this.fadeOut = fadeOut;
      return this;
    }

    public TitleBuilder resolver(@NotNull TagResolver resolver) {
      this.resolver = resolver;
      return this;
    }

    public void send() {
      Times times = Times.times(fadeIn, stay, fadeOut);

      audience.forEachAudience(recipient -> {
        Component titleComponent = stylize.deserialize(title, recipient, resolver);
        Component subTitleComponent = stylize.deserialize(subTitle, recipient, resolver);

        recipient.showTitle(Title.title(titleComponent, subTitleComponent, times));
      });
    }
  }

  public void clearTitle(@NotNull Audience audience) {
    audience.clearTitle();
  }

  @Deprecated
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

  @Deprecated
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
        .resolver(resolver)
        .send();
  }

  @Deprecated
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

  @Deprecated
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
        .resolver(resolver)
        .send();
  }

  @Deprecated
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

  @Deprecated
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
        .resolver(resolver)
        .send();
  }
}