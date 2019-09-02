#!/bin/bash
PWD=$(dirname "$0")
cd "$PWD/.."
PJD=`pwd`
PBD="${PJD}/protobuf"
JSD="../im-node/src/sdk/protobuf"
JAD="../im-server/src/main/java"
cd $PBD
for file in *
do
  if [[ -f $file ]] && [[ $file == *.proto ]]; then
    echo "compile ${file}"
    protoc --js_out=import_style=commonjs,binary:${JSD} ${file}
    protoc --java_out=${JAD} ${file}
  fi
done
cd $PWD

