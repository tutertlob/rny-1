# LINE Notifier
LINE Messaging APIを使って通知をする。

## Configuration
`config/default.json`にLINE Messaging APIの設定をする。

* bot.channelAccessToken
* dest.userId

## Install
```
npm install
```

## Run
```
node index.js
```

## Notify
### Receive
```
curl -X POST http://localhost:3000/receive
```
### Pickup
```
curl -X POST http://localhost:3000/pickup
```

