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

# Note that this script is designed for use in GitHub Actions, and is not
# particularly robust nor configurable. Run from project parent directory.

# Use a nameref as a cache - maven evaluation is pretty slow.
# Re-calling the script and relying on it to handle caching is way easier than passing around info.
declare -a spigot_versions

# We don't care about concatenation - either it's not null and we return or it's null and we instantiate.
# shellcheck disable=SC2199
if [[ ${spigot_versions[@]} ]]; then
  for spigot_version in "${spigot_versions[@]}"; do
    echo "$spigot_version"
  done
  return
fi

# Pull Spigot dependency information from Maven.
modules=$(mvn help:evaluate -Dexpression=project.modules -q -DforceStdout -P all -pl internal | grep -oP '(?<=<string>)(.*)(?=<\/string>)')

declare -n versions="spigot_versions"

for module in "${modules[@]}"; do
  # Get number of dependencies declared in pom of specified internal module.
  max_index=$(mvn help:evaluate -Dexpression=project.dependencies -q -DforceStdout -P all -pl internal/"$module" | grep -c "<dependency>")

  for ((i=0; i < max_index; i++)); do
    # Get artifactId of dependency.
    artifact_id=$(mvn help:evaluate -Dexpression=project.dependencies["$i"].artifactId -q -DforceStdout -P all -pl internal/"$module")

    # Ensure dependency is Spigot.
    if [[ "$artifact_id" == spigot ]]; then
      # Get Spigot version.
      spigot_version=$(mvn help:evaluate -Dexpression=project.dependencies["$i"].version -q -DforceStdout -P all -pl internal/"$module")
      versions+=("$spigot_version")
      echo "$spigot_version"
      break
    fi
  done
done
