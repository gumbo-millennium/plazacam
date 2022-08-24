#!/usr/bin/env bash

if ! [ -f "$HOME/device-id.txt" -a -f "$HOME/device-access-token.txt" ]; then
    echo "Missing some required files, please create the following files first:"
    echo " - ~/device-id.txt containing a UUID to identify this file with"
    echo " - ~/device-access-token.txt containing an access token to use to login with"
    exit 1
fi

~/bin/do-capture.sh video0 0
~/bin/do-capture.sh video2 180
