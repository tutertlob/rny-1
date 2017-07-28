const line = require('@line/bot-sdk');
const config = require('config');

const client = new line.Client({
  channelAccessToken: config.bot.channelAccessToken
});

const message = {
  type: 'text',
  text: config.message.receive
};

client.pushMessage(config.dest.userId, message)
.then(() => {
  console.log("success");
}).catch((err) => {
  console.log(err);
});

