#!/usr/bin/env bash

set -e

if ! [ -f "$HOME/device-id.txt" -a -f "$HOME/device-access-token.txt" ]; then
  echo "Missing some required files, please create the following files first:"
  echo " - ~/device-id.txt containing a UUID to identify this file with"
  echo " - ~/device-access-token.txt containing an access token to use to login with"
  exit 1
fi

DEVICE_NAME="$( cat $HOME/device-id.txt )"
DEVICE_KEY="$( cat $HOME/device-access-token.txt )"

CAPTURE_FOLDER="${HOME}/captures"
test -d "${CAPTURE_FOLDER}" || mkdir -p "${CAPTURE_FOLDER}"
cd "${CAPTURE_FOLDER}"

PLAZACAM_FILE="$CAPTURE_FOLDER/plazacam.jpeg"
COFFEECAM_FILE="$CAPTURE_FOLDER/coffeecam.jpeg"
PLAZACAM_RESOURCE=/dev/video0
COFFEECAM_RESOURCE=/dev/video2

# Check if plazacam exists and is newer than 1 min
if [ -f "$PLAZACAM_FILE" ] && [ $(find "$PLAZACAM_FILE" -mmin -1) ]; then
    echo "Plazacam is up to date"
else
    echo "Plazacam is not up to date"
    streamer \
        -w 2 \
        -o "$PLAZACAM_FILE" \
        -c "$PLAZACAM_RESOURCE" \
        -s 640x480 \
        -j 95
fi

# Check if coffeecam exists and is newer than 1 min
if [ -f "$COFFEECAM_FILE" ] && [ $(find "$COFFEECAM_FILE" -mmin -1) ]; then
    echo "Coffeecam is up to date"
else
    echo "Coffeecam is not up to date"
    streamer \
        -w 2 \
        -o "$COFFEECAM_FILE" \
        -c "$COFFEECAM_RESOURCE" \
        -s 640x480 \
        -j 95
fi

echo "Uploading Plazacam"
curl \
    --fail \
    --header "Authorization: Bearer $DEVICE_KEY" \
    --header "Accept: application/json" \
    --form "device=$DEVICE_NAME" \
    --form "name=plazacam" \
    --form "_method=PUT" \
    --form "image=@$PLAZACAM_FILE" \
    https://www.gumbo-millennium.nl/api/webcam/

echo "Uploading Coffeecam"
curl \
    --fail \
    --header "Authorization: Bearer $DEVICE_KEY" \
    --header "Accept: application/json" \
    --form "device=$DEVICE_NAME" \
    --form "name=coffeecam" \
    --form "_method=PUT" \
    --form "image=@$COFFEECAM_FILE"\
    https://www.gumbo-millennium.nl/api/webcam/
