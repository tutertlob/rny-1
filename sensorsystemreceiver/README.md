# Sensor System Receiver
Arduinoで制御された`Mailbox_notifier`から送信される郵便投函、郵便取り出し通知と画像を受信する。受信した通知はデータベースMongoDBに、
画像はストレージに格納し、`Viewer`によってブラウザからそれらを確認できるようになる。

## Prerequisites
Javaでシリアルポートを使用するため[`RXTX`](http://rxtx.qbang.org/wiki/index.php/Installation_on_Linux)が必要。

以下のURLのインストール手順に沿ってrxtxライブラリをインストールする。

http://rxtx.qbang.org/wiki/index.php/Installation_on_Linux

## Install
Mavenでjarファイルを作成する。
```
mvn package
```
プロパティファイルを`target`ディレクトリにコピーする
```
cp sensorsystemreceiver.properties target/
```

## Configuration
`sensorsystemreceiver.properties`にIM920通信モジュールが接続されているシリアルポートを設定する。
- serial.port
  - IM920が接続されたシリアルポート。

## Run
`java.library.path`にはrxtxのjniライブラリのディレクトリを指定する。
```
java -Djava.library.path=/usr/lib/jni -jar target/sensorsystemreceiver-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Properties
- serial.port/.baud
  - シリアルポートとそのボーレートの指定。IM920本体に設定されているボーレートと一致させる。
- rest.host/.port
  - REST APIを公開するサーバーのアドレスとポート。
- receiver.base/.resource
  - Mailbox_notifierに画像撮影を要求するREST APIのURL。</br>
    ```http://<rest.host>:<rest.port>/<receiver.base>/receiver.resource>```
- database
  - 使用するデータベースを指定する（ただしMongoDBのみ）
- database.host/.port
  - データベースサーバーを指定する。
- database.db/.collection
  - MongoDBで使用するデータベース名とコレクションを指定する。
- filestore.path
  - 画像ファイルの保存ディレクトリを指定する。`Viewer`が外部に公開するフォルダを指定する。
