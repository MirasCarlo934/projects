//
//  LightPattern.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 27/01/2019.
//  Copyright © 2019 Symphony. All rights reserved.
//

import UIKit
import os.log

class LightPattern {
    
    // MARK: Static class elements
    /*
     positive: general palettes
     negative: pattern-specific palettes
     */
    public static var colorPalettes: [Int: String] = [
        0: "Christmas",
        1: "Summer",
        2: "Winter",
        3: "Spring",
        4: "Fall",
        -1: "Normal",
        -2: "Blue",
        -3: "Green"
    ]
    
    public static var patternDictionary: [Int: String] = [
        0: "Flow",
        1: "Solid",
        2: "Fade",
        3: "Twinkle",
        4: "Fire",
        5: "Sparkle",
        6: "Burst",
        7: "Burst Reverse"
    ]
    
    /*
     -1 -> all colors/palettes allowed
     -2 -> no palettes/colors allowed
     -3 -> palettes only
     -4 -> colors only
     
     any array of positive numbers means that those are the available palettes for the pattern, colors included
     any array of negative numbers means that those are the available palettes for the pattern, no colors included
     */
    public static var patternPalettes: [Int: [Int]] = [
        0: [-1],
        1: [-4],
        2: [-4],
        3: [-4],
        4: [-101, -102, -103],
        5: [-2],
        6: [-1],
        7: [-1]
    ]
    
    /**
     Returns the  pattern sequence ID for the pattern sequence specified.
     
     - Parameter patternName: The pattern sequence name to specify
     - Returns: The color palette ID, nil if not found
     */
    public static func getPatternID(patternName: String) -> Int? {
        for k in patternDictionary.keys {
            if patternDictionary[k] == patternName {
                return k
            }
        }
        return nil
    }
    
    /**
     Returns the palettes available for the pattern sequence specified
     
     - Parameter patternSequenceID: The ID of the pattern sequence to specify
     - Returns:
     - The array of IDs of the color palettes available
     - **nil** if *patternSequenceID* specified is invalid
     
     -1 -> all colors & palettes allowed
     -2 -> no palettes & colors allowed
     -3 -> palettes only
     -4 -> colors only
     */
    public static func getPalettesForPatternSequence(patternSequenceID: Int) -> (Int, [Int])? {
        let palettes = patternPalettes[patternSequenceID]
        
        if(palettes != nil) {
            var arr = [Int]()
            
            switch(palettes![0]) {
            case -1, -3 :
                var i = 0
                var palette = colorPalettes[i]
                while palette != nil {
                    arr.append(i)
                    i += 1
                    palette = colorPalettes[i]
                }
                break
            case -2, -4:
                arr = []
                break
            default:
                if palettes![0] < -100 { // only select palettes are available + no colors
                    for palette in palettes! {
                        arr.append( palette + 100 )
                    }
                    return (-3, arr)
                } else { // select palettes + all colors
                    return (-1, palettes!)
                }
            }
            
            return (palettes![0], arr)
        } else {
            return nil
        }
    }
    
    public static func getColorPaletteID(colorPaletteName: String) -> Int? {
        for k in colorPalettes.keys {
            if colorPalettes[k] == colorPaletteName {
                return k
            }
        }
        return nil
    }
    
    // MARK: Class properties
    private var colorPaletteID: Int?
    private var patternSequenceID: Int?
//    private var colorPalette: String?
    private var color: UIColor?
//    private var patternSequence: String?
    private var duration: Int? // measured in seconds
    
    // MARK: Class constructors
    
    // for setting color palette with pattern sequence ID
    init?(colorPalette: String, patternSequenceID: Int, duration: Int) {
        if duration <= 0 {
            return nil
        }
        do {
            try self.setPatternSequence(patternSequenceID: patternSequenceID)
            try self.setColorPalette(colorPaletteName: colorPalette)
            self.duration = duration
        } catch let e as InvalidArgumentError {
            os_log("%@", e.errorMessage)
            return nil
        } catch {
            os_log("LightPattern init error!")
            return nil
        }
    }
    
    /**
     
     */
    // for setting color palette with string
    init?(colorPalette: String, patternSequenceName: String, duration: Int) {
        if duration <= 0 {
            return nil
        }
        do {
            try self.setPatternSequence(patternSequenceName: patternSequenceName)
            try self.setColorPalette(colorPaletteName: colorPalette)
            self.duration = duration
        } catch let e as InvalidArgumentError {
            os_log("%@", e.errorMessage)
            return nil
        } catch {
            os_log("LightPattern init error!")
            return nil
        }
    }
    
    init(lightPattern: LightPattern) {
        copy(lightPattern: lightPattern)
    }
    
    init() {
        do {
            try self.setPatternSequence(patternSequenceID: 0)
            try self.setColorPalette(colorPaletteID: 0)
        } catch let e as InvalidArgumentError {
            os_log("%@", e.errorMessage)
        } catch {
            os_log("LightPattern init error!")
        }
    }
    
    // MARK: Public Methods
    
    public func copy(lightPattern: LightPattern) {
        self.patternSequenceID = lightPattern.getPatternSequenceID()
        self.color = lightPattern.getColor()
        self.colorPaletteID = lightPattern.getColorPaletteID()
        self.duration = lightPattern.getDuration()
    }
    
    public func clearColors() {
        self.color = nil
        self.colorPaletteID = nil
    }
    
    public func toString() -> String {
        if color != nil {
            return "#\(color!.hexValue()) \(getPatternSequence()!)"
        } else if colorPaletteID != nil {
            return "\(getColorPalette()!) \(getPatternSequence()!)"
        } else {
            return "\(getPatternSequence()!)"
        }
    }
    
    public func isPatternWithNoColorConfig() -> Bool {
        guard let id = patternSequenceID
            else {
                return false
        }
        if LightPattern.patternPalettes[id]![0] == -2 {
            return true
        } else {
            return false
        }
    }
    
    //MARK: Encapsulation
    
    public func setColor(color: UIColor) {
        self.color = color
        self.colorPaletteID = nil
    }
    
    public func getColor() -> UIColor? {
        return color
    }
    
    public func setColorPalette(colorPaletteID: Int) throws {
        guard let _ = LightPattern.colorPalettes[colorPaletteID]
            else {
                throw InvalidArgumentError(kind: .unrecognizedArgument, errorMessage: "Specified color palette ID does not exist!")
        }
        self.colorPaletteID = colorPaletteID
//        self.colorPalette = colorPalette
        self.color = nil
    }
    
    public func setColorPalette(colorPaletteName: String) throws {
        guard let id = LightPattern.getColorPaletteID(colorPaletteName: colorPaletteName)
            else {
                throw InvalidArgumentError(kind: .unrecognizedArgument, errorMessage: "Specified color palette name does not exist!")
        }
        try setColorPalette(colorPaletteID: id)
    }
    
    public func getColorPalette() -> String? {
        guard let id = colorPaletteID
            else {
                return nil
        }
        return LightPattern.colorPalettes[id]
    }
    
    public func getColorPaletteID() -> Int? {
        return colorPaletteID
    }
    
    public func setPatternSequence(patternSequenceID: Int) throws {
        guard let _ = LightPattern.patternDictionary[patternSequenceID]
            else {
                throw InvalidArgumentError(kind: .unrecognizedArgument, errorMessage: "Specified pattern sequence ID does not exist!")
        }
        self.patternSequenceID = patternSequenceID
        
        let patternPaletteIndicator = LightPattern.patternPalettes[patternSequenceID]![0]
        if patternPaletteIndicator == -2 {
            clearColors()
        } else if patternPaletteIndicator == -3 {
            self.color = nil
        } else if patternPaletteIndicator == -4 {
            self.colorPaletteID = nil
        }
    }
    
    public func setPatternSequence(patternSequenceName: String) throws {
        guard let id = LightPattern.getPatternID(patternName: patternSequenceName)
            else {
                throw InvalidArgumentError(kind: .unrecognizedArgument, errorMessage: "Specified pattern sequence does not exist!")
        }
        try setPatternSequence(patternSequenceID: id)
    }
    
    public func getPatternSequence() -> String? {
        guard let id = patternSequenceID
            else {
                return nil
        }
        return LightPattern.patternDictionary[id]
    }
    
    public func getPatternSequenceID() -> Int? {
        return patternSequenceID
    }
    
    public func setDuration(duration: Int) {
        self.duration = duration
    }
    
    public func getDuration() -> Int? {
        return duration
    }
    
    //    public func getPatternSequence() -> String {
    //        if patternSequence != nil {
    //            return patternSequence!
    //        } else {
    //            fatalError("Pattern sequence not set!")
    //        }
    //    }
    //
    //    public func getPatternSequenceID() -> Int {
    //        if patternSequenceID != nil {
    //            return patternSequenceID!
    //        } else {
    //            fatalError("Pattern sequence ID not set!")
    //        }
    //    }
    //
    //    public func setDuration(duration: Int) {
    //        self.duration = duration
    //    }
    //
    //    public func getDuration() -> Int {
    //        if duration != nil {
    //            return duration!
    //        } else {
    //            fatalError("Duration not set!")
    //        }
    //    }
    
    public func getTextualDuration() -> String? {
        var str: String?
        guard let duration = duration
            else {
                return nil
        }
        if duration >= 120 {
            str = "\(duration/60) "
            if duration % 60 > 0 {
                str! += "mins, \(duration % 60) secs"
            } else {
                str! += "minutes"
            }
        } else if duration > 60 {
            str = "\(duration/60) min, \(duration % 60) secs"
        } else if duration == 60 {
            str = "1 minute"
        } else {
            if duration > 1 {
                str = "\(duration) seconds"
            } else {
                str = "\(duration) second"
            }
        }
        
        return str;
    }
}
