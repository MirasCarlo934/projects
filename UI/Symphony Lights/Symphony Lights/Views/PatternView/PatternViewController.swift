//
//  PatternTableViewController.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 04/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit

class PatternViewController: UITableViewController, UIPickerViewDataSource, UIPickerViewDelegate {
    
    
    //MARK: Properties
    var pattern: LightPattern?
    var api: LEDLightAPIManager!
    private var patternEdits: LightPattern!
    private var availablePalettes: [Int]!
    private var isShowingColorCell = true
    private var isShowingColorPalettePickerCell = true
    
//    private var patternSequences = ["None", "Solid", "Flow", "Drop", "Flicker", "Alternating"]

    @IBOutlet weak var titleBar: UINavigationItem!
    
    @IBOutlet weak var colorCell: UITableViewCell!
    @IBOutlet weak var colorPalettePicker: UIPickerView!
    @IBOutlet weak var colorPalettePickerCell: UITableViewCell!
    
    @IBOutlet weak var patternCell: UITableViewCell!
    @IBOutlet weak var patternPicker: UIPickerView!
    
    @IBOutlet weak var durationCell: UITableViewCell!
    @IBOutlet weak var durationPicker: UIPickerView!
    
    @IBOutlet weak var saveButton: UIBarButtonItem!
    @IBOutlet weak var removeButton: UIButton!
    
    let removeAlert = UIAlertController(title: "Are you sure you want to remove this pattern sequence?", message: "", preferredStyle: .alert)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        patternPicker.dataSource = self
        patternPicker.delegate = self
        durationPicker.dataSource = self
        durationPicker.delegate = self
        colorPalettePicker.dataSource = self
        colorPalettePicker.delegate = self
        
        removeAlert.addAction(UIAlertAction(title:"No", style: .cancel, handler: nil))
        removeAlert.addAction(UIAlertAction(title:"Yes", style: .destructive, handler: { action in
            self.performSegue(withIdentifier: "RemovePattern", sender: nil)
            }))
        
        if pattern != nil { // edit pattern
            LOG.debug("Editing pattern \(pattern!.toString())...")
            titleBar.title = "Edit Pattern"
            patternEdits = LightPattern(lightPattern: pattern!)
            updateAvailableColorPalettes()
            
            patternPicker.selectRow(patternEdits.getPatternSequenceID()!, inComponent: 0, animated: false)
            durationPicker.selectRow(patternEdits.getDuration()!/60, inComponent: 0, animated: false)
            durationPicker.selectRow(patternEdits.getDuration()!%60, inComponent: 1, animated: false)
            patternCell.detailTextLabel!.text = patternEdits.getPatternSequence()
            durationCell.detailTextLabel!.text = patternEdits.getTextualDuration()
            if patternEdits.getColorPaletteID() != nil {
                colorPalettePicker.selectRow(availablePalettes.firstIndex(of: patternEdits.getColorPaletteID()!)!, inComponent: 0, animated: false)
                colorCell.detailTextLabel!.text = patternEdits.getColorPalette()
            } else if patternEdits.getColor() != nil {
                colorCell.detailTextLabel!.text = patternEdits.getColor()!.hexValue()
            }
            
        } else { // add pattern
            LOG.debug("Adding pattern...")
            titleBar.title = "Add Pattern"
            patternEdits = LightPattern()
            updateAvailableColorPalettes()
            updatePatternCellDetailText()
            updateColorCellDetailText()
//            patternPicker.selectRow(0, inComponent: 0, animated: false)
//            colorPalettePicker.selectRow(0, inComponent: 0, animated: false)
        }
        
        updateSaveButtonState()
        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false

        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem
    }
    
    //MARK: UIPickerViewDataSource
    
    // Number of columns of data
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        if pickerView == patternPicker {
            return 1
        } else if pickerView == colorPalettePicker {
            return 1
        }
        else { // durationPicker
            return 2
        }
    }
    
    // Number of rows of data
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        if pickerView == patternPicker {
            return LightPattern.patternDictionary.count
        } else if pickerView == colorPalettePicker {
            return availablePalettes.count
        } else {
            return 60
        }
    }
    
    // The data to return for the row and component (column) that's being passed in
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        if pickerView == patternPicker {
            return LightPattern.patternDictionary[row]
        } else if pickerView == colorPalettePicker {
            return LightPattern.colorPalettes[availablePalettes![row]]
        } else { // durationPicker
            switch component {
            case 0:
                if row > 1 {
                    return "\(row) mins"
                } else {
                    return "\(row) min"
                }
            case 1:
                return "\(row) s"
            default:
                fatalError("Invalid number of component \(component)!")
            }
        }
    }
    
    // Capture the picker view selection
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        if pickerView == patternPicker {
            do {
                try patternEdits.setPatternSequence(patternSequenceID: row)
                updateAvailableColorPalettes()
                updatePatternCellDetailText()
                updateColorCellDetailText()
            } catch let e as InvalidArgumentError {
                fatalError(e.errorMessage)
            } catch {
                fatalError("Error in setting pattern sequence!")
            }
        } else if pickerView == colorPalettePicker {
//            colorCell.detailTextLabel!.text = LightPattern.colorPalettes[availablePalettes![row]]
            do {
                try patternEdits.setColorPalette(colorPaletteID: availablePalettes![row])
            } catch let e as InvalidArgumentError {
                fatalError(e.errorMessage)
            } catch {
                fatalError("Error in setting color palette!")
            }
            updateColorCellDetailText()
        } else { // durationPicker
            let duration: Int = pickerView.selectedRow(inComponent: 0)*60 + pickerView.selectedRow(inComponent: 1)
            patternEdits.setDuration(duration: duration)
            if(duration > 0) {
                durationCell.detailTextLabel!.text = patternEdits.getTextualDuration()
            } else {
                durationCell.detailTextLabel!.text = "can't be 0"
            }
        }
        
        let colorOrPalette: String
        if patternEdits.getColorPalette() != nil {
            colorOrPalette = patternEdits.getColorPalette()!
        } else {
            colorOrPalette = patternEdits.getColor()!.hexValue()
        }
        api.testSequence(ptrn: patternEdits.getPatternSequence()!, hexOrPalette: colorOrPalette)
        updateSaveButtonState()
    }

    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        if pattern != nil { // means editing pattern
            return 3
        } else { // means adding pattern
            return 2
        }
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch(section) {
        case 0:
            var count = 2
            if isShowingColorCell {count += 1}
            if isShowingColorPalettePickerCell {count += 1}
            return count
        case 1:
            return 2
        case 2:
            return 1
        default:
            fatalError("Invalid section index \(section)")
        }
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
//        let cell = tableView.dequeueReusableCell(withIdentifier: "reuseIdentifier", for: indexPath)
//        print(indexPath)
        if indexPath.section == 0 {
            switch indexPath.row {
            case 2:
                if isShowingColorCell {return colorCell}
                else {return colorPalettePickerCell}
            case 3:
                return colorPalettePickerCell
            default:
                return super.tableView(tableView, cellForRowAt: indexPath)
            }
        } else {
            return super.tableView(tableView, cellForRowAt: indexPath)
        }
    }
    
    @IBAction func removePattern(_ sender: Any) {
        self.present(removeAlert, animated: true)
    }

    /*
    // Override to support conditional editing of the table view.
    override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the specified item to be editable.
        return true
    }
    */

    /*
    // Override to support editing the table view.
    override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            // Delete the row from the data source
            tableView.deleteRows(at: [indexPath], with: .fade)
        } else if editingStyle == .insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }    
    }
    */

    /*
    // Override to support rearranging the table view.
    override func tableView(_ tableView: UITableView, moveRowAt fromIndexPath: IndexPath, to: IndexPath) {

    }
    */

    /*
    // Override to support conditional rearranging of the table view.
    override func tableView(_ tableView: UITableView, canMoveRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the item to be re-orderable.
        return true
    }
    */

    // MARK: - Navigation

    @IBAction func cancel(_ sender: UIBarButtonItem) {
        LOG.debug("Cancelling PatternViewController")
        api.resumeNormalOperation()
        dismiss(animated: true, completion: nil)
    }
    
    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        
        switch segue.identifier ?? "" {
        case "ChoosePatternColor": // going forward to ColorWheelView
            
            guard let sender = sender as? UITableViewCell, sender == colorCell!
                else {
                    fatalError("Invalid sender for \(segue.identifier!)")
            }
            guard let colorWheel = segue.destination as? ColorWheelViewController
                else{
                    fatalError("Invalid destination for \(segue.identifier!)")
            }
            
            LOG.debug("Choosing color for pattern")
            if patternEdits.getColor() != nil {
                colorWheel.defaultColor = patternEdits.getColor()
            }
            
            colorWheel.lightAPI = api
            
            break
            
        default: // going back to LightView
            
            guard let button = sender as? UIBarButtonItem, button === saveButton
                else {
                    LOG.debug("Assuming cancel button pressed, cancelling")
                    return
            }
            if pattern != nil {
                // for editing pattern only
                pattern!.copy(lightPattern: patternEdits)
            } else {
                // for adding pattern only
                pattern = LightPattern(lightPattern: patternEdits)
            }
        }
    }
    
    @IBAction func unwindToPatternEditor(sender: UIStoryboardSegue) {
        if let colorWheel = sender.source as? ColorWheelViewController {
            patternEdits.setColor(color: colorWheel.selectedColor)
            updateColorCellDetailText()
            updateSaveButtonState()
        } else {
            fatalError("Unrecognized controller source for unwindToPatternEditor()!")
        }
    }
 
    // MARK: Private Methods
    
    private func updateSaveButtonState() {
        if patternEdits.getPatternSequenceID() != nil && patternEdits.getDuration() != nil && patternEdits.getDuration()! > 0 {
            if patternEdits.isPatternWithNoColorConfig() {
                saveButton.isEnabled = true
            } else if patternEdits.getColorPaletteID() != nil || patternEdits.getColor() != nil {
                saveButton.isEnabled = true
            } else {
                saveButton.isEnabled = false
            }
        } else {
            saveButton.isEnabled = false
        }
        
    }
    
    private func updateAvailableColorPalettes() {
        guard let patternSequenceID = patternEdits.getPatternSequenceID()
            else {
                availablePalettes = Array(LightPattern.colorPalettes.keys)
                return
        }
        guard let palettes = LightPattern.getPalettesForPatternSequence(patternSequenceID: patternSequenceID)
            else {
                fatalError("Error in getting pattern sequences!")
        }
        
        availablePalettes = palettes.1
        
        switch palettes.0 {
        case -1: // all colors & palettes allowed
            // TODO: Allow colorCell to be pressed
            isShowingColorCell = true
            isShowingColorPalettePickerCell = true
            colorCell.accessoryType = .disclosureIndicator
            colorCell.isUserInteractionEnabled = true
            colorPalettePicker.reloadAllComponents()
            do {
                try patternEdits.setColorPalette(colorPaletteID: availablePalettes![0])
            } catch let e as InvalidArgumentError {
                fatalError(e.errorMessage)
            } catch {
                fatalError("Error in setting color palette!")
            }
            break
        case -2: // no palettes & colors allowed
            isShowingColorCell = false
            isShowingColorPalettePickerCell = false
            break
        case -3: // palettes only
            // TODO: Disallow colorCell to be pressed
            isShowingColorCell = true
            isShowingColorPalettePickerCell = true
            colorCell.accessoryType = .none
            colorCell.isUserInteractionEnabled = false
            colorPalettePicker.reloadAllComponents()
            do {
                try patternEdits.setColorPalette(colorPaletteID: availablePalettes![0])
            } catch let e as InvalidArgumentError {
                fatalError(e.errorMessage)
            } catch {
                fatalError("Error in setting color palette!")
            }
            break
        case -4: // colors only
            // TODO: Allow colorCell to be pressed
            isShowingColorCell = true
            isShowingColorPalettePickerCell = false
            colorCell.accessoryType = .disclosureIndicator
            colorCell.isUserInteractionEnabled = true
            patternEdits.setColor(color: UIColor.white)
            break
        default:
            fatalError("Invalid configuration of color/palette options!")
        }
        
        tableView.reloadData()
        updateColorCellDetailText()
    }
    
    private func updatePatternCellDetailText() {
        patternCell.detailTextLabel!.text = patternEdits.getPatternSequence()
    }
    
    private func updateColorCellDetailText() {
        if patternEdits.getColorPalette() != nil {
            colorCell.detailTextLabel!.text = patternEdits.getColorPalette()!
        } else if patternEdits.getColor() != nil {
            colorCell.detailTextLabel!.text = "#" + patternEdits.getColor()!.hexValue()
        } else {
            colorCell.detailTextLabel!.text = ""
        }
    }
}
