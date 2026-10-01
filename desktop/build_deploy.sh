#!/usr/bin/env bash
#
# Builds the single runnable jar (target/accountant-desktop-<version>-jar-with-dependencies.jar).
#
# Uses the Maven wrapper, because Maven itself is not installed on this machine, and the default
# profile, so the development fakes in src/dev stay out of the build.
#
# The jar needs no argument to run against the production data: 'accountant.context' is only set
# by whoever wants something else - dev.sh asks for 'devel', the tests for 'test' - so there is
# nothing in the sources to switch over before a release.

set -eu

cd "$(dirname "$0")"

./mvnw clean package assembly:single
