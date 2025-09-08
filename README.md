# Goofy Encrypted Backup System

## Info
A tool for me to backup specific folders to a remote location while keeping them encrypted.

Also directly has the ability to restore it all if your local data gets lost.

You do need to store your secret key and salt securely though xd

Still W.I.P

### How to setup
* Create a `conf.json` file where the main settings are
* Create a `secret.key` file where your secret encryption password/passphrase is
* Start the program for the first time, you can choose `exit`
* Store your `secret.key` and `conf.json` securely! 
  * There is a randomly generated salt in the `config.json` which you need!

### Example `conf.json` file
```json
{
  "secretFile": "./secret.key",
  "inputPaths": [ "./input" ],
  "outputPath": "./output",
  "syncDeletions": false,
  "logs": true
}
```

### Example `secret.key` file
```
Incredibly Secure Passwort 12345 amazing yes
```

### Usage
You can either pass command line arguments to it or just type your option into the shell.

There are currently 4 options:
* `exit` - Exits the program
* `backup` - Starts the backup process and writes the data to your output location (incremental)
* `backup-clean` - Starts a completely new backup process and writes all the files / deletes most of the old data as well
* `restore` - Starts a restoration process and saves the data in your local directory.

### Notes
Apparently it can use a lot of ram LOL. I will optimize it for that sometime.

And it doesn't delete everything yet, sometimes it leaves some file remains on the remote backup directory.

There are some other things like multithreading that id like to add as well.

Also turning this into a docker container so i can put it on my nas where it belongs.

But for now its a good start xd