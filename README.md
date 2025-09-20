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

### TODOs
* Have some kind of ignore system, probably regex-ish based?
* Save the main.conf file several times if the process fails midway
* Have some kind of progress indicator in the output (maybe an ETA & speed?)
* Delete all files first, then all folders
* Log all attempts (success, error, warn, etc.) into log files
* Turn into a docker image thingy
* Set the restore path in either the conf or have it asked in the cli
* Have a check for remaining space before doing backups incase of insufficient space
* Keep older versions of the main.conf or have some sort of snapshot system
* Some kind of extra info in the files in case the main.conf gets lost?
* Maybe store the main.conf as a recursive tree instead of a giant list to save space