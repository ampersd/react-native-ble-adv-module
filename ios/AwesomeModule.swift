import CoreBluetooth

@objc(AwesomeModule)
class AwesomeModule: NSObject, CBPeripheralManagerDelegate {

  @objc(multiply:withB:withResolver:withRejecter:)
  func multiply(a: Float, b: Float, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
      resolve(a*b)
  }
  
  var peripheralManager: CBPeripheralManager!
  var customName: String = "iPhoneKumpan"
  let _uuidService: CBUUID = CBUUID(string: "00000201-0000-1000-8000-0003E9051C02")
    
    func initPeripheralManager() {
        peripheralManager = CBPeripheralManager(delegate: self, queue: nil)
        //set up the GATT service which we will advertise
        let uuidCharact: CBUUID = CBUUID(string: "00000202-0000-1000-8000-0003E9051C02");
        let _CharacRead: CBMutableCharacteristic =
        CBMutableCharacteristic(type: uuidCharact, properties: [CBCharacteristicProperties.read,  CBCharacteristicProperties.notify], value: nil, permissions: CBAttributePermissions.readable);
        let service: CBMutableService = CBMutableService(type: _uuidService, primary: true);
        service.characteristics = [_CharacRead];

        peripheralManager.add(service);
    }
    
  @objc(turnBluetoothAdvertise:turnOn:withResolver:withRejecter:)
  func turnBluetoothAdvertise(customName: String, turnOn: Bool, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
      if (peripheralManager == nil) {
          initPeripheralManager()
      }
      self.customName = customName
      stopAdvertising()
      if (turnOn) {
// to overcome problem when we've got in logs:
//          [CoreBluetooth] API MISUSE: <CBPeripheralManager: 0x281c1d8c0> can only accept this command while in the powered on state
          
// I moved advertising logic in the separate method, which also should be called once
// inside peripheralManagerDidUpdateState callback when powerOn event is happened
          startAdvertising()
      }
      resolve(customName)
  }
    
    func startAdvertising() {
        // check that status is On, first
        if (peripheralManager.state != CBManagerState.poweredOn) {
            return
        }
        print("startAdvertising")
        let advertisementData = [CBAdvertisementDataLocalNameKey: self.customName, CBAdvertisementDataServiceUUIDsKey: [_uuidService]] as [String : Any]
        peripheralManager.startAdvertising(advertisementData)
    }
    
    func stopAdvertising() {
        if (peripheralManager.isAdvertising) {
            peripheralManager.stopAdvertising()
        }
    }
    
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager)
    {
        print("state: \(peripheral.state)")
        // this should happened when we first time call
        // turnBluetoothAdvertise method
        if (peripheralManager.state == CBManagerState.poweredOn) {
            startAdvertising()
        }
    }
    
    private func peripheralManagerDidStartAdvertising(peripheral: CBPeripheralManager, error: NSError?)
    {
        if let error = error {
            print("Failed… error: \(error)")
            return
        }
        print("Succeeded!")
    }
}
