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
    compileOnly("com.github.SytexMC:Stylize:v1.1.0")
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
    implementation 'com.github.SytexMC:Stylize:v1.1.0'
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
  <version>v1.1.0</version>
  <scope>provided</scope>
</dependency>
```
</details>

<h2></h2>

### Usage

Stylize offers two ways to get started, depending on your needs:

- **Easy Setup**: If you want to get started quickly, use the `stylize()` method. This will set up Stylize with the most common options, including support for MiniPlaceholders and PlaceholderAPI.

```java
Stylize stylize = Stylize.stylize();
```

- **Custom Setup**: If you need more control, use the `builder()` method. This allows you to choose the specific features and options you want to use.

```java
Stylize stylize = Stylize.builder()
.useMiniPlaceholders(true)
.usePlaceholderAPI(true)
.tagResolver(StandardTags.defaults())
.build();
```

Once you have a Stylize object, you can use it to translate strings into styled components.

- **Translating Strings**: The `translate()` method takes a string and returns a styled component. You can also pass in additional arguments to customize the translation.

```java
stylize.translate(string); // Basic use
stylize.translate(string, player); // For player-specific placeholders
stylize.translate(string, player1, player2); // For relational placeholders
stylize.translate(string, player, tagResolver); // For custom tag resolvers
```

- **Sending Messages**: Stylize also provides methods for sending messages to players in different contexts.

```java
stylize.sendMessage(player, "<green>Hello, world!"); // Send a chat message
stylize.broadcastMessage("<yellow>Server-wide announcement!"); // Broadcast a message to all players
stylize.sendActionBar(player, "<blue>Action bar message"); // Send an action bar message
stylize.sendTitle(player, "<red>Big Title", "<gray>Subtitle text"); // Send a title
stylize.sendTitlePart(player, TitlePart.SUBTITLE, "<gold>Some subtitle :)"); // Send a title
```

<h2></h2>

### License

Stylize is licensed under the [MIT License](LICENSE).
