#!/usr/bin/env bash

test -d ~/bin || mkdir ~/bin

set -e

# Download updater
echo "Downloading updater"
curl \
    --fail \
    --location \
    --output ~/bin/update-plazacam.sh \
    https://github.com/gumbo-millennium/plazacam/raw/main/scripts/update.sh

# Download capture
echo "Downloading capture script"
curl \
    --fail \
    --location \
    --output ~/bin/capture-plazacam.sh \
    https://github.com/gumbo-millennium/plazacam/raw/main/scripts/capture.sh

# Create device ID
if [ ! -f $HOME/device-id.txt ]; then
    echo "Creating device ID"
    cat /proc/sys/kernel/random/uuid > $HOME/device-id.txt
fi

# Fix permissions
chmod u+x ~/bin/capture-plazacam.sh ~/bin/update-plazacam.sh
