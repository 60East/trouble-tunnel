#!/bin/bash

#
# ||| COPY |||
# ||| THIS |||
# vvv PART vvv
# toolbox init
#
TOOLBOX_INSTALL_URL=http://devnull.crankuptheamps.com/releases/scripts/orch-toolbox/install.sh
function die() { echo $* 1>&2; exit 1; }
cd $(dirname $0) || die "failed to cd to '$(dirname $0)'"
export DOT_ORCH=$(dirname $(pwd))
export ORCH_TOOLBOX=$(curl -sSL $TOOLBOX_INSTALL_URL | bash)
cd $ORCH_TOOLBOX || die "failed to cd to ORCH_TOOLBOX '$ORCH_TOOLBOX'"
[ -f .env ] || die ".env not found in '$(pwd)'"
source .env
#
# ^^^ COPY ^^^
# ||| THAT |||
# ||| PART |||
# toolbox init
#

set_score $(pwd)/$0
perf_init

cd $GIT_ROOT || die
exec_to_files_or_die ant ant

TGZ=trouble-tunnel.tgz
[ -f "$TGZ" ] || record_failure "release file not found"

RELEASE_DIR=/mnt/www/devnull/releases/trouble-tunnel
echo orcpub send $TGZ $RELEASE_DIR
in_automation && exec_to_files_or_die orcpub-send-tgz orcpub send $TGZ $RELEASE_DIR

cp $GIT_ROOT/build/results/* $ORCH_RESULTS

perf_finalize
