## About
OpenInv is a [Bukkit plugin](https://dev.bukkit.org/bukkit-plugins/openinv/) which allows users to open and edit anyone's inventory or ender chest - online or not!

## Features
- **OpenInv**: Open anyone's inventory, even if they're offline.
  - Read-only mode! No edits allowed! Don't grant the permission `OpenInv.editinv`
  - Cross-world support! Don't grant `OpenInv.crossworld`
  - No self-opening! Don't grant `OpenInv.openself`
  - Drop items as the player! Place items in the unused slots to the right of the armor to drop them
- **OpenEnder**: Open anyone's ender chest, even if they're offline.
  - Read-only mode! No edits allowed! Don't grant `OpenInv.editender`
  - Cross-world support! Don't grant `OpenInv.crossworld`
  - No opening others! Don't grant `OpenInv.openenderall`
- **SilentContainer**: Open containers without displaying an animation or making sound.
- **AnyContainer**: Open containers, even if blocked by ocelots or blocks.

## Commands
See [the wiki](https://github.com/Jikoo/OpenInv/wiki/Commands).

## Permissions
<table>
  <tr>
    <th>Node</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>OpenInv.*</td>
    <td>Gives permission to use all of OpenInv.</td>
  </tr>
  <tr>
    <td>OpenInv.openinv</td>
    <td>Required to use /openinv.</td>
  </tr>
  <tr>
    <td>OpenInv.openself</td>
    <td>Required to open own inventory.</td>
  </tr>
  <tr>
    <td>OpenInv.editinv</td>
    <td>Required to make changes to open inventories.</td>
  </tr>
  <tr>
      <td>OpenInv.openonline</td>
      <td>Allows users to open online players' inventories. For compatibility reasons this is granted by the nodes OpenInv.openinv and OpenInv.openender.</td>
  </tr>
  <tr>
      <td>OpenInv.openoffline</td>
      <td>Allows users to open offline players' inventories. For compatibility reasons this is granted by the nodes OpenInv.openinv and OpenInv.openender.</td>
  </tr>
  <tr>
    <td>OpenInv.openender</td>
    <td>Required to use /openender.</td>
  </tr>
  <tr>
    <td>OpenInv.editender</td>
    <td>Required to make changes to open ender chests.</td>
  </tr>
  <tr>
    <td>OpenInv.openenderall</td>
    <td>Allows users to open others' ender chests. Without it, users can only open their own.</td>
  </tr>
  <tr>
    <td>OpenInv.exempt</td>
    <td>Prevents the player's inventory being opened by others.</td>
  </tr>
  <tr>
    <td>OpenInv.override</td>
    <td>Allows bypassing of the exempt permission.</td>
  </tr>
  <tr>
    <td>OpenInv.crossworld</td>
    <td>Allows cross-world usage of /openinv and /openender.</td>
  </tr>
  <tr>
    <td>OpenInv.search</td>
    <td>Required to use /searchinv and /searchender.</td>
  </tr>
  <tr>
    <td>OpenInv.searchenchant</td>
    <td>Required to use /searchenchant.</td>
  </tr>
  <tr>
    <td>OpenInv.anychest</td>
    <td>Required to use /anychest.</td>
  </tr>
  <tr>
    <td>OpenInv.any.default</td>
    <td>Cause AnyContainer to be enabled by default.</td>
  </tr>
  <tr>
    <td>OpenInv.silent</td>
    <td>Required to use /silentcontainer.</td>
  </tr>
  <tr>
    <td>OpenInv.silent.default</td>
    <td>Cause SilentContainer to be enabled by default.</td>
  </tr>
  <tr>
    <td>OpenInv.spectate</td>
    <td>Allows users in spectate gamemode to edit inventories.</td>
  </tr>
</table>

## For Developers

### As a Dependency
The OpenInv API is available via [JitPack](https://jitpack.io/).
```xml
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>
```
```xml
  <dependencies>
    <dependency>
      <groupId>com.github.jikoo.OpenInv</groupId>
      <artifactId>openinvapi</artifactId>
      <version>${openinv.version}</version>
    </dependency>
  </dependencies>
```

### Compilation
To compile, the relevant Spigot jars must be installed in your local repository using the `install` plugin:
```shell
mvn install:install-file -Dpackaging=jar -Dfile=spigot-1.8-R0.1-SNAPSHOT.jar \
    -DgroupId=org.spigotmc -DartifactId=spigot -Dversion=1.8-R0.1-SNAPSHOT
```
Note that BuildTools automatically installs produced files. If you use BuildTools to compile Spigot locally, you don't need to install it manually.  
If you want to use Paper as a dependency, you can install it by executing PaperClip with the property `paperclip.install` set to true:
```shell
wget -O paperclip.jar https://papermc.io/api/v1/paper/1.16.5/latest/download
java -jar -Dpaperclip.install=true paperclip.jar
```

To compile for a single version, specify the NMS revision you are targeting: `mvn -pl <NMS module> -am clean install`

To compile for a set of versions, you'll need to use a profile. The only provided profile is `all`. Select a profile using the `-P` argument: `mvn clean package -am -P all`

For more information, check out the [official Maven guide](http://maven.apache.org/guides/introduction/introduction-to-profiles.html).

The final file is `target/OpenInv.jar`
