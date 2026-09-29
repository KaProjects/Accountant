#!/usr/bin/env bash
#
# Builds the single runnable jar (target/accountant-desktop-<version>-jar-with-dependencies.jar).
#
# Uses the Maven wrapper, because Maven itself is not installed on this machine, and the default
# profile, so the development fakes in src/dev stay out of the build.

set -eu

cd "$(dirname "$0")"

./mvnw clean package assembly:single
