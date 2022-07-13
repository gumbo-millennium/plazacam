#!/usr/bin/env bash

# Move to dir of script
cd "$( dirname "${BASH_SOURCE[0]}" )"

# Check if plantuml is available
if ! which plantuml > /dev/null; then
    echo "plantuml not found. Please install it first."
    exit 1
fi

# Convert all diagrams
for file in docs/*.puml
do
    filename="$( basename "${file%.*}" )"
    # Write target filename in yellow
    echo -e "Converting \e[33m$filename.puml\e[0m to \e[32m$filename.svg\e[0m"

    cat docs/$filename.puml \
        | plantuml -tsvg -pipe -nometadata \
        | sponge \
        | npx svgo --multipass - \
        | sponge docs/$filename.svg

    # Write result
    echo -e "\e[32mOK\e[0m"
done
