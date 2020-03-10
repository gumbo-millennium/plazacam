#!/bin/bash

function upload() {
    curl --output /dev/null "$@"
}

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
FILE="${SCRIPT_DIR}/dist/${1}.jpg"

# Config
ENABLE_SPETTER=no

if [ ! -f "$FILE" ]; then
    echo "Cannot find photo $FILE on device."
    exit 1
fi

echo "Sending photo $( basename "$FILE" ) to server..."

declare -A endpoints
endpoints[plaza]="https://www.gumbo-millennium.nl/api/plazacam/0/plaza
endpoints[coffee]="https://www.gumbo-millennium.nl/api/plazacam/0/coffee

ENDPOINT="plaza"
if [ "$( basename "$FILE" )" = "coffeecam.jpg" ]; then
    ENDPOINT="coffee"
fi

# Send request to Gumbo server
curl \
--location \
--connect-time 15 \
--max-time 15 \
--header 'Accept: application/json' \
--request POST \
--form "_method=PUT" \
--form "file=@-" \
"${endpoints[$ENDPOINT]}" <$FILE \
|| true

echo "Done"

