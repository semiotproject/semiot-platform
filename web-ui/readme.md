### Installation

```
npm i -g bower
npm i -g grunt-cli

npm run install-all
```

### Development

```
npm run dev
```

### Buiding

```
npm run build
```

### Troubleshooting

If you have something like
```
Error: EACCES, permission denied '/home/username/.config/configstore/insight-bower.yml'
You don't have access to this file.
```

Try next:
```
sudo chown -R [user name] ~/.config
sudo chown -R [user name] ~/.cache
```