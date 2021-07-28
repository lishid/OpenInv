#!/bin/bash
#
# Copyright (C) 2011-2021 lishid. All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, version 3.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
#

# TODO FIGURE OUT AND REMOVE WHEN LESS STRESS
hacky_versions=("1.16.5-R0.1-SNAPSHOT" "1.17.1-R0.1-SNAPSHOT")
for hacky_version in "${hacky_versions[@]}"; do
  echo "$hacky_version"
done

exit 0

# Note that this script is designed for use in GitHub Actions, and is not
# particularly robust nor configurable. Run from project parent directory.

# Pull Spigot dependency information from Maven.
# Since we only care about Spigot versions, only check modules in the folder internal.
readarray -t modules <<< "$(mvn help:evaluate -Dexpression=project.modules -q -DforceStdout -P all | grep -oP '(?<=<string>)(internal/.*)(?=</string>)')"

for module in "${modules[@]}"; do
  # Get number of dependencies declared in pom of specified internal module.
  max_index=$(mvn help:evaluate -Dexpression=project.dependencies -q -DforceStdout -P all -pl "$module" | grep -c "<dependency>")

  for ((i=0; i < max_index; i++)); do
    # Get artifactId of dependency.
    artifact_id=$(mvn help:evaluate -Dexpression=project.dependencies["$i"].artifactId -q -DforceStdout -P all -pl "$module")

    # Ensure dependency is Spigot.
    if [[ "$artifact_id" == spigot ]]; then
      # Get Spigot version.
      spigot_version=$(mvn help:evaluate -Dexpression=project.dependencies["$i"].version -q -DforceStdout -P all -pl "$module")
      versions+=("$spigot_version")
      echo "$spigot_version"
      break
    fi
  done
done
