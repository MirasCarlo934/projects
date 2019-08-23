//
//  Light.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 02/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit

class Light/*: NSObject, NSCoding*/ {
    
    private var name: String
    private var staticColor: UIColor?
    private var brightness: Int
    private var patterns = [LightPattern]()
    private var status: LightStatus
    
    private var api: LEDLightAPIManager
    
    // for callback purposes
    private var homeViewCell: HomeTableViewCell?
    
    /**
     Initialize a Light object with initial patterns array and static color. Most useful when initializing from file.
     
     - Author: Carlo Miras
     */
    init?(name: String, staticColor: UIColor?, brightness: Int, patterns: [LightPattern], status: LightStatus, api: LEDLightAPIManager) {
        if name.isEmpty || brightness < 0 || brightness > 100  {
            return nil
        }
        self.name = name
        self.staticColor = staticColor
        self.brightness = brightness
        self.patterns = patterns
        self.status = status
        
        self.api = api
    }
    
    /**
     Initialize a Light object with its patterns array set to empty and static color to nil. Most useful when initializing from scratch.
     
     - Author: Carlo Miras
     */
    init?(name: String, brightness: Int, status: LightStatus, api: LEDLightAPIManager) {
        if name.isEmpty || brightness < 0 || brightness > 100  {
            return nil
        }
        self.name = name
        self.brightness = brightness
        self.status = status
        
        self.api = api
    }
    
    //MARK: Encapsulation
    
    public func setHomeViewCell(_ cell: HomeTableViewCell) {
        homeViewCell = cell
    }
    
    public func getName() -> String {
        return name
    }
    
    public func setStaticColor(_ color: UIColor) {
//        os_log("Setting static color of %@ to %@", name, color.hexValue())
        self.staticColor = color
        api.setStaticColor(hexColor: color.hexValue())
    }
    
    public func getStaticColor() -> UIColor? {
        return staticColor
    }
    
    public func setBrightness(brightness: Int) {
//        os_log("Setting brightness of %@ to %@", name, brightness)
        if brightness < 0 || brightness > 100 {
            fatalError("Attempted to set invalid brightness '\(brightness)' (must be 0-100)")
        }
        self.brightness = brightness
        api.setBrightness(brightness)
    }
    
    public func getBrightness() -> Int {
        return brightness
    }
    
    public func addPattern(_ pattern: LightPattern) {
        patterns.append(pattern)
        api.setSequences(patterns)
    }
    
    public func removePattern(pattern: LightPattern) {
        for i in 0...patterns.count-1 {
            if patterns[i] === pattern {
                patterns.remove(at: i)
                break
            }
        }
        api.setSequences(patterns)
    }
    
    public func removePattern(at: Int) {
        patterns.remove(at: at)
        api.setSequences(patterns)
    }
 
    public func reorderPatternInList(sourceIndex: Int, destinationIndex:Int) {
        let pattern = patterns[sourceIndex]
        patterns.remove(at: sourceIndex)
        patterns.insert(pattern, at: destinationIndex)
        resendPatternList()
    }
    
    public func resendPatternList() {
        api.setSequences(patterns)
    }
    
    public func getPatterns() -> [LightPattern] {
        return patterns
    }
    
    public func setStatus(_ status: LightStatus) {
        self.status = status
        if status == .Running {
            api.powerOn()
        } else if status == .Off {
            api.powerOff()
        }
        
        if homeViewCell != nil {
            homeViewCell!.displayStatus()
        }
    }
    
    public func getStatus() -> LightStatus {
        return status
    }
    
    public func getAPI() -> LEDLightAPIManager {
        return api
    }
}

class RGBColor {
    private var red: Int
    private var green: Int
    private var blue: Int
    
    init?(red: Int, green: Int, blue: Int) {
        if red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255 {
            return nil
        }
        self.red = red
        self.green = green
        self.blue = blue
    }
    
    public func getRed() -> Int {
        return red
    }
    
    public func getGreen() -> Int {
        return green
    }
    
    public func getBlue() -> Int {
        return blue
    }
}

enum LightStatus: String {
    case Running = "Running", Off = "Off", Disconnected = "Disconnected"
}
