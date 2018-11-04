//
//  Light.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 02/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import Foundation

class Light/*: NSObject, NSCoding*/ {
    
    private var name: String
    private var color: RGBColor
    private var brightness: Int
    private var patterns = [LightPattern]()
    private var status: LightStatus
    
    /**
     Initialize a Light object with initial patterns array
     
     - Author: Carlo Miras
     */
    init?(name: String, color: RGBColor, brightness: Int, patterns: [LightPattern], status: LightStatus) {
        if name.isEmpty || brightness < 0 || brightness > 100  {
            return nil
        }
        self.name = name
        self.color = color
        self.brightness = brightness
        self.patterns = patterns
        self.status = status
    }
    
    /**
     Initialize a Light object with its patterns array set to empty
     
     - Author: Carlo Miras
     */
    init?(name: String, color: RGBColor, brightness: Int, status: LightStatus) {
        if name.isEmpty || brightness < 0 || brightness > 100  {
            return nil
        }
        self.name = name
        self.color = color
        self.brightness = brightness
        self.status = status
    }
    
    //MARK: Encapsulation
    
    public func getName() -> String {
        return name
    }
    
    public func getColor() -> RGBColor {
        return color
    }
    
    public func setBrightness(brightness: Int) {
        if brightness < 0 || brightness > 100 {
            fatalError("Attempted to set invalid brightness '\(brightness)' (must be 0-100)")
        }
        self.brightness = brightness
    }
    
    public func getBrightness() -> Int {
        return brightness
    }
    
    public func getPatterns() -> [LightPattern] {
        return patterns
    }
    
    public func getStatus() -> LightStatus {
        return status
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

class LightPattern {
    private var colorPalette: String
    private var patternSequence: String
    private var duration: Int
    
    init?(colorPalette: String, pattern: String, duration: Int) {
        self.colorPalette = colorPalette
        self.patternSequence = pattern
        self.duration = duration
    }
    
    //MARK: Encapsulation
    
    public func setColorPalette(colorPalette: String) {
        self.colorPalette = colorPalette
    }
    
    public func getColorPalette() -> String {
        return colorPalette
    }
    
    public func setPattern(pattern: String) {
        self.patternSequence = pattern
    }
    
    public func getPatternSequence() -> String {
        return patternSequence
    }
    
    public func setDuration(duration: Int) {
        self.duration = duration
    }
    
    public func getDuration() -> Int {
        return duration
    }
}

enum LightStatus: String {
    case Running = "Running", Idle = "Idle", Off = "Off", Disconnected = "Disconnected"
}
