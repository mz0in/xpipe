<img src="https://github.com/xpipe-io/xpipe/assets/72509152/88d750f3-8469-4c51-bb64-5b264b0e9d47" alt="drawing" width="250"/>

XPipe is a new type of shell connection hub and remote file manager that allows you to access your entire server infrastructure from your local machine. It works on top of your installed command-line programs and does not require any setup on your remote systems.

XPipe fully integrates with your tools such as your favourite text/code editors, terminals, shells, command-line tools and more. The platform is designed to be extensible, allowing anyone to add easily support for more tools or to implement custom functionality through a modular extension system.

It currently supports:
- [Kubernetes](https://kubernetes.io/) clusters, pods, and containers
- [Docker](https://www.docker.com/), [Podman](https://podman.io/), and [LXD](https://linuxcontainers.org/lxd/introduction/) container instances located on any host
- [SSH](https://www.ssh.com/academy/ssh/protocol) connections, config files, and tunnels
- [Windows Subsystem for Linux](https://ubuntu.com/wsl), [Cygwin](https://www.cygwin.com/), and [MSYS2](https://www.msys2.org/) instances
- [Powershell Remote Sessions](https://learn.microsoft.com/en-us/powershell/scripting/learn/remoting/running-remote-commands?view=powershell-7.3)
- Any other custom remote connection methods that work through the command-line

## Connection Hub

- Easily connect to and access all kinds of remote connections in one place
- Allows you to create specific login environments on any system to instantly jump into a properly set up environment for every use case
- Can create desktop shortcuts that automatically open remote connections in your terminal
- Organize all your connections into hierarchical categories to keep a good overview

![connections](https://github.com/xpipe-io/xpipe/assets/72509152/5df3169a-4150-4478-a3de-ae1f9748c3c8)

## Remote File Manager

- Interact with the file system of any remote system using a workflow optimized for professionals
- Quickly open a terminal session into any directory in your favourite terminal emulator
- Utilize your favourite local programs to open and edit remote files
- Dynamically elevate sessions with sudo when required without having to restart the session
- Integrates with your local desktop environment for a seamless transfer of local files

![browser](https://github.com/xpipe-io/xpipe/assets/72509152/4d4e4e54-17c1-4ebe-acf8-f615cfce8b3f)

## Terminal Launcher

- Automatically login into a shell in your favourite terminal with one click (no need to fill password prompts, etc.)
- Works for all kinds of shells and connections, locally and remote.
- Supports command shells (e.g. bash, PowerShell, cmd, etc.) and some database shells (e.g. PostgreSQL Shell)
- Comes with support for all commonly used terminal emulators across all operating systems
- Supports launches from the GUI or directly from the command-line
- Solves all encoding issues on Windows systems as all Windows shells are launched in UTF8 mode by default

<br>
<p align="center">
  <img src="https://github.com/xpipe-io/xpipe/assets/72509152/02351317-f25d-4af3-8116-bc3b4fb92312" alt="Terminal launcher"/>
</p>
<br>

## Versatile scripting system

- Create reusable simple shell scripts, templates, and groups to run on connected remote systems
- Automatically make your scripts available in the PATH on any remote system without any setup
- Setup shell init environments for connections to fully customize your work environment for every purpose
- Open custom shells and custom remote connections by providing your own commands

![scripts](https://github.com/xpipe-io/xpipe/assets/72509152/56533f22-b689-4201-b58a-eebe0a6d517a)

## Secure Vault

- Securely stores all information exclusively on your system,
  optionally with a custom master passphrase to further encrypt secrets
- Supports syncing your vault data via your own remote git repository
- Can integrate with your password manager to fetch secrets and not store them itself

# Downloads

Note that this is a desktop application that should be run on your local desktop workstation, not on any server or containers. It will be able to connect to your server infrastructure from there.

## Windows

Installers are the easiest way to get started and come with an optional automatic update functionality:

- [Windows .msi Installer (x86-64)](https://github.com/xpipe-io/xpipe/releases/latest/download/xpipe-installer-windows-x86_64.msi)

You can also install XPipe by pasting the installation command into your terminal. This will perform the setup automatically:

```
powershell -ExecutionPolicy Bypass -Command iwr "https://raw.githubusercontent.com/xpipe-io/xpipe/master/get-xpipe.ps1" -OutFile "$env:TEMP\get-xpipe.ps1" ";"  "&" "$env:TEMP\get-xpipe.ps1"
```

If you don't like installers, you can also use a portable version that is packaged as an archive:

- [Windows .zip Portable (x86-64)](https://github.com/xpipe-io/xpipe/releases/latest/download/xpipe-portable-windows-x86_64.zip)

Alternatively, you can also use [choco](https://community.chocolatey.org/packages/xpipe) to install it with `choco install xpipe`.

## Linux

You can install XPipe by pasting the installation command into your terminal. This will perform the setup automatically.
The script supports installation via `apt`, `dnf`, `yum`, `zypper`, `rpm`, and `pacman` on Linux:

```
bash <(curl -sL https://raw.githubusercontent.com/xpipe-io/xpipe/master/get-xpipe.sh)
```

### Debian-based distros

The following debian installers are available:

- [Linux .deb Installer (x86-64)](https://github.com/xpipe-io/xpipe/releases/latest/download/xpipe-installer-linux-x86_64.deb)
- [Linux .deb Installer (ARM 64)](https://github.com/xpipe-io/xpipe/releases/latest/download/xpipe-installer-linux-arm64.deb)

Note that you should use apt to install the package with `sudo apt install <file>` as other package managers, for example dpkg,
are not able to resolve and install any dependency packages.

### RHEL-based distros

The following rpm installers are available:

- [Linux .rpm Installer (x86-64)](https://github.com/xpipe-io/xpipe/releases/latest/download/xpipe-installer-linux-x86_64.rpm)
- [Linux .rpm Installer (ARM 64)](https://github.com/xpipe-io/xpipe/releases/latest/download/xpipe-installer-linux-arm64.rpm)

The same applies here, you should use a package manager that supports resolving and installing required dependencies if needed.

### Arch

There is an official [AUR package](https://aur.archlinux.org/packages/xpipe) available that you can either install manually or via an AUR helper such as with `yay -S xpipe`.

### NixOS

There's an official [xpipe nixpkg](https://search.nixos.org/packages?channel=unstable&show=xpipe&from=0&size=50&sort=relevance&type=packages&query=xpipe) available that you can install with `nix-env -iA nixos.xpipe`. This one is however not always up to date.

There is also a custom repository that contains the latest up-to-date releases: https://github.com/xpipe-io/nixpkg.
You can install XPipe by following the instructions in the linked repository.

### Portable

In case you prefer to use an archive version that you can extract anywhere, you can use these:

- [Linux .tar.gz Portable (x86-64)](https://github.com/xpipe-io/xpipe/releases/latest/download/xpipe-portable-linux-x86_64.tar.gz)
- [Linux .tar.gz Portable (ARM 64)](https://github.com/xpipe-io/xpipe/releases/latest/download/xpipe-portable-linux-arm64.tar.gz)

Note that this assumes that you have some basic packages for graphical systems already installed
as it is not a perfect standalone version. It should however run on most systems.

## macOS

Installers are the easiest way to get started and come with an optional automatic update functionality:

- [MacOS .pkg Installer (x86-64)](https://github.com/xpipe-io/xpipe/releases/latest/download/xpipe-installer-macos-x86_64.pkg)
- [MacOS .pkg Installer (ARM 64)](https://github.com/xpipe-io/xpipe/releases/latest/download/xpipe-installer-macos-arm64.pkg)

You also can install XPipe by pasting the installation command into your terminal. This will perform the `.pkg` install automatically:

```
bash <(curl -sL https://raw.githubusercontent.com/xpipe-io/xpipe/master/get-xpipe.sh)
```

If you don't like installers, you can also use a portable version that is packaged as an archive:

- [MacOS .dmg Portable (x86-64)](https://github.com/xpipe-io/xpipe/releases/latest/download/xpipe-portable-macos-x86_64.dmg)
- [MacOS .dmg Portable (ARM 64)](https://github.com/xpipe-io/xpipe/releases/latest/download/xpipe-portable-macos-arm64.dmg)

Alternatively, you can also use [Homebrew](https://github.com/xpipe-io/homebrew-tap) to install XPipe with `brew install --cask xpipe-io/tap/xpipe`.

# Further information

## Open source model

XPipe utilizes an open core model, which essentially means that the main application is open source while certain other components are not. Select parts are not open source yet, but may be added to this repository in the future.

This mainly concerns the features only available in the professional tier and the shell handling library implementation. Furthermore, some tests and especially test environments and that run on private servers are also not included in this repository.

## More links

You have more questions? Then check out the [FAQ](https://xpipe.io/faq).

For information about the security model of XPipe, see the [security page](https://docs.xpipe.io/security).

For information about the privacy policy of XPipe, see the [privacy page](https://docs.xpipe.io/privacy-policy).

In case you're interested in development, check out the [contributing page](/CONTRIBUTING.md).

[![Discord](https://discordapp.com/api/guilds/979695018782646285/widget.png?style=banner2)](https://discord.gg/8y89vS8cRb)
