#!/usr/bin/env bash

set -e

if ! [ -f "$HOME/device-id.txt" -a -f "$HOME/device-access-token.txt" ]; then
    echo "Missing some required files, please create the following files first:"
    echo " - ~/device-id.txt containing a UUID to identify this file with"
    echo " - ~/device-access-token.txt containing an access token to use to login with"
    exit 1
fi

CAPTURE_FOLDER="${HOME}/captures"
test -d "${CAPTURE_FOLDER}" || mkdir -p "${CAPTURE_FOLDER}"
cd "${CAPTURE_FOLDER}"

CAMERA_NAME="$1"
CAMERA_ROTATE="${2:-0}"

CAMERA_FILE="${HOME}/capture-$CAMERA_NAME.jpeg"
CAMERA_RESOURCE="/dev/$CAMERA_NAME"

# Check if plazacam exists and is newer than 1 min
if [ -f "$CAMERA_FILE" ] && [ $(find "$CAMERA_FILE" -mmin -1) ]; then
    echo "Cam is up to date"
else
    echo "Cam is not up to date"
    fswebcam \
        --device $CAMERA_RESOURCE \
        --skip 10 \
        --frames 1 \
        --rotate ${CAMERA_ROTATE} \
        --resolution 720x1280 \
        --no-title \
        --no-info \
        --jpeg 90 \
        --save "$CAMERA_FILE"
fi

echo "Uploading $DEVICE_NAME"

DEVICE_NAME="$( cat $HOME/device-id.txt )"
DEVICE_KEY="$( cat $HOME/device-access-token.txt )"

curl \
    --fail \
    --header "Authorization: Bearer $DEVICE_KEY" \
    --header "Accept: application/json" \
    --form "device=$DEVICE_NAME" \
    --form "name=$CAMERA_NAME" \
    --form "_method=PUT" \
    --form "image=@$CAMERA_FILE" \
    https://www.gumbo-millennium.nl/api/webcam/
