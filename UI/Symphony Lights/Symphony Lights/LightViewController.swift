//
//  ViewController.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 02/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit
import os.log

class LightViewController: UIViewController, UISliderBarViewDelegate, UITableViewDataSource {
    
    //MARK: Properties
    var light: Light?
    private var addPatternCell: UITableViewCell?

    @IBOutlet weak var brightnessSlider: UISliderBarView!
    @IBOutlet weak var patternsTable: UITableView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        addPatternCell = patternsTable.dequeueReusableCell(withIdentifier: "AddPatternCell")!
        
        brightnessSlider.delegate = self
        patternsTable.dataSource = self
        
        guard let light = self.light
            else {
                fatalError("Light object not set for the LightView!")
        }
        navigationItem.title = light.getName()
        brightnessSlider.value = light.getBrightness()
        
    }
    
    // MARK: - Navigation
    
    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        
        guard let id = segue.identifier
            else {
                fatalError("No segue identifier!")
        }
        os_log("%@", id)
        
        switch(segue.identifier ?? "") {
        case "EditPattern":
            guard let sender = sender as? PatternsTableViewCell
                else {
                    fatalError("Unexpected sender for \(String(describing: segue.identifier)) segue!");
            }
            guard let patternView = segue.destination as? PatternTableViewController
                else {
                    fatalError("Invalid destination for PatternsTableViewCell!")
            }
            guard let pattern = sender.pattern
                else {
                    fatalError("Pattern not set for PatternsTableViewCell!")
            }
            patternView.pattern = pattern
        default:
            fatalError("Unexpected segue identifier '\(String(describing: segue.identifier))'!")
        }
        
    }
    
    //MARK: UITableViewDataSource
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        guard let light = self.light
            else {
                fatalError("Light object not set for the LightView!")
        }
        return light.getPatterns().count + 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let light = self.light
            else {
                fatalError("Light object not set for the LightView!")
        }
        
        if indexPath.row == light.getPatterns().count {
            return addPatternCell!
        }
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: "PatternsTableViewCell", for: indexPath) as? PatternsTableViewCell
            else {
                fatalError("The dequeued cell is not an instance of PatternsTableViewCell!")
        }
        
        let pattern = light.getPatterns()[indexPath.row]
        cell.pattern = pattern
        cell.patternLabel.text = "\(pattern.getColorPalette()) \(pattern.getPatternSequence())"
        cell.durationLabel.text = "\(pattern.getDuration()) seconds"
        
        return cell
    }
    
    //MARK: UISliderBarViewDelegate
    
    func valueChanged(_ sender: UISliderBarView) {
        guard let light = self.light
            else {
                fatalError("Light object not set for the LightView!")
        }
        light.setBrightness(brightness: Int(sender.value))
    }
}

