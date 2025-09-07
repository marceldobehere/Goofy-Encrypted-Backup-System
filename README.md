# Goofy Encrypted Backup System

## Info
yes

### How to setup
* Create a `conf.json` file where the main settings are
* Create a `secret.key` file where your secret encryption password/passphrase is


### Example `conf.json` file
```json
{
  "secretFile": "./secret.key",
  "inputPaths": [ "./input" ],
  "outputPath": "./output"
}
```

### Example `secret.key` file
```
Incredibly Secure Passwort 12345 amazing yes
```