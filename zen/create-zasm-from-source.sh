#!/bin/bash

set -e

echo "#!/usr/bin/java --source 11" > zasm
grep -hE "^import" ../src/zen/zasm/* | sort -u >> zasm
grep -hvE "^import|^package" ../src/zen/zasm/Zasm.java >> zasm
grep --exclude=Zasm.java -hvE "^import|^package" ../src/zen/zasm/* >> zasm
chmod +x zasm
