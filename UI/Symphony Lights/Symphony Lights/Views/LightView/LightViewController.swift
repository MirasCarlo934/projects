//
//  ViewController.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 02/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit

class LightViewController: UIViewController, UISliderBarViewDelegate, UITableViewDataSource, UITableViewDelegate {
    
    //MARK: Properties
    var light: Light!
    private var addPatternCell: UITableViewCell?
    
    @IBOutlet weak var rightStack: UIStackView!
    @IBOutlet weak var brightnessSlider: UISliderBarView!
    @IBOutlet weak var patternsTable: UITableView!
    @IBOutlet weak var powerButton: UIButton!
    @IBOutlet weak var editPatternsButton: UIBarButtonItem!
    
    private var rightStack_hidden = false
    private var rightStack_x: CGFloat!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        addPatternCell = patternsTable.dequeueReusableCell(withIdentifier: "AddPatternCell")!
        
        brightnessSlider.delegate = self
        patternsTable.delegate = self
        patternsTable.dataSource = self
        
        rightStack_x = rightStack.frame.origin.x
        
        guard let light = self.light
            else {
                fatalError("Light object not set for the LightView!")
        }

        if light.getStatus() == .Off {
            powerButton.setImage(UIImage(named: "power-orange.png"), for: .normal)
        } else if light.getStatus() == .Disconnected {
            powerButton.setImage(UIImage(named: "power-gray.png"), for: .normal)
            powerButton.isEnabled = false
        } else {
            powerButton.setImage(UIImage(named: "power-green.png"), for: .normal)
        }

        navigationItem.title = light.getName()
        brightnessSlider.value = light.getBrightness()
    }
    
    // MARK: - Navigation
    
    @objc func goBack() {
        performSegue(withIdentifier: "UnwindToHomeView", sender: nil)
    }
    
    // Unwinds from a view opened from this view
    @IBAction func unwindToLightView(sender: UIStoryboardSegue) {
        LOG.debug("Unwinding to Light View...")
        if let patternViewController = sender.source as? PatternViewController {
            if let selectedPatternIndexPath = patternsTable.indexPathForSelectedRow {
                
                if sender.identifier == "SavePatternEdits" {
                    guard let pattern = patternViewController.pattern
                        else {
                            fatalError("Pattern to be added is nil!")
                    }
                    if selectedPatternIndexPath == patternsTable.indexPath(for: addPatternCell!) {
                        // add new pattern
                        LOG.info("Adding new pattern \(pattern.toString()) to \(light.getName())")
                        let newIndexPath = IndexPath(row: light.getPatterns().count, section: 0)
                        light.addPattern(pattern)
                        patternsTable.insertRows(at: [newIndexPath], with: .automatic)
                    } else {
                        // edit existing pattern
                        LOG.info("Finished editing pattern \(pattern.toString()) of \(light.getName())")
                        light.resendPatternList()
                        patternsTable.reloadRows(at: [selectedPatternIndexPath], with: .none)
                    }
                } else if sender.identifier == "RemovePattern" {
                    guard let pattern = patternViewController.pattern
                        else {
                            fatalError("Pattern to be added is nil!")
                    }
                    light.removePattern(pattern: pattern)
                    patternsTable.reloadData()
                }
                
            } else {
                fatalError("Unwind error!")
            }
        } else if let colorViewController = sender.source as? ColorWheelViewController {
            LOG.info("Setting static color of \(light.getName()) to \(colorViewController.selectedColor.hexValue())")
            light.setStaticColor(colorViewController.selectedColor)
            light.getAPI().setStaticColor(hexColor: light.getStaticColor()!.hexValue())
        }
    }
    
    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        
        switch segue.identifier ?? "" {
        case "EditPattern":
            guard let sender = sender as? PatternsTableViewCell
                else {
                    fatalError("Unexpected sender for \(String(describing: segue.identifier)) segue!");
            }
            // for modular segue
            guard let patternNavController = segue.destination as? UINavigationController
                else {
                    fatalError("Invalid NavigationController!")
            }
            guard let patternView = patternNavController.viewControllers.first as? PatternViewController
            // for show segue
//            guard let patternView = segue.destination as? PatternViewController
                else {
                    fatalError("Invalid destination for PatternsTableViewCell!")
            }
            guard let pattern = sender.pattern
                else {
                    fatalError("Pattern not set for PatternsTableViewCell!")
            }
            patternView.pattern = pattern
            patternView.api = light.getAPI()
//            break
        case "AddPattern":
            guard let patternNavController = segue.destination as? UINavigationController
                else {
                    fatalError("Invalid NavigationController!")
            }
            guard let patternView = patternNavController.viewControllers.first as? PatternViewController
                else {
                    fatalError("Invalid destination for PatternsTableViewCell!")
            }
            patternView.api = light.getAPI()
            break
        case "SetStaticColor":
            guard let colorNavController = segue.destination as? UINavigationController
                else {
                    fatalError("Invalid NavigationController!")
            }
            guard let colorWheel = colorNavController.viewControllers.first as? ColorWheelViewController
                else {
                    fatalError("Invalid destination, should be ColorWheelViewController")
            }
            colorWheel.lightAPI = light.getAPI()
            colorWheel.defaultColor = light.getStaticColor()
            break
        default:
            fatalError("Unexpected segue identifier '\(String(describing: segue.identifier))'!")
        }
        
    }
    
//    override func performSegue(withIdentifier identifier: String, sender: Any?) {
//        super.performSegue(withIdentifier: identifier, sender: sender)
//        if let button = sender as? BlockButton {
//            button.superview!.superview!
//        }
//    }
    
    //MARK: UITableViewDataSource
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return light.getPatterns().count + 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if indexPath.row == light.getPatterns().count {
            return addPatternCell!
        }
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: "PatternsTableViewCell", for: indexPath) as? PatternsTableViewCell
            else {
                fatalError("The dequeued cell is not an instance of PatternsTableViewCell!")
        }
        
        let pattern = light.getPatterns()[indexPath.row]
        cell.pattern = pattern
        if (pattern.getColorPalette() != nil) { // true if pattern uses a colorpalette
            cell.patternLabel.text = pattern.toString()
            cell.colorPreview.isHidden = true
        } else { // if pattern uses a solid color
            cell.patternLabel.text = "      " + pattern.getPatternSequence()!
            cell.colorPreview.isHidden = false
            cell.colorPreview.setColor(color: pattern.getColor()!)
        }
        cell.durationLabel.text = pattern.getTextualDuration()
        
        return cell
    }
    
//    func tableView(_ tableView: UITableView, editingStyleForRowAt indexPath: IndexPath) -> UITableViewCell.EditingStyle {
//        return .none
//    }
//    
//    func tableView(_ tableView: UITableView, shouldIndentWhileEditingRowAt indexPath: IndexPath) -> Bool {
//        return false
//    }
    
    func tableView(_ tableView: UITableView, moveRowAt sourceIndexPath: IndexPath, to destinationIndexPath: IndexPath) {
        light.reorderPatternInList(sourceIndex: sourceIndexPath.row, destinationIndex: destinationIndexPath.row)
    }
    
    func tableView(_ tableView: UITableView, targetIndexPathForMoveFromRowAt sourceIndexPath: IndexPath, toProposedIndexPath proposedDestinationIndexPath: IndexPath) -> IndexPath {
        if tableView.cellForRow(at: proposedDestinationIndexPath) === addPatternCell {
            return sourceIndexPath
        } else {
            return proposedDestinationIndexPath
        }
    }
    
    func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCell.EditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == UITableViewCell.EditingStyle.delete {
            light.removePattern(at: indexPath.row)
            tableView.deleteRows(at: [indexPath], with: UITableView.RowAnimation.automatic)
        }
    }
    
    // Override to support editing the table view.
//    override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCell.EditingStyle, forRowAt indexPath: IndexPath) {
//        if editingStyle == .delete {
//            // Delete the row from the data source
//            let pattern = light.getPatterns()[indexPath]
//            light.removePattern(pattern)
//            tableView.deleteRows(at: [indexPath], with: .fade)
//        } else if editingStyle == .insert {
//            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
//        }
//    }
    
    // MARK: UITableViewDelegate
    
//    func scrollViewDidScroll(_ scrollView: UIScrollView) {
//        let stackY = rightStack.frame.origin.y
//        let stackHeight = rightStack.frame.size.height
//        let stackWidth = rightStack.frame.size.width
//        let tableX = patternsTable.frame.origin.x
//        let tableY = patternsTable.frame.origin.y
//        let tableHeight = patternsTable.frame.size.height
//        let tableWidth = patternsTable.frame.size.width
//        if !rightStack_hidden && scrollView.contentOffset.y > 50 {
//            rightStack_hidden = true
//            UIView.animate(withDuration: 0.3, animations: {
//                self.rightStack.frame = CGRect(x: self.rightStack_x + stackWidth + 16, y: stackY, width: stackWidth, height: stackHeight)
//                self.patternsTable.frame = CGRect(x: tableX, y: tableY, width: tableWidth + stackWidth, height: tableHeight)
//            }, completion: { (Bool) -> Void in
//                self.patternsTable.setNeedsDisplay()
//            })
//        } else if rightStack_hidden && scrollView.contentOffset.y < -50 {
//            rightStack_hidden = false
//            UIView.animate(withDuration: 0.3, animations: {
//                self.rightStack.frame = CGRect(x: self.rightStack_x, y: stackY, width: stackWidth, height: stackHeight)
//                self.patternsTable.frame = CGRect(x: tableX, y: tableY, width: tableWidth - stackWidth, height: tableHeight)
//            }, completion: { (Bool) -> Void in
//                self.patternsTable.setNeedsDisplay()
//            })
//        }
//    }
    
    //MARK: UISliderBarViewDelegate
    
    func valueChanged(_ sender: UISliderBarView) {
        guard let light = self.light
            else {
                fatalError("Light object not set for the LightView!")
        }
        light.setBrightness(brightness: Int(sender.value))
    }
    
    // MARK: Actions
    
    @IBAction func powerButtonPressed(_ sender: Any) {
        if light.getStatus() == .Running {
            powerButton.setImage(UIImage(named: "power-orange.png"), for: .normal)
            light.setStatus(.Off)
        } else if light.getStatus() == .Off {
            powerButton.setImage(UIImage(named: "power-green.png"), for: .normal)
            light.setStatus(.Running)
        }
    }
    
    @IBAction func editPatternsList(_ sender: Any) {
        if patternsTable.isEditing { // set back to normal
            editPatternsButton.title = "Edit"
            editPatternsButton.style = .plain
            self.patternsTable.setEditing(false, animated: true)
            toggleSidebarVisibility(visible: true, delay: 0.3, completion: {})
        } else { // set to editing mode
            editPatternsButton.title = "Done"
            editPatternsButton.style = .done
            toggleSidebarVisibility(visible: false, delay: 0, completion: {
                self.patternsTable.setEditing(true, animated: true)
            })
        }
    }
    
    // MARK: Private Functions
    
    func toggleSidebarVisibility(visible: Bool, delay: Double, completion: @escaping () -> Void) {
        let _: Timer = Timer.scheduledTimer(withTimeInterval: delay, repeats: false, block: { (timer) in
            let stackY = self.rightStack.frame.origin.y
            let stackHeight = self.rightStack.frame.size.height
            let stackWidth = self.rightStack.frame.size.width
            let tableX = self.patternsTable.frame.origin.x
            let tableY = self.patternsTable.frame.origin.y
            let tableHeight = self.patternsTable.frame.size.height
            let tableWidth = self.patternsTable.frame.size.width
            if !self.rightStack_hidden && !visible {
                self.rightStack_hidden = true
                UIView.animate(withDuration: 0.2, animations: {
                    self.rightStack.frame = CGRect(x: self.rightStack_x + stackWidth + 16, y: stackY, width: stackWidth, height: stackHeight)
                    self.patternsTable.frame = CGRect(x: tableX, y: tableY, width: tableWidth + stackWidth + 16, height: tableHeight)
                }, completion: { (Bool) -> Void in
                    completion()
                })
            } else if self.rightStack_hidden && visible {
                self.rightStack_hidden = false
                UIView.animate(withDuration: 0.2, animations: {
                    self.rightStack.frame = CGRect(x: self.rightStack_x, y: stackY, width: stackWidth, height: stackHeight)
                    self.patternsTable.frame = CGRect(x: tableX, y: tableY, width: tableWidth - stackWidth - 16, height: tableHeight)
                }, completion: { (Bool) -> Void in
                    completion()
                })
            }
        })
    }
    
}

