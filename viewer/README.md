# Viewer
Arduinoで制御されたセンサーとカメラのモジュール`Mailbox_sender`から送信されて来たポスト投函イベントと画像をブラウザで確認できるようにする。

## Configuration
`config/default.json`にMongoDB、`sensorsystemreceiver`のREST API、カメラモジュールの設置情報を設定をする。

MongoDBはポスト投函／取り出しイベントの保存先データベースとして使用する。MongoDBの設定は"mongodb"項目に記載する。

`sensorsystemreceiver`はREST APIを一つホストしており、`Mailbox_sender`に最新画像の撮影と送信を要求するために使用する。REST APIの設定は"receiverRest"に行い`sensorsystemreceiver`と設定内容を合わせる。ただし、本機能はArduino制御のモジュール機器の消費電力を大幅に抑制するためモジュール側では撮影要求コマンドの受信待機は行わず、従ってシステムとしては機能しない。機能させるには`Mailbox_sender`で間欠的にIM920パケット受信確認するか、HWシリアルポートを複数持つArduinoにてIM920通信モジュールを未使用のHWシリアルポートに接続し、シリアルのHW割り込みを掛けられるようにデザインする必要がある。

カメラモジュールを複数台設置する場合を想定し、モジュールIDと任意のユニークな識別子、Viewerでの表示名を"cameras"に記載する。モジュールIDには、IM920無線フレームデータの送信モジュールの固有IDを指定する。識別名と表示名はHTML内で使用され、それぞれ設置場所に関連したものが望ましい。

## Install
NodeJSパッケージのインストール
```
npm install
```
Bootstrapの再配置
```
mkdir -p public/javascripts/bootstrap
cp node_modules/bootstrap/dist/js/bootstrap.min.js public/javascripts/bootstrap/. 
cp node_modules/bootstrap/dist/js/bootstrap.min.js.map public/javascripts/bootstrap/.
mkdir -p public/stylesheets/bootstrap
cp node_modules/bootstrap/dist/css/bootstrap.min.css public/stylesheets/bootstrap/.
cp node_modules/bootstrap/dist/css/bootstrap.min.css.map public/stylesheets/bootstrap/.
```
jQueryの再配置
```
mkdir -p public/javascripts/jquery
cp node_modules/jquery/dist/jquery.min.js public/javascripts/jquery/.
```
popperの再配置
```
mkdir -p public/javascripts/popper.js/umd
cp node_modules/popper.js/dist/umd/popper.min.js public/javascripts/popper.js/umd/.
cp node_modules/popper.js/dist/umd/popper.min.js.map public/javascripts/popper.js/umd/.
```
open-iconicのダウンロードと配置
```
wget https://github.com/iconic/open-iconic/archive/master.zip
unzip master.zip
mv open-iconic-master public/stylesheets/open-iconic
```

## Run
```
npm start
```
## View
ブラウザで以下のURLを開く 
```
http://<your view server address>:3001
```
