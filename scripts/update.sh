#!/usr/bin/env bash

curl \
    --fail \
    --location \
    --output ~/bin/update-plazacam.sh \
    https://github.com/gumbo-millennium/plazacam/raw/main/scripts/update.sh

curl \
    --fail \
    --location \
    --output ~/bin/capture-plazacam.sh \
    https://github.com/gumbo-millennium/plazacam/raw/main/scripts/capture.sh

chmod u+x ~/bin/capture-plazacam.sh ~/bin/update-plazacam.sh
