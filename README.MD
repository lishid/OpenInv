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
<table width=100%>
  <tr>
    <th width=175px>Command</th>
    <th>Aliases</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>/openinv [player]</td>
    <td>oi, inv, open</td>
    <td>Open a player's inventory. If unspecified, will select last player opened or own if none opened previously.</td>
  </tr>
  <tr>
    <td>/openender [player]</td>
    <td>oe</td>
    <td>Open a player's ender chest. If unspecified, will select last player opened or own if none opened previously.</td>
  </tr>
  <tr>
    <td>/searchinv &ltitem&gt [minAmount]</td>
    <td>si</td>
    <td>Lists all online players that have a certain item in their inventory.</td>
  </tr>
  <tr>
    <td>/searchender &ltitem&gt [minAmount]</td>
    <td>se</td>
    <td>Lists all online players that have a certain item in their ender chest.</td>
  </tr>
  <tr>
    <td>/searchenchant &lt[enchantment] [MinLevel]&gt</td>
    <td>searchenchants</td>
    <td>Lists all online players with a specific enchantment.</td>
  </tr>
  <tr>
    <td>/anycontainer [check]</td>
    <td>ac, anychest</td>
    <td>Check or toggle the AnyContainer function, allowing opening blocked containers.</td>
  </tr>
  <tr>
    <td>/silentcontainer [check]</td>
    <td>sc, silentchest</td>
    <td>Check or toggle the SilentContainer function, allowing opening containers silently.</td>
  </tr>
</table>

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
</table>

## For Developers
To compile, the relevant Craftbukkit/Spigot jars must be installed in your local repository using the install plugin.
Ex: `mvn install:install-file -Dpackaging=jar -Dfile=spigot-1.8-R0.1-SNAPSHOT.jar -DgroupId=org.spigotmc -DartifactId=spigot -Dversion=1.8-R0.1-SNAPSHOT`

To compile for a single version, specify the NMS revision you are targeting: `mvn -pl <NMS module> -am clean install`

To compile for a set of versions, you'll need to use a profile. The only provided profile is `all`. Select a profile using the `-P` argument: `mvn clean package -am -P all`

For more information, check out the [official Maven guide](http://maven.apache.org/guides/introduction/introduction-to-profiles.html).

The final file is `target/OpenInv.jar`

## License
```
Copyright (C) 2011-2019 lishid. All rights reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
```
