ProjectAres
===========

Custom Bukkit/Bungee plugins used by the former Overcast Network

# License

ProjectAres is free software: you can redistribute it and/or modify it
under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

A copy of the GNU Affero General Public License is included in the file LICENSE.txt,
and can also be found at https://www.gnu.org/licenses/agpl-3.0.en.html

**The AGPL license is quite restrictive, please make sure you understand it.
If you run a modified version of this software as a network service,
anyone who can use that service must also have access to the modified source code.**


# Status

These plugins were converted from an internal project to open source in a rush,
so they are not yet well adapted for third party use. Improving this adaptation
is a top development priority, specifically:

* Eliminating dependencies on external plugins (e.g. raven-minecraft)
* Allowing network features to be fully disabled
* Expanding standalone functionality
* Map author username lookup through Mojang API
* Documentation for third-parties (e.g. "how do I run this thing??")


# Getting Help

You *may* find some help on the PGM Discord, in these channels:

[#help](https://discord.gg/wSYAT55) Help with *running* a ProjectAres server

[#contributing](https://discord.gg/6zGDEen) Discussion about *contributing* to ProjectAres

Please keep in mind that this help is provided by volunteers and other users,
out of the kindness of their golden hearts, and not as an obligation to you.


# Building

You will need a recent version of [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
Old JDK versions do have bugs that will prevent the code from compiling.
You will also need [Maven](http://maven.apache.org).

To build everything, just run `mvn clean package`.
This should download all dependencies from our [repository](https://repo.extension.ws) and create several .jar files in the `target` directories of each module.
If the build fails, please report it in Discord.

You can also download builds from our [Jenkins](https://build.extension.ws) server.


# Running

The plugins in this repo work only with [SportBukkit](https://github.com/OvercastNetwork/SportBukkit)
and our custom fork of [BungeeCord](https://github.com/OvercastNetwork/BungeeCord).
They will not work with standard CraftBukkit or BungeeCord.

Appropriate SportBukkit settings are provided in `bukkit.yml.sample` in this folder.
Of particular interest are these:

* `settings.bungeecord: true` This is required in order to connect through Bungee
* `settings.legacy-knockback: true` Emulate knockback mechanics from older versions of Minecraft
* `settings.water-pushes-tnt: false` Disable water pushing TNT, a newer Minecraft feature that we don't use.

On any PGM or Lobby server, these external plugins can/must be loaded:

* [bukkit-settings](https://github.com/OvercastNetwork/BukkitSettings) (required)
* [Channels](https://github.com/OvercastNetwork/Channels) (required)
* [raven-bukkit](https://github.com/OvercastNetwork/raven-minecraft) (optional)
* [ChatModerator](https://github.com/OvercastNetwork/ChatModerator) (optional)

As well as these plugins from this repo:

* api-bukkit (built at `API/bukkit/target/api-bukkit-#-SNAPSHOT.jar`)
* commons-bukkit (built at `Commons/bukkit/target/commons-bukkit-#-SNAPSHOT.jar`)

For a PGM server, you also need:

* PGM (built at `PGM/target/PGM-#-SNAPSHOT.jar`)

For tournament support on PGM, you also need:

* Tourney (built at `Tourney/target/Tourney-#-SNAPSHOT.jar`)

And for a Lobby server, you just need:

* Lobby (built at `Lobby/target/Lobby-#-SNAPSHOT.jar`)

For a Bungee proxy, you just need these two:

* api-bungee (built at `API/bungee/target/api-bungee-#-SNAPSHOT.jar`)
* commons-bungee (built at `Commons/bungee/target/commons-bungee-#-SNAPSHOT.jar`)


# Contents

* `Util` Utility code library (not a plugin) used by everything
  * `core` Utils independent of Bukkit or Bungee
  * `bukkit` Bukkit utils
  * `bungee` Bungee utils
* `API` Client
  * `api` Data models and services, not related to Minecraft
  * `minecraft` Code specific to Minecraft, including standalone service implementations
  * `bukkit` Bukkit plugin
  * `bungee` Bungee plugin
  * `ocn` Hybrid plugin implementing OCN's HTTP and AMQP services
* `Commons` Functionality common to all servers e.g. friends, nicknames, etc.
  * `core` Code that is common to Bukkit and Bungee plugins
  * `bukkit` Bukkit plugin
  * `bungee` Bungee plugin
* `PGM` Primary Bukkit plugin on match servers
* `Lobby` Main Bukkit plugin on lobby servers
* `Tourney` Bukkit plugin that extends PGM with tournament-related functionality

Direct dependencies between the Bukkit plugins:

* `API` -> `Raven`
* `Commons` -> `API`, `BukkitSettings`, `Channels`
* `PGM` -> `Commons`, `API`, `BukkitSettings`, `Channels`
* `Tourney` -> `PGM`, `Commons`, `API`

The API plugin is used by all the other plugins to interact with the data model.
It is split into interface and implementation layers, allowing different backends
to be used. There is a built-in default backend that implements minimal functionality
for running a standalone server. The api-ocn plugin is the backend implementation for
the former Overcast Network.


# Coding Guidelines

* General
  * Use your judgment always. Any rule can be broken with a good reason. Don't follow a rule without understanding its purpose.
  * Write code for readability above all else. Always think about how another developer would work with your code.
  * Avoid repetitive code. Factor out the repetition, if there is a reasonable way to do so.
  * We have a lot of legacy code lying around. Be careful not to build on obsolete systems or use outdated conventions.
* Formatting
  * For basic things like tab size and bracket placement, just match the existing code.
  * Use whatever layout you feel makes the code most readable, even if that differs from case to case.
  * You can use concise formats in places where it helps readability (e.g. single-line getters).
* Comments
  * Try to write code that is obvious enough so that it doesn't need to be explained with comments.
  * In places where a reader might be confused or miss something important, use comments to fill them in.
  * Don't put redundant or obvious things in comments or javadocs.
  * Ensure your IDE is not inserting any generated comments.
* Nulls
  * Strongly prefer `java.util.Optional` over `null`, generally speaking.
  * Use empty collections as nil values for collections.
  * Use `@Nullable` wherever nulls are allowed. Place it before the type, if possible.
  * Don't use `@Nonnull`. Assume anything (in our own code) without `@Nullable` is never null.
  * Use `checkNotNull` on constructor arguments for manually created objects. Don't check `@Inject`ed values, they cannot be null.
* Structure
  * Design classes to do [one thing only](https://en.wikipedia.org/wiki/Single_responsibility_principle).
    If a class provides multiple services, break them down into seperate public interfaces and keep the class private.
  * Use `final` fields, and create immutable data types, wherever possible.
  * Don't create unnecessary getters and setters, only what is actually used.
  * No mutable static fields, collections, or any other static state (there are a few exceptions, such as caches and `ThreadLocal`s).
  * Getters don't have to start with `get`, but they can if you think it's important.
* Injection
  * We use Guice everywhere. You will need to [understand it thoroughly](https://github.com/google/guice/wiki/Motivation).
  * Follow the Guice [best practices](https://github.com/google/guice/wiki/InjectOnlyDirectDependencies) (especially that one).
  * We sometimes use the term "manifest" in place of "module", to avoid confusion with PGM modules.
  * We make good use of SportBukkit's built-in Guice support:
    * Each plugin has its own [private environment](https://github.com/OvercastNetwork/minecraft-api/blob/master/src/main/java/tc/oc/inject/ProtectedModule.java)
      in which most of its bindings live.
    * Each plugin instance is bound to `org.bukkit.plugin.Plugin` inside its private module.
    * Anything that indirectly depends on `Plugin` will need to be bound in some plugin's private module.
    * If other plugins need access to that thing, it needs to be [exposed](https://google.github.io/guice/api-docs/latest/javadoc/com/google/inject/PrivateModule.html#expose-com.google.inject.Key-).
    * Avoid depending on `Plugin` directly. There are specific bindings for most Bukkit service types (e.g. `Configuration`)
      and several interfaces of our own that wrap small parts of the Bukkit API (e.g. `BukkitEventBus`).
    * If you really need a `Plugin`, always inject the base interface, never a specific plugin's class. This makes it easy to move things between plugins.
    * A `@Singleton` bound in a private environment (e.g. a plugin's private environment) will only be unique within that environment,
      not the entire process, so be careful not to accidentally duplicate a singleton in multiple private environments.
* Exceptions
  * Detect errors as early as possible, ideally at server startup. This applies to both user errors and internal assertions.
  * Only catch specific exceptions that you are expecting and can handle thoroughly. Don't hide exceptions that other handlers need to know about.
  * Avoid catching common exceptions like `IllegalArgumentException`, because it's hard to be certain where they come from.
    If you need to catch them, keep the code inside the `try` block as small as possible.
  * Don't catch all exceptions or try to handle internal errors for no particular reason. We have a nice framework for dealing with unhandled exceptions at the top-level.
  * If you need to implement a general exception handler, inject a `tc.oc.commons.core.exception.ExceptionHandler` and pass it the exceptions.
  * Ensure that all errors are seen by a human who knows how to fix them:
     * Pure user errors only need to be sent to the user who provided the bad input
     * Map errors can be sent to builders in-game (and in Sentry too, on production servers) using the `MapdevLogger`.
     * Any unexpected exception on any server must notify developers somehow i.e. through the `LoggingExceptionHandler` or some system logger.
       You can tell the user about the error so they know what's going on, but don't expect them to deal with it.
* Concurrency
  * Avoid concurrency. It's hard, and we don't have a general solution for doing it safely.
  * A Bukkit server has a single "main thread" where most game logic runs, and multiple background threads for I/O and serialization.
    All `Event`s (except `AsyncEvent`s) and scheduled tasks (except async tasks) run on the main thread.
  * Our own plugins use background thread pools for API operations, which use HTTP and AMQP.
  * Never block the main thread on API calls, or any other I/O operation. Use `ListenableFuture`s and `FutureCallback`s to handle API results.
  * Don't use the Bukkit API, or any of our own APIs, from a background thread, unless it is explicitly allowed by the API.
    Use `MainThreadExecutor` or `SyncExecutor` to get back on the main thread from a background thread.
  * A Bungee server is entirely multi-threaded. Handlers for a specific event run in sequence, but seperate events and tasks can run concurrently.
    This is one of the reasons we avoid doing things in Bungee.
* Localization
  * All in-game text must be localized.
  * To add a text string to the game, put the english template in one of the `.properties` files in Commons.
  * Use a `TranslatableComponent` to display the localized message. However, it must pass through the `ComponentRenderContext` before being
    sent to the player. That will apply the server-side translations.
  * Any changes to the templates must be sent to Crowdin before volunteers can start translating them.
    This is done with the [Crowdin integration tool](https://crowdin.com/page/cli-tool) by running `crowdin-cli upload sources` in the Commons folder.
    This should be done after the new code is deployed to production.
* Libraries
  * TODO
* Utilities
  * TODO
* Testing
  * TODO
* Logging
  * TODO

## Workflow

* We use Git, with a typical [feature branch workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/feature-branch-workflow)
* Trivial changes and emergency fixes can be merged straight to the master branch
* Any significant change requires a PR, and code review by at least one other developer.
  This applies indiscriminately to all developers. Everyone should have their code reviewed, and anyone can review anyone else's code.
* Once a change has been merged to master, it should be deployed ASAP so that problems can be found.
  Deploying several old changes at once just makes it harder to trace bugs to their source.
* Without automated tests, we rely heavily on user reports and Sentry alerts to discover regressions.
  Developers should be around for at least a few hours after their change is deployed, in case something breaks.
