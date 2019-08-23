//
//  ColorWheelViewController.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 17/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit
import os.log

import FlexColorPicker

class ColorWheelViewController: CustomColorPickerViewController, ColorPickerDelegate {
    
    var pattern: LightPattern?
    var lightAPI: LEDLightAPIManager?
    var defaultColor: UIColor?
    
    var debouncer = Debouncer(timeInterval: 0.020)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        delegate = self
        
        if defaultColor != nil {
            self.selectedColor = defaultColor!
        }
        if lightAPI == nil {
            fatalError("LEDLightAPIManager not set!")
        }
        
        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false
        
        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem
    }
    
    //TODO: improve this
    @IBAction func cancel(_ sender: Any) {
//        let isPresentingInSetStaticColor = presentingViewController is ColorWheelNavigationController
        if defaultColor != nil {
            lightAPI!.setStaticColor(hexColor: defaultColor!.hexValue())
        } else {
            lightAPI!.resumeNormalOperation()
        }

        if (navigationController as? ColorWheelNavigationController) != nil {
            LOG.debug("Cancelling Set Static Color")
            dismiss(animated: true, completion: nil)
        } else if let owningNavigationController = navigationController {
            LOG.debug("Cancelling Choose Pattern Color")
            owningNavigationController.popViewController(animated: true)
        } else {
//            os_log("Cancelling Set Static Color")
            dismiss(animated: true, completion: nil)
        }
    }
    
//    @IBAction func save(_ sender: Any) {
//        if (sender as? UIBarButtonItem) === saveButton {
//            if let pattern = pattern {
//                guard let owningNavigationController = navigationController
//                    else {
//                        fatalError("Not in Choose Pattern Color!")
//                }
//                pattern.setColor(color: selectedColor.hexValue())
//                os_log("Finished choosing pattern color")
//                owningNavigationController.popViewController(animated: true)
//            } else if let light = light {
//                if (navigationController as? ColorWheelNavigationController) != nil {
//                    light.setStaticColor(color: selectedColor)
//                    os_log("Finished setting static color")
//                    dismiss(animated: true, completion: nil)
//                } else {
//                    fatalError("Invalid navigation controller for SetStaticColor!")
//                }
//            } else {
//                fatalError("No pattern or light set!")
//            }
//        } else {
//            fatalError("Invalid sender for save!")
//        }
//    }
    
    func colorPicker(_ colorPicker: ColorPickerController, selectedColor: UIColor, usingControl: ColorControl) {
        LOG.verbose("Selected color: \(selectedColor.hexValue())")
        if lightAPI != nil {
            if pattern == nil {
                debouncer.function = { self.lightAPI!.setStaticColor(hexColor: selectedColor.hexValue()) }
            } else {
//                pattern!.
            }
        }
    }

    func colorPicker(_ colorPicker: ColorPickerController, confirmedColor: UIColor, usingControl: ColorControl) {
//        os_log("%@", type: .debug, "Confirmed Color: \(confirmedColor)")
    }
}
