# Plazacam scripts

These scripts run the Plazacam, which is a Raspberry Pi with two webcams attached.

## Installing

This script is made for Python3, and requires the following packages:

- pygame

### Option A: Configuring cron

This script should be added to a cronjob, since it wil auto-exit on the whole hour.

```crontab
# m h  dom mon dow   command
0 7-21 * * mon-fri python3 /home/pi/webcam.py >/dev/null 2>&1
0 22-6 * * mon-fri python3 /home/pi/webcam.py >/dev/null 2>&1
0 * * * sun,sat python3 /home/pi/webcam.py >/dev/null 2>&1
```

### Option B: Using supervisord

After installing `supervisord`, you can add the following to `/etc/supervisor/conf.d/webcam.conf` folder (or copy `webcam.conf`):

```ini
[program:plazacam]
command=pyton3 webcam.py
directory=/home/gumbo/plazacam/

autostart=true
autorestart=true

# Don't wait for it to start
startsecs=2

# Run as gumbo user
user=gumbo
```

## License

Licensed under [the MIT license](./LICENSE.md).
