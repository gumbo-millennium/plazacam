#!/usr/bin/env python3

import requests, json, time
import pygame, pygame.camera, pygame.font

# Loose imports
from datetime import datetime, timedelta
from dateutil import tz
from pygame import Surface
from os.path import isfile

# Prep authentication
if not isfile("cams.json"):
    print("No cams.json file found, please copy cams.dist.json and change accordingly.")
    exit(1)

# Load file
cameraFile = open("cams.json", 'r')
cameraData = cameraFile.read()
cameraFile.close()

# Get camera information
cams = ()
for row in json.loads(cameraData):
    if not row.get("device") or not row.get("name") or not row.get("url"):
        continue

    cams += (
        (
            row.get("name"),
            row.get("device"),
            row.get("url")
        ),
    )

# Validate data
if len(cams) == 0:
    print("No proper cameras found in cams.json")
    exit(1)

# Prep service and cam
pygame.init()
pygame.camera.init()

# Get cameras
availableCams = pygame.camera.list_cameras()

# Ensure all devices in cams exist on this device
for name, device, url in cams:
    if not device in availableCams:
        print(f"{device} for {name} not found on this device")
        exit(1)

# Prep assets
overlay = pygame.image.load("assets/overlay.png")
logo = pygame.image.load("assets/logo.png")
nocam = pygame.image.load("assets/no-cam.png")

# Prep timezome and fonts
timezone = tz.gettz('Europe/Amsterdam')
font = pygame.font.SysFont("Arial", 12)
fontBold = pygame.font.SysFont("Arial", 12, True)

# Add the overlay to the rgb buffer
# Also write the date and time, and the name of the camera
def assign_overlay(surface: Surface, name: str) -> Surface:
    # Add overlay
    surface.blit(overlay, (
        (surface.get_width() - overlay.get_width()) / 2,
        (surface.get_height() - overlay.get_height())
    ))

    # Add logo
    surface.blit(logo, (
        surface.get_width() - logo.get_width() - 16,
        surface.get_height() - logo.get_height()
    ))

    # Add text and date/time
    textTop = surface.get_height() - (16 * 2.5)
    surface.blit(
        fontBold.render(f"{name}", True, (255, 255, 255)),
        (16, textTop)
    )
    surface.blit(
        font.render(datetime.now(timezone).strftime(
            "%d-%m-%Y, om %H:%M:%S (%Z)"
        ), True, (255, 255, 255)),
        (16, textTop + 18)
    )


    return surface

def make_photo(device, name) -> None:
    try:
        print(f"Opening video capture on {device}")
        cam = pygame.camera.Camera(device, (640, 480))
        cam.start()

        print(f"Allow camera to adjust white balance")
        img = cam.get_image()
        time.sleep(1)

        print(f"Capturing image from {device}")
        img = cam.get_image()

        print(f"Terminating camera {device}")
        cam.stop()
    except:
        print(f"Error opening camera {device}")
        img = nocam.copy()

    print("Adding overlay to image")
    img = assign_overlay(img, name)

    print(f"Saving image {device}")
    pygame.image.save(img, f'dist/{name}.jpg')

while 1:
    # Render each camera
    for name, device, url in cams:
        try:
            make_photo(device, name)
        except Exception as e:
            print(f"Error with {device}: {e}")
            continue

    # Submit each photo
    for name, device, url in cams:
        filePath = f'dist/{name}.jpg'
        if not isfile(filePath):
            print(f"No image found for {name}")
            continue

        print(f"Submitting {device} picture as {name}")
        result = requests.request(
            method='POST',
            url=url,
            files={'file': open(filePath, 'rb')},
            data={"_method": "PUT"}
        )

        if result.status_code == 200:
            print(f"{name} submitted successfully")
        else:
            print(f"Error submitting {name}: {result.status_code} {result.reason}")

    # Sleep until the next minute starts
    now = datetime.now()

    # Set time to zero and add a minute
    nextMinute = now.replace(second=0, microsecond=0) + timedelta(minutes=1)

    # Exit the script if the next minute is a full hour
    if nextMinute.minute == 0:
        print("Next minute is a full hour, exiting")
        break
    #
    # # Also exit the script if we're before 7 AM, after 10PM or on weekends
    if nextMinute.hour < 7 or nextMinute.hour >= 22 or nextMinute.weekday() >= 5:
        print("Next minute is outside of working hours, exiting")
        break

    # Figure out the time to sleep
    sleepTime = (nextMinute - now).total_seconds()

    # Report back
    print(f"Sleeping for {sleepTime} seconds")
    time.sleep(sleepTime)
