#include <Stream.h>
#include <inttypes.h>
#include <avr/sleep.h>
#include <avr/power.h>
#include <avr/pgmspace.h>

/* IM920 */
#include <AltSoftSerial.h>
#include <im920.h>

/* Linksprite */
#include <SoftwareSerial.h>
// JpegCamera - Version: Latest 
#include <JPEGCamera.h>

int isFrontDoorOpened;
int isBackDoorOpened;
SoftwareSerial cameraSerial(11,10);
AltSoftSerial im920Serial;
JPEGCamera camera(cameraSerial);
static int busyPin = 12;
static int resetPin = 4;
IM920& im920 = IM920::Instance();
IM920Frame frame;
PacketOperator& packet = PacketOperator::refInstance(frame);
DataPacket& data = DataPacket::Instance();
NoticePacket& notice = NoticePacket::Instance();

void handleTiltSensorInt(void); // Mailbox front door
void handleReedSwitchInt(void); // Mailbox back door
void Sleep(void);

void setup()
{
    pinMode(3, INPUT_PULLUP);
    Serial.begin(38400);
    cameraSerial.begin(38400);
    im920Serial.begin(38400);
    im920.begin(im920Serial, resetPin, busyPin, 38400);
    
    isFrontDoorOpened = 0;
    isBackDoorOpened = 0;
    
    while (cameraSerial.available() > 0) cameraSerial.read();
    
    camera.reset();
}

void loop() 
{
 	Sleep();  	

	if (isFrontDoorOpened) {
	  isFrontDoorOpened = 0;
	  Serial.println("Front door opened.");
	  notice.reset(frame);
	  notice.setNotice(frame, "Posted");
	  im920.send(frame);
	} else if (isBackDoorOpened) {
	  isBackDoorOpened = 0;
	  Serial.println("Back door opened.");
	  notice.reset(frame);
	  notice.setNotice(frame, "Pulled");
	  im920.send(frame);
	} else {
	  return;
	}

	camera.takePicture();

	for (int size = 0, count = 0, total = camera.getSize(); !camera.isEOF();)
	{
	  data.reset(frame);
		uint8_t* jpeg = packet.getPayloadArray(frame);
		size = camera.readData(jpeg);
		if (!camera.isEOF()) data.setFragment(frame, true);
		data.resetPayloadLength(frame, size);
		data.updatePacketLength(frame);
		im920.send(frame);
		count += size;
    Serial.print(count);
		Serial.print("/");
		Serial.println(total);
	}
	camera.stopPicture();
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
  // attachInterrupt(INT0, handleTiltSensorInt, FALLING);
  attachInterrupt(INT1, handleReedSwitchInt, RISING);
  camera.enterPowerSaving();
  
  set_sleep_mode(SLEEP_MODE_PWR_DOWN);
  sleep_enable();
  
  power_adc_disable();
  power_spi_disable();
  power_timer0_disable();
  power_timer1_disable();
  power_timer2_disable();
  power_twi_disable();
  sleep_mode();
  
  // here after waking from sleep
  sleep_disable();
  power_all_enable();
  Serial.println("Mega shakeeeeen!");
  camera.quitPowerSaving();
  // detachInterrupt(INT0);
  detachInterrupt(INT1);
}
