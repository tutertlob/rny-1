const config = require('config');
const http = require("http");
const express = require("express");
const bodyParser = require("body-parser");
const line = require('@line/bot-sdk');

const app = express();

const client = new line.Client({
  channelAccessToken: config.bot.channelAccessToken
});

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

/*
 * POST /
 * body { "key": "message key" }
 */
app.post("/", function(req, res) {
  res.header("Content-Type", "application/json; charset=utf-8");

  const key = req.body.key;
  console.log("key : " + key);
  if (key == null) {
    res.status(415);
    res.send({ "status": "error", "detail": "key must not be null" });
    return;
  }

  const msg = config.message[key];
  if (msg == null) {
    res.status(415);
    res.send({ "status": "error", "detail": "message not found" });
    return;
  }

  const message = {
    "type": "text",
    "text": msg
  };

  client.pushMessage(config.dest.userId, message)
  .then(() => {
    res.send({ "status": "success" });
    console.log("success : " + msg);
  })
  .catch((err) => {
    res.status(500);
    res.send({ "status": "error", "detail": err });
    console.log(err);
  });
});

http.createServer(app).listen(config.server.port, config.server.host);
console.log("listening on " + config.server.host + ":" + config.server.port);

