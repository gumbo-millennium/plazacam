#!/usr/bin/env bash
# vim: set ts=4 sw=4 ex :

if [ $# -lt 3 -o $# -gt 5 ]; then
    echo "Usage $0 <title> <filename> <source> [rotation] [frame-skip]"
    exit 1
fi

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TITLE="$1"
FILENAME="$SCRIPT_DIR/dist/$2.jpg"
MISSING_FILE="$HOME/assets/no-cam.png"
SOURCES="$3 $MISSING_FILE test"
TEMPFILE="$( tempfile )"
ROTATE_ANGLE="0"

if [ "$4" != "" ]; then
    ROTATE_ANGLE="$4"
fi

# Allow for different timeouts
WEBCAM_SKIP="${5:-15}"

# Ensure directory existance
echo "Making output directory"
test -d "$( dirname "$FILENAME" )" || mkdir "$( dirname "$FILENAME" )"

# Send message
echo "Requesting photo"

# Make photos
OK=none
for SOURCE in $SOURCES
do
    # Handle test
    if [ "$SOURCE" = "fail" ]; then
        continue
        elif [ "$SOURCE" = "test" ]; then
        METHOD="TEST";
        elif [ "$SOURCE" = "$MISSING_FILE" ]; then
        METHOD="FILE";
    else
        METHOD="V4L2";
        SOURCE="/dev/v4l/by-id/$SOURCE"
    fi

    # Try to make screenshot
    fswebcam \
    --config ./cam-config \
    --skip $WEBCAM_SKIP \
    --device "${METHOD}:${SOURCE}" \
    --title "$TITLE" \
    --rotate "$ROTATE_ANGLE" \
    "$TEMPFILE" \
    2>&1 \
    | ts '> '

    # Check if file exists
    if [ -s "$TEMPFILE" ]; then
        OK=$SOURCE
        break
    fi
done

if [ "$OK" = "none" ]; then
    exit 1
fi

echo "Got cam from $OK"

# Send message
echo "Adding banner"
composite \
-gravity SouthWest "$SCRIPT_DIR/assets/overlay.png" \
"$TEMPFILE" \
"$TEMPFILE" \
2>&1 \
| ts '> '

echo "Adding logo"
composite \
-gravity South "$SCRIPT_DIR/assets/logo.png" \
"$TEMPFILE" \
"$TEMPFILE" \
2>&1 \
| ts '> '

# Add title and timestamp
echo "Adding text to banner"
convert "$TEMPFILE" \
-pointsize 11 \
-draw "gravity SouthEast fill white  text 8,18 '$( date +"%Y-%m-%d %H:%M (%Z)" )'" \
-draw "gravity SouthWest fill white  text 8,18 '${TITLE}'" \
"$FILENAME" \
2>&1 \
| ts '> '


# Send message
echo "Completed processing $TITLE"
