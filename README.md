# Plazacam scripts

These scripts run the Plazacam, which is a Raspberry Pi with two webcams attached.

This script should be added to a cronjob, often like this:

```crontab
# m h  dom mon dow   command
* 7-21 * * mon-fri /home/pi/webcam.sh >/dev/null 2>&1
*/30 22-6 * * mon-fri /home/pi/webcam.sh >/dev/null 2>&1
*/30 * * * sun,sat /home/pi/webcam.sh >/dev/null 2>&1
```

## License

Licensed under [the MIT license](./LICENSE.md).
