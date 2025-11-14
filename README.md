<h1>Ponio</h1>

> A virtual gamepad Linux server for the [Ponio Mobile Application](https://github.com/ashudevcodes/ponio_android_controller)

Transform your Android phone into a wireless gamepad for Linux gaming! (ARCH Btw)

<kbd><img width="1919" height="1079" alt="ponioWithGame" src="https://github.com/user-attachments/assets/e1d49089-2e2d-4729-bc17-39385c2a4dd0" /></kbd>

## Features
- Wireless control over TCP (UDP coming soon... i guess)

## Pro Tips for Best Experience
- **Perfect for story-based games** - Works great with single-player adventures
- **No internet needed** - Turn on mobile hotspot or laptop hotspot, connect both devices to it
- **Zero latency** - Direct device-to-device connection = 0ms lag, no buffer

## Requirements
- Linux OS
- Android device with Ponio app installed
- Both devices connected to the same WiFi network

## Usage
1. Run the server 
2. Open the mobile app
3. Connect
4. Game

## Use Cases

- Couch gaming on your HTPC
- Retro gaming with emulators
- Playing story-driven games from your couch
- Testing games with controller input
- Gaming when you don't have a physical gamepad handy

### Download

> [!NOTE]
> This will download **ponio** to the current directory and make the downloaded file executable

```sh 

curl -L -o ponio https://github.com/ashudevcodes/ponio/releases/latest/download/ponio
chmod +x ponio

```
```sh
# To run the server
./ponio
```
## Built With
- [raylib](https://www.raylib.com/) - For UI rendering
- [libevdev](https://www.freedesktop.org/software/libevdev/doc/latest/index.html) - To handle joystick command inputs

## Contributing
Got ideas? Cool. Issues? Sure. Pull requests? Why not

## License

[MIT](./LICENSE)

## Links

- [Ponio Android App](https://github.com/ashudevcodes/ponio_android_controller)

---

**Made with ❤️ for the Linux gaming community**
