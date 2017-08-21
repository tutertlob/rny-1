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
curl -X POST -H 'Content-Type:application/json' -d '{"keys":"receive"}' http://localhost:3000/
```
### Pickup
```
curl -X POST -H 'Content-Type:application/json' -d '{"keys":"pickup"}' http://localhost:3000/
```

