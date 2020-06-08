#include "Mailbox_sender_ide.h"		// Additional Header
#include "driver_gpio.h"

#undef __assert
#define __assert    __assert_print

#define MAILBOX_SENDER_PANID 0x1198
#define MAILBOX_SENDER_GW_ADDR16  0x2ef0

#define CAMERA_SERIAL   Serial3
#define CAMERA_PWRPIN   4

#define PIN2  2
#define PIN3  3
#define INT0_PIN  (PIN2)
#define INT1_PIN  (PIN3)

#define JPEG_READ_SIZE  (128)

void handleTiltSensorInt(void); // Mailbox front door
void handleReedSwitchInt(void); // Mailbox back door
void Sleep(void);

int isFrontDoorOpened;
int isBackDoorOpened;
Packet *packet;
Data *iData;
Command *iCmd;

void setup() {
  // put your setup code here, to run once:
  pinMode(INT0_PIN, INPUT_PULLUP);
  pinMode(INT1_PIN, INPUT_PULLUP);
  pinMode(CAMERA_PWRPIN, OUTPUT);
  digitalWrite(CAMERA_PWRPIN, HIGH); // turn on power of camera
  delay(1000);

  Serial.begin(115200);

  digitalWrite(25,HIGH);
  Camera.begin(CAMERA_BAUD_38400);
  digitalWrite(26,HIGH);
  Wireless.init();
  Wireless.begin(36, 0x1198, SUBGHZ_100KBPS, SUBGHZ_PWR_20MW);
  // Wireless.setAckReq(false);

  isFrontDoorOpened = 0;
  isBackDoorOpened = 0;

  packet = Packet_new();
}

void loop() {
  Serial.println("loop\n");
  Sleep();

  if (isFrontDoorOpened) {
    isFrontDoorOpened = 0;
    Serial.println("Front door opened.");
    Wireless.sendNotice(MAILBOX_SENDER_PANID, MAILBOX_SENDER_GW_ADDR16, "Posted");
    DEBUG_PRINT("Posted");
  }
  
  if (isBackDoorOpened) {
    isBackDoorOpened = 0;
    Serial.println("Back door opened.");
    Wireless.sendNotice(MAILBOX_SENDER_PANID, MAILBOX_SENDER_GW_ADDR16, "Pulled");
    DEBUG_PRINT("Pulled");
  }
  
  Packet_initialize(packet);
  if (Wireless.listen(packet) == 0) {
    if (Packet_getType(packet) != COMMAND) return;
    iCmd = (Command *)Packet_getInterface(packet);
    if (iCmd->getCommand(packet) != 0x80) return;
  }

  {
    int size, count, total;

    Camera.takePicture();

    for (size = 0, count = 0, total = Camera.getSize(); !Camera.isEOF();)
    {
      uint8_t* jpeg = NULL;

      Packet_initialize(packet);
      Packet_setType(packet, DATA);
      iData = (Data *)Packet_getInterface(packet);

      jpeg = iData->getDataArray(packet);
      size = Camera.readData(jpeg, JPEG_READ_SIZE);
      if (!Camera.isEOF()) iData->setFragmented(packet, true);
      iData->resetDataSize(packet, size);
      Wireless.send(packet, MAILBOX_SENDER_PANID, MAILBOX_SENDER_GW_ADDR16);
      count += size;
      
      Serial.print_long((long)count, DEC);
      Serial.print("/");
      Serial.println_long((long)total, DEC);
    }
    Camera.stopPicture();
  }
}

void handleTiltSensorInt() {
  ++isFrontDoorOpened;
}

void handleReedSwitchInt() {
  ++isBackDoorOpened;
}

void Sleep(void) {
  Serial.println("I'm so sleepy.");
  Serial.flush();

  attachInterrupt(0, handleTiltSensorInt, FALLING);
  attachInterrupt(1, handleReedSwitchInt, FALLING);
  
  Camera.enterPowerSaving();
  delay(200);
  digitalWrite(CAMERA_PWRPIN, LOW); // turn off power of camera
  
  Serial.end();

  drv_digitalWrite(11,HIGH); // PWR LED OFF
  stop_mode();
  drv_digitalWrite(11,LOW);  // PWR LED ON

  // here after waking from sleep
  digitalWrite(CAMERA_PWRPIN, HIGH); // turn on power of camera
  delay(1000);
  Serial.begin(115200);
  Serial.println("Mega shakeeeeen!");
  Camera.quitPowerSaving();
  detachInterrupt(0);
  detachInterrupt(1);
}
