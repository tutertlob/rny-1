#include <Stream.h>
#include <inttypes.h>
#include <avr/sleep.h>
#include <avr/power.h>
#include <avr/pgmspace.h>
#include <avr/io.h>
#include <avr/interrupt.h>

/* IM920 */
// AltSoftSerial - Version: 1.4.0
#include <AltSoftSerial.h>
// im920-arduino - Version: Latest 
#include <im920.h>

/* Linksprite */
#include <SoftwareSerial.h>
// JpegCamera - Version: Latest 
#include <JPEGCamera.h>

#define CAPTURE_COMMAND_CAPABLE
#define CAMERA_BAUDRATE 38400
#define IM920_BAUDRATE  38400
#define SERIAL_BAUDRATE 38400

static int isFrontDoorOpened = 0;
static int isBackDoorOpened = 0;
#ifdef CAPTURE_COMMAND_CAPABLE
static int isCaptureCommandReceived = 0;
#endif // CAPTURE_COMMAND_CAPABLE

SoftwareSerial cameraSerial(7,6);
AltSoftSerial im920Serial;
JPEGCamera camera(cameraSerial);
static int busyPin = 12;
static int resetPin = 4;
static int cameraPowerPin = 11;
IM920& im920 = IM920::Instance();
IM920Frame frame;
DataPacket& data = DataPacket::Instance();
NoticePacket& notice = NoticePacket::Instance();

void handleTiltSensorInt(void); // Mailbox front door
void handleReedSwitchInt(void); // Mailbox back door
void Sleep(void);

void turnOnCameraPowerSource(void);
void turnOffCameraPowerSource(void);

void setup()
{
  pinMode(2, INPUT_PULLUP);
  pinMode(3, INPUT_PULLUP);
  
  pinMode(cameraPowerPin, OUTPUT);
  digitalWrite(cameraPowerPin, LOW);
  turnOffCameraPowerSource();
  
  Serial.begin(SERIAL_BAUDRATE);
  cameraSerial.begin(CAMERA_BAUDRATE);
  im920Serial.begin(IM920_BAUDRATE);
  im920.begin(im920Serial, resetPin, busyPin, IM920_BAUDRATE);
  
  isFrontDoorOpened = 0;
  isBackDoorOpened = 0;
#ifdef CAPTURE_COMMAND_CAPABLE
  isCaptureCommandReceived = 0;
#endif // CAPTURE_COMMAND_CAPABLE
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
	}
	
	if (isBackDoorOpened) {
	  isBackDoorOpened = 0;
	  Serial.println("Back door opened.");
	  notice.reset(frame);
	  notice.setNotice(frame, "Pulled");
	  im920.send(frame);
	}
	
#ifdef CAPTURE_COMMAND_CAPABLE
	if (isCaptureCommandReceived) {
    isCaptureCommandReceived = 0;
	  Serial.println("Capture command received.");
	  notice.reset(frame);
	  notice.setNotice(frame, "Captured");
	  im920.send(frame);
  }
#endif // CAPTURE_COMMAND_CAPABLE

	camera.takePicture();

	for (int size = 0, count = 0, total = camera.getSize(); !camera.isEOF();)
	{
	  data.reset(frame);
		uint8_t* jpeg = data.getPayloadArray(frame);
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

#ifdef CAPTURE_COMMAND_CAPABLE
// An isr for PCINT0_vect has been already declared in the SoftwareSerial source
// so this causes a compile error of multiple defined of it
#ifndef PCINT0_vect
ISR(PCINT0_vect)
{
  isCaptureCommandReceived = 1;
  PCMSK0 &= ~0x01;
  sleep_disable();
}
#endif

void enableWakeUpSource()
{
  PCMSK0 |= 1;
  PCICR |= 1;
}

void disableWakeUpSource()
{
  PCMSK0 &= ~0x01;
  PCICR &= ~0x1;
}
#endif // CAPTURE_COMMAND_CAPABLE

void Sleep(void) {
  Serial.println("I'm so sleepy.");
  Serial.flush();
  
  camera.enterPowerSaving();
#ifndef CAPTURE_COMMAND_CAPABLE
  im920.getInterface().enableSleep();
#endif // CAPTURE_COMMAND_CAPABLE
  
  // set tx pin low to reduce current consumption
  cameraSerial.end();
  turnOffCameraPowerSource();
  pinMode(6, OUTPUT);
  digitalWrite(6, LOW);
  
  set_sleep_mode(SLEEP_MODE_PWR_DOWN);
  sleep_enable();
  
#ifdef CAPTURE_COMMAND_CAPABLE
  enableWakeUpSource();
#endif // CAPTURE_COMMAND_CAPABLE
  attachInterrupt(INT0, handleTiltSensorInt, FALLING);
  attachInterrupt(INT1, handleReedSwitchInt, FALLING);
  
  sleep_mode();
  
  // here after waking from sleep
  sleep_disable();
  power_adc_disable();
  power_spi_disable();
  power_twi_disable();
#ifdef CAPTURE_COMMAND_CAPABLE
  disableWakeUpSource();
#endif // CAPTURE_COMMAND_CAPABLE

  detachInterrupt(INT0);
  detachInterrupt(INT1);
  
  cameraSerial.begin(CAMERA_BAUDRATE);
  
#ifndef CAPTURE_COMMAND_CAPABLE
  im920.getInterface().disableSleep();
#endif // CAPTURE_COMMAND_CAPABLE
  turnOnCameraPowerSource();
  camera.quitPowerSaving();
  
#ifdef CAPTURE_COMMAND_CAPABLE
  while (im920Serial.available() >0) {
    im920Serial.read();
    isCaptureCommandReceived = 1;
  }
#endif // CAPTURE_COMMAND_CAPABLE
  
  Serial.println("Mega shakeeeeen!");
  Serial.flush();
}

void turnOnCameraPowerSource(void) {
  digitalWrite(cameraPowerPin, HIGH);
  delay(100);
  
  while (cameraSerial.available() > 0) cameraSerial.read();
  
  camera.reset();
}

void turnOffCameraPowerSource(void) {
  digitalWrite(cameraPowerPin, LOW);
}
