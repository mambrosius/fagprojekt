// arduinoCan_rx.ino
// Anders Helbo
// Morten Ambrosius

#include <SPI.h>
#include "mcp_can.h"

// the cs pin of the version is default to D9 (v0.9b and v1.0 is default D10)
const int SPI_CS_PIN = 9;
int minID = 0;
int maxID = 4095;

MCP_CAN CAN(SPI_CS_PIN);  

void setup() {

  Serial.begin(115200);

  //init can bus : baudrate = 500k
  while (CAN_OK != CAN.begin(CAN_500KBPS)) {             
    
    Serial.println("arduinoCan_rx init fail");
    Serial.println(" Init CAN BUS Shield again");
    
    delay(100);
    
  }

  //Serial.println("CAN BUS Shield init ok!");
}

void loop() {

  unsigned char len = 0;
  unsigned char buf[8];

  if(Serial.available()){
    minID = Serial.parseInt();
    maxID = Serial.parseInt();
    Serial.flush();
  }

  // check if data coming
  if(CAN_MSGAVAIL == CAN.checkReceive()) {

    CAN.readMsgBuf(&len, buf);    // read data,  len: data length, buf: data buf

    //[canId, rtr, ext, data, errorFlag]

   
   if(minID <= CAN.getCanId() && CAN.getCanId() <= maxID){
      Serial.print(CAN.getCanId(),HEX);
      Serial.print("_");
      Serial.print(CAN.isRemoteRequest(),HEX);
      Serial.print("_");
      Serial.print(CAN.isExtendedFrame(),HEX);
      Serial.print("_");
      Serial.print(len,HEX);
      Serial.print("_");

      // print data
      Serial.print(buf[0], HEX);

      for (int i = 1; i<len; i++) {
        Serial.print(" ");
        Serial.print(buf[i], HEX);
      }

      Serial.print("_");
      Serial.println(CAN.checkError(),HEX);   
      delay(150);
    }
  
  }
}
