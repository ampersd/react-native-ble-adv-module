@objc(AwesomeModule)
class AwesomeModule: NSObject {

  @objc(multiply:withB:withResolver:withRejecter:)
  func multiply(a: Float, b: Float, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
    resolve(a*b)
  }
  
  @objc(turnBluetoothAdvertise:turnOn:withResolver:withRejecter:)
  func turnBluetoothAdvertise(customName: String, turnOn: Bool, resolve:RCTPromiseResolveBlock,reject:RCTPromiseRejectBlock) -> Void {
    resolve(customName)
  }
}
