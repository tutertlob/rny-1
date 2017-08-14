const config = require('config');
const http = require("http");
const express = require("express");
const line = require('@line/bot-sdk');

const app = express();

const client = new line.Client({
  channelAccessToken: config.bot.channelAccessToken
});

function pushMessage(res, msg) {
  res.header("Content-Type", "application/json; charset=utf-8");

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
};

app.post("/receive", function(req, res) {
  pushMessage(res, config.message.receive);
});

app.post("/pickup", function(req, res) {
  pushMessage(res, config.message.pickup);
});

http.createServer(app).listen(config.server.port, config.server.host);
console.log("listening on " + config.server.host + ":" + config.server.port);

