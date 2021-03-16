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

# A script for installing required Spigot versions.
#
# Note that this script is designed for use in GitHub Actions, and is
# not particularly robust nor configurable.
# In its current state, the script must be run from OpenInv's parent
# project directory and will always install BuildTools to ~/buildtools.

buildtools_dir=~/buildtools
buildtools=$buildtools_dir/BuildTools.jar

get_spigot_versions () {
  # Get all submodules of internal module
  modules=$(mvn help:evaluate -Dexpression=project.modules -q -DforceStdout -P all -pl internal | grep -oP '(?<=<string>)(.*)(?=<\/string>)')
  for module in "${modules[@]}"; do

    # Get number of dependencies declared in pom of specified internal module
    max_index=$(mvn help:evaluate -Dexpression=project.dependencies -q -DforceStdout -P all -pl internal/"$module" | grep -c "<dependency>")

    for ((i=0; i < max_index; i++)); do
      # Get artifactId of dependency
      artifact_id=$(mvn help:evaluate -Dexpression=project.dependencies["$i"].artifactId -q -DforceStdout -P all -pl internal/"$module")

      # Ensure dependency is spigot
      if [[ "$artifact_id" == spigot ]]; then
        # Get spigot version
        spigot_version=$(mvn help:evaluate -Dexpression=project.dependencies["$i"].version -q -DforceStdout -P all -pl internal/"$module")
        echo "$spigot_version"
        break
      fi
    done
  done
}

get_buildtools () {
  if [[ -d $buildtools_dir && -f $buildtools ]]; then
    return
  fi

  mkdir $buildtools_dir
  wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar -O $buildtools
}

versions=$(get_spigot_versions)
echo Found Spigot dependencies: "$versions"

for version in "${versions[@]}"; do
  set -e
  exit_code=0
  mvn dependency:get -Dartifact=org.spigotmc:spigot:"$version" -q -o || exit_code=$?
  if [ $exit_code -ne 0 ]; then
    echo Installing missing Spigot version "$version"
    revision=$(echo "$version" | grep -oP '(\d+\.\d+(\.\d+)?)(?=-R[0-9\.]+-SNAPSHOT)')
    get_buildtools
    java -jar $buildtools -rev "$revision"
  else
    echo Spigot "$version" is already installed
  fi
done
