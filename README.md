<div align="center">  
  <img src="https://github.com/user-attachments/assets/4d79ab53-c6a1-4e41-abe6-b4ab0c193b40" alt="Stylize" style="width:800px;height:300px;"> 
  <br>
  <img src="https://img.shields.io/jitpack/version/com.github.SytexMC/Stylize?style=flat-square&label=JitPack">
  <img src="https://img.shields.io/github/stars/sytexmc/stylize?style=flat-square&label=Stars">
  <img src="https://img.shields.io/github/forks/sytexmc/stylize?style=flat-square&label=Forks">
  <a href="https://sytexmc.github.io/Stylize/javadocs/me/sytex/stylize/package-summary.html"><img alt="Static Badge" src="https://img.shields.io/badge/Javadocs-red?style=flat-square"></a>
  <img src="https://img.shields.io/github/license/sytexmc/stylize?style=flat-square&label=License">
  <img src="https://img.shields.io/github/last-commit/sytexmc/stylize?style=flat-square&label=Last Commit">
</div>

<br>
<p align="center">
  A highly configurable & lightweight library for text formatting in <a href="https://papermc.io/">Paper</a> plugins, <br>
  with built-in support for <a href="https://docs.advntr.dev/minimessage/index.html">MiniMessages</a>, <a href="https://modrinth.com/plugin/miniplaceholders">MiniPlaceholders</a> & <a href="https://www.spigotmc.org/resources/placeholderapi.6245/">PlaceholderAPI</a>.
</p>

<h2></h2>
  
### Getting started

<p>
Stylize requires at least <a href="https://adoptium.net/de/temurin/releases/?version=17">Java</a> 17 and <a href="https://papermc.io/">Paper</a> 1.18.2 or later. <br>
To add it to your project, include one of the following snippets in your build script.
</p>

<details>
  <summary><strong>Gradle (Kotlin DSL)</strong></summary>

  ```kotlin
  repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.SytexMC:Stylize:v1.0.1")
}
```
</details>
<br>
<details>
  <summary><strong>Gradle (Groovy)</strong></summary>

  ```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.SytexMC:Stylize:v1.0.1'
}
```
</details>
<br>
<details>
  <summary><strong>Maven</strong></summary>

  ```xml
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>


<dependency>
  <groupId>com.github.SytexMC</groupId>
  <artifactId>Stylize</artifactId>
  <version>v1.0.1</version>
  <scope>provided</scope>
</dependency>
```
</details>
<h2></h2>

### Obtaining a Stylize Instance  

Stylize provides two ways to get an instance, depending on your needs:  

#### 1. Quick & Ready-to-Use  

Preconfigured with MiniPlaceholders & PlaceholderAPI support as well as a few default resolvers.

```java
Stylize stylize = Stylize.stylize();
```

#### 2. Fully Configurable

If you need more control, use the builder to customize the enabled features and tag resolvers:

```java
Stylize stylize = Stylize.builder()
    .useMiniPlaceholders(true) 
    .usePlaceholderAPI(true) 
    .tagResolver(StandardTags.defaults()) 
    .build();
```

<h2></h2>

### Usage Examples

Some usage examples of how to use your newly obtained Stylize instance

#### Translating Strings

Translate strings into components using the defined TagResolvers. <br>
These methods support player-specific placeholders, relational placeholders, and custom tag resolvers.

```java
// Translate a simple string
stylize.translate(string);

// Include player-specific placeholders (e.g., player name, health, etc.)
stylize.translate(string, player);

// Use relational placeholders involving multiple players (e.g., one player to another)
stylize.translate(string, player1, player2);

// Use custom tag resolvers along with the player context
stylize.translate(string, player, tagResolver);
```

#### Additional Methods

Methods that translate strings into components and send them in various contexts, including chat messages, action bars, and titles.

```java
// Send a personalized chat message to a player
stylize.sendMessage(player, "<green>Hello, world!");

// Broadcast a message to all players on the server
stylize.broadcastMessage("<yellow>Server-wide announcement!");

// Display a temporary action bar message for a player
stylize.sendActionBar(player, "<blue>Action bar message");

// Send a full title (main title and subtitle) to a player
stylize.sendTitle(player, "<red>Big Title", "<gray>Subtitle text");

// Set a specific part of the title (e.g., subtitle) for a player
stylize.sendTitlePart(player, TitlePart.SUBTITLE, "<gold>Some subtitle :)");
```

<h2></h2>

### License

Stylize is licensed under the [MIT License](LICENSE).
