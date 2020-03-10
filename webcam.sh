#!/bin/bash

function upload() {
    curl --output /dev/null "$@"
}

echo "Getting plazacam..."
./make-photo.sh "Plazacam" plazacam "usb-046d_B525_HD_Webcam_76B58D50-video-index0" 180 25

echo "Uploading"
./upload-photo.sh plazacam

echo "Waiting..."
sleep 5s

echo "Getting koffiecam..."
./make-photo.sh "Koffiecam" coffeecam "usb-Sonix_Technology_Co.__Ltd._USB_2.0_Camera-video-index0" 0

echo "Uploading"
./upload-photo.sh coffeecam

echo "Waiting..."
sleep 5s

echo "Done"

