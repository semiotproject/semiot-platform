### Desired pre-requests
node.js version >= 4
npm version >= 3

#### How to upgrade to latest npm
```
sudo npm i -g npm
```

#### How to install latest node.js
Use [n](https://github.com/tj/n)

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

### Building

```
npm run build
```

### Launching local server

```
npm run serve
```

### Troubleshooting

*Question*: I have an error installing packages with bower:
```
Error: EACCES, permission denied '/home/username/.config/configstore/insight-bower.yml'
You don't have access to this file.
```

*Solution*: add permissions to next folders:
```
sudo chown -R [user name] ~/.config
sudo chown -R [user name] ~/.cache
```

*Question*: I have an error with npm:
```
npm i
npm http GET https://registry.npmjs.org/highcharts/0.0.9
npm http 304 https://registry.npmjs.org/highcharts/0.0.9
npm ERR! tar.unpack untar error /home/nikolay/.npm/highcharts/0.0.9/package.tgz
npm ERR! Error: unexpected eof
npm ERR!     at decorate (/usr/lib/nodejs/fstream/lib/abstract.js:67:36)
npm ERR!     at Extract.Abstract.error (/usr/lib/nodejs/fstream/lib/abstract.js:61:12)
npm ERR!     at Extract._streamEnd (/usr/lib/nodejs/tar/lib/extract.js:75:22)
npm ERR!     at BlockStream.<anonymous> (/usr/lib/nodejs/tar/lib/parse.js:50:8)
npm ERR!     at BlockStream.EventEmitter.emit (events.js:92:17)
npm ERR!     at BlockStream._emitChunk (/usr/lib/nodejs/block-stream.js:203:10)
npm ERR!     at BlockStream.resume (/usr/lib/nodejs/block-stream.js:58:15)
npm ERR!     at Extract.Reader.resume (/usr/lib/nodejs/fstream/lib/reader.js:252:34)
npm ERR!     at Entry.<anonymous> (/usr/lib/nodejs/tar/lib/parse.js:256:8)
npm ERR!     at Entry.EventEmitter.emit (events.js:92:17)
npm ERR! If you need help, you may report this log at:
npm ERR!     <http://github.com/isaacs/npm/issues>
npm ERR! or email it to:
npm ERR!     <npm-@googlegroups.com>
```

*Solution*: clean npm cache:
```
sudo npm cache clean
```
