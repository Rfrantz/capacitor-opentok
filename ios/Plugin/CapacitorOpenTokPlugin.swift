import Foundation
import Capacitor
import OpenTok

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CapacitorOpenTokPlugin)
public class CapacitorOpenTokPlugin: CAPPlugin, OTPublisherKitDelegate {
    public func publisher(_ publisher: OTPublisherKit, didFailWithError error: OTError) {
        
    }
    

    private var call: CAPPluginCall?
    var session : OTSession?
    var publisher : OTPublisher?
    var subscriber: OTSubscriber?

    /**
     Connect Opentok Client
     */
    @objc func connect(_ call: CAPPluginCall) {
        let apiKey = call.getString("api_key") ?? ""
        let sessionId = call.getString("session_id")  ?? ""
        let token = call.getString("token") ?? ""
        
        if(apiKey == "" || sessionId == "" || token == ""){
            return call.reject("É necessário enviar todos os parametros")
        }
        
        if(self.session == nil)
        {
            self.publisher = nil;

            self.session = OTSession(apiKey: apiKey, sessionId: sessionId, delegate: self as OTSessionDelegate)
            
            var  error: OTError?
            session!.connect(withToken: token, error: &error)
            if(error != nil){
                call.reject("Erro ao conectar sessão", nil, error)
            }
        }
        else
        {
            call.reject("Session is not disconnected. Connect only when session is disconnected.")
        }
    }
    
    /**
     Disconnect Opentok Client
     */
    @objc func disconnect(_ call: CAPPluginCall) {

        if (self.session?.sessionConnectionStatus == OTSessionConnectionStatus.notConnected ) {
            CAPLog.print("Opentok: Erro ao desconectar - Não há conexão ativa")
            call.reject("Opentok: Não há conexão ativa");
        }
        else {
            if (self.session?.sessionConnectionStatus == OTSessionConnectionStatus.connected) {
                CAPLog.print("Opentok: Disconnecting from session")
                self.session?.disconnect(nil)
            } else if(self.session?.sessionConnectionStatus == OTSessionConnectionStatus.connecting) {
                CAPLog.print("Opentok: Waiting Opentok finish connecting to disconnect from session")
            }
            call.resolve();
        }
    }
    
    /**
     Mute/Unmute
     */
    @objc func mute(_ call: CAPPluginCall){
        publisher?.publishAudio.toggle()
        let audioStatus : Bool
        audioStatus = publisher!.publishAudio.self
        call.resolve([
            "enabled":  audioStatus
        ])
    }

}

extension CapacitorOpenTokPlugin: OTSubscriberDelegate {
   public func subscriberDidConnect(toStream subscriber: OTSubscriberKit) {
       print("The subscriber did connect to the stream.")
   }

   public func subscriber(_ subscriber: OTSubscriberKit, didFailWithError error: OTError) {
       print("The subscriber failed to connect to the stream.")
   }
}


/**
 *   Session Delegate
 */
extension CapacitorOpenTokPlugin: OTSessionDelegate {

    
    public func sessionDidConnect(_ session: OTSession) {
        CAPLog.print("Session connected")
    
//        guard let bridge = self.bridge else { return }
        
        let settings = OTPublisherSettings()
        settings.name = UIDevice.current.name
//        settings.audioTrack = false
        guard let publisher = OTPublisher(delegate: self, settings: settings) else {
            return
        }
    

        var error: OTError?
        session.publish(publisher, error: &error)
        guard error == nil else {
            CAPLog.print(error!)
            return
        }

        guard let publisherView = publisher.view else {
            return
        }
        let screenBounds = UIScreen.main.bounds
        publisherView.frame = CGRect(x: screenBounds.width - 200, y: screenBounds.height - 300, width: 150, height: 210)
        
        publisherView.tag = 300
        publisherView.layer.zPosition = 300
        publisherView.layer.cornerRadius = 10
        publisherView.isUserInteractionEnabled = true
        publisherView.clipsToBounds = true
        publisherView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        
        self.publisher = publisher
        DispatchQueue.main.async {
            self.bridge?.viewController?.view.addSubview(publisherView)
        }

    }
    
    public func sessionDidDisconnect(_ session: OTSession) {

        self.publisher = nil
        self.session = nil

        DispatchQueue.main.async {
            let publisherUiView = self.bridge?.viewController?.view.viewWithTag(300)
            publisherUiView?.removeFromSuperview()
            
            let subscriberCamUiView = self.bridge?.viewController?.view.viewWithTag(200)
            subscriberCamUiView?.removeFromSuperview()
        }
        
        self.bridge?.triggerJSEvent(eventName: "capacitorOpentok_sessionDidDisconnect", target: "window")
        CAPLog.print("Session disconnected")
    }
    
    public func session(_ session: OTSession, streamCreated stream: OTStream) {
        CAPLog.print("Session streamCreated: \(stream.streamId)")
        
        subscriber = OTSubscriber(stream: stream, delegate: self)
        guard let subscriber = subscriber else {
            return
        }

        var error: OTError?
        session.subscribe(subscriber, error: &error)
        guard error == nil else {
            print(error!)
            return
        }

        guard let subscriberView = subscriber.view else {
            return
        }
        subscriberView.frame = UIScreen.main.bounds
        let screenBounds = UIScreen.main.bounds
        subscriberView.frame = CGRect(x: 0, y: screenBounds.height - 850, width: screenBounds.width, height: 720)
        
        subscriberView.tag = 200
        subscriberView.isUserInteractionEnabled = true
        
        self.subscriber = subscriber
        DispatchQueue.main.async {
            self.bridge?.viewController?.view.addSubview(subscriberView)
        }
        
    }
    

    
    public func session(_ session: OTSession, streamDestroyed stream: OTStream) {
        self.subscriber?.view?.removeFromSuperview()
        self.publisher?.view?.removeFromSuperview()
        self.publisher = nil
        self.subscriber = nil
        
        self.bridge?.triggerJSEvent(eventName: "capacitorOpentok_streamDestroyed", target: "window")
        CAPLog.print("Session streamDestroyed: \(stream.streamId)")
        self.call?.resolve()
//        subscribers[stream.streamId]?.view!.removeFromSuperview();
//        subscribers.removeValue(forKey: stream.streamId);
    }
    

    public func session(_ session: OTSession, didFailWithError error: OTError) {
        CAPLog.print("session Failed to connect: \(error.localizedDescription)")
        self.call?.resolve()
//
//        if(self.connectResult != nil){
//            self.connectResult(false);
//            self.connectResult = nil;
//        }
//
////        if(self.disconnectResult != nil){
//            self.disconnectResult(nil);
//            self.disconnectResult = nil;
////        }
//
//        self.session = nil;
    }
}
