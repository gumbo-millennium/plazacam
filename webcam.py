#!/usr/bin/env python3

import requests
import json
import time
import pygame.camera
import pygame.font

# Loose imports
from datetime import datetime, timedelta
from dateutil import tz
from pygame import Surface
from os.path import isfile

# Prep authentication
if not isfile("cams.json"):
    print("ERROR: No cams.json file found, please copy cams.dist.json and change accordingly.")
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
            row.get("url"),
            row.get("config", {})
        ),
    )

# Validate data
if len(cams) == 0:
    print("ERROR: No proper cameras found in cams.json")
    exit(1)

# Prep service and cam
pygame.camera.init()
pygame.font.init()

# Get cameras
availableCams = pygame.camera.list_cameras()

# Ensure all devices in cams exist on this device
for name, device, url, options in cams:
    if not device in availableCams:
        print(f"WARNING: {device} for {name} not found on this device")

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


def make_photo(device: str, name: str, config: dict) -> None:
    try:
        print(f"Opening video capture on {device}")
        cam = pygame.camera.Camera(device, (640, 480))
        cam.start()

        # Apply settings
        print(f"Applying settings to camera")
        try:
            cam.set_controls(
                hflip=config.get("hflip", False),
                vflip=config.get("vflip", False),
                brightness=config.get("brightness", 0),
            )

            print(f"Set settings: {config}")
            print(f"Current settings: {cam.get_controls()}")
        except Exception as e:
            print("WARNING: Failed to apply settings to camera")
            print(f"         {e}")

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


def sleep_until_next_minute() -> None:
    # Sleep until the next minute starts
    now = datetime.now()

    # Set time to zero and add a minute
    nextMinute = now.replace(second=0, microsecond=0) + timedelta(minutes=1)

    # Figure out the time to sleep
    sleepTime = (nextMinute - now).total_seconds()

    # Report back
    print(f"Sleeping for {sleepTime} seconds")

    # And 😴
    try:
        time.sleep(sleepTime)
    except KeyboardInterrupt:
        print("Recieved keyboard interrupt, exiting.")
        exit(0)


firstRun = True

while 1:
    # Check if we need to run this loop
    if firstRun:
        print("Initial run, submitting picture...")
        firstRun = False
    else:
        now = datetime.now()
        if (now.hour < 7 or now.hour >= 22 or now.weekday() < 5) and now.minute != 7:
            print("Skipping current run")
            sleep_until_next_minute()
            continue

    # Render each camera
    for name, device, url, config in cams:
        try:
            make_photo(device, name, config)
        except Exception as e:
            print(f"Error with {device}: {e}")
            continue

    # Submit each photo
    for name, device, url, config in cams:
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

    print("Run complete")
    sleep_until_next_minute()
