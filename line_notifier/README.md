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
npm start
```

## Notify
### Receive
```
curl -X POST -H 'Content-Type:application/json' -d '{"key":"receive"}' http://localhost:3000/
```
### Pickup
```
curl -X POST -H 'Content-Type:application/json' -d '{"key":"pickup"}' http://localhost:3000/
```

