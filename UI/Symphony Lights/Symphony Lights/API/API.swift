//
//  API.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 05/01/2019.
//  Copyright © 2019 Symphony. All rights reserved.
//

import Foundation
import SwiftWebSocket
//import SwiftyBeaver
//import os.log

class LEDLightAPIManager {
    
    // MARK: Properties
//    private let log = SwiftyBeaver.self
    
    private let socket: WebSocket
    private let url: String
    
    // MARK: Initialization
    init(url: String, reconnecting: Bool) {
        self.url = url
        socket = WebSocket(url)
        
        LOG.debug("Connecting to web socket @ " + url)
        
        socket.event.open = {
            LOG.debug("Web socket opened @ " + url)
        }

        socket.event.close = { code, reason, clean in
            LOG.error("Web socket closed @ " + url)
            #if AWAY
                LOG.debug("Will reconnect if device is NEAR")
            #else
                if reconnecting {
                    self.socket.open()
                }
            #endif
        }
        
        socket.event.message = { (message) in
            if let msg = message as? String {
                LOG.verbose(url + " : " + msg)
                //                callback(msg)
            }
        }

        socket.event.error = { error in
            LOG.error("Web socket error @ " + url + " with error: " + error.localizedDescription)
        }
    }
    
    // MARK: Methods
    
    func send(_ data: String) {
//        debounce(delay: 0.1, queue: DispatchQueue.main, action: {
        LOG.verbose("Sending " + data + " to " + self.url)
        self.socket.send(data)
//        })
//        debouncedFunc();
    }
    
    func setStaticColor(hexColor: String) {
        let color = convertHEXtoRGB(hexColor: hexColor)
        setStaticColor(r: color.red, g: color.green, b: color.blue)
    }
    
    func setStaticColor(r: UInt8, g: UInt8, b: UInt8) {
//        socket.send("{'cmd': 4,'data': [\(r), \(g), \(b)]}")
        
        send("{'core': 7,'cmd': 19,'data': [\(r), \(g), \(b)]}")
    }
    
    /**
     Sends a *Set Brightness* request to the LED strip
     
     
    */
    func setBrightness(_ brightness: Int) {
        send("{'core': 7,'cmd': 22, val: \(brightness)}")
    }
    
    func setSequences(_ sequences: [LightPattern]) {
        var seqStr: String = "["
        for seq in sequences {
            let colorOrPalette: String
            if seq.getColor() != nil {
                colorOrPalette = seq.getColor()!.hexValue()
            } else {
                colorOrPalette = seq.getColorPalette()!
            }
            seqStr.append("""
                {
                'ptrn': \(seq.getPatternSequence()!),
                'color': \(colorOrPalette),
                'dur': \(seq.getDuration()!)
                },
                """)
        }
        seqStr.removeLast()
        seqStr.append("]")
        
        send("""
            {
            'core': 7,
            'cmd': 11,
            'seq': \(seqStr)
            }
            """)
    }
    
    func testSequence(ptrn: String, hexOrPalette: String) {
        send("{'core': 7, 'cmd': 16, 'ptrn': '\(ptrn)', color: '\(hexOrPalette)'}")
    }
    
    func resumeNormalOperation() {
        send("{'core': 7, 'cmd': 17}")
    }
    
    func powerOn() {
        send("{'core': 7, 'cmd': 21, 'on': true}")
    }
    
    func powerOff() {
        send("{'core': 7, 'cmd': 21, 'on': false}")
    }
    
    // MARK: Private Methods
    
    private func convertHEXtoRGB(hexColor:String) -> (red: UInt8, green: UInt8, blue: UInt8) {
        let red = UInt8(hexColor[..<hexColor.index(hexColor.startIndex, offsetBy: 2)], radix: 16)
        let green = UInt8(hexColor[hexColor.index(hexColor.startIndex, offsetBy: 2)..<hexColor.index(hexColor.startIndex, offsetBy: 4)], radix: 16)
        let blue = UInt8(hexColor[hexColor.index(hexColor.startIndex, offsetBy: 4)..<hexColor.endIndex], radix: 16)
        return (red!, green!, blue!)
    }
}
