#!/bin/sh
#
# Copyright © 2015-2021 the original authors.
# Licensed under Apache License 2.0
# RepForge wrapper - uses gradle/wrapper/gradle-wrapper.jar (Gradle 8.11.1)
#
set -e
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
if [ ! -f "$CLASSPATH" ]; then echo "Missing $CLASSPATH - open in Android Studio to regenerate"; exit 1; fi
exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
