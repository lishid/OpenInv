## Goals
This fork of OpenInv was created when the lag caused by offline player lookups post-UUID migration became too much to bear. Overall, it does not differ much from the original.

### Changes of Note
- Removed updater
- Removed wand
- New configuration option `DisableSaving`: Inventory edits will not be saved unless the user is online.

## Previous Versions
This fork supports any version after 1.4.5. Binaries are available in the [releases tab](https://github.com/Jikoo/OpenInv/releases). Please allow a brief period after the release of a new version for updates.

## For Developers
To compile, the relevant Craftbukkit/Spigot jars must be installed in your local repository using the install plugin.  
Ex: `install:install-file -Dpackaging=jar -Dfile=spigot-1.11-R0.1-SNAPSHOT.jar -DgroupId=org.spigotmc -DartifactId=spigot -Dversion=1.11-R0.1-SNAPSHOT`

Compiling OpenInv for a specific version is very easy - just compile the correct module.

Compiling for a set of versions is slightly more complex. You'll need to use a profile for the versions you want to compile. Provided profiles are latest, modern (versions 1.7.10+), and all. For more information, check out the [official guide](http://maven.apache.org/guides/introduction/introduction-to-profiles.html).

Regarding deprecation: Any deprecated methods will be removed in the next minor revision bump.

## License
```
Copyright (C) 2011-2014 lishid.  All rights reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation,  version 3.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
```