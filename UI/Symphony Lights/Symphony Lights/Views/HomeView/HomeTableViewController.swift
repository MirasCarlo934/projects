//
//  HomeTableViewController.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 02/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit
import os.log

class HomeTableViewController: UITableViewController {
    
    //MARK: Properties
    private var lights = [Light]()

    override func viewDidLoad() {
        super.viewDidLoad()

        //loads sample lights
        loadSampleLights()
        
        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false

        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem
    }

    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return lights.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let cell = tableView.dequeueReusableCell(withIdentifier: "HomeTableViewCell", for: indexPath) as? HomeTableViewCell
            else {
                fatalError("The dequeued cell is not an instance of HomeTableViewCell!")
        }
        let light = lights[indexPath.row]
        
        cell.light = light
        cell.lightNameLabel.text = light.getName()
        cell.displayStatus()
        
        light.setHomeViewCell(cell)

        return cell
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

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        
        guard let lightViewController = segue.destination as? LightViewController
            else {
                fatalError("Segue destination is not LightViewController!")
        }
        
        guard let selectedHomeCell = sender as? HomeTableViewCell else {
            fatalError("Unexpected sender: \(sender!)")
        }
        
        guard let indexPath = tableView.indexPath(for: selectedHomeCell) else {
            fatalError("The selected cell is not being displayed by the table")
        }
        
        lightViewController.light = lights[indexPath.row]
    }
 
    
    //MARK: Private Functions
    
    private func loadSampleLights() {
        let api = LEDLightAPIManager(url: "ws://192.168.0.113:8080/ws", reconnecting: true)
        
        guard let light1 = Light(name: "Salas Lights", staticColor: nil, brightness: 50, patterns: [LightPattern(colorPalette: "Christmas", patternSequenceName: "Flow", duration: 60)!, LightPattern(colorPalette: "Spring", patternSequenceName: "Burst Reverse", duration: 10)!], status: LightStatus.Running, api: api)
            else {
               fatalError("Sample light 1 failed to initialize!")
        }
        guard let light2 = Light(name: "Gate Lights", brightness: 60, status: LightStatus.Running, api: api)
            else {
                fatalError("Sample light 2 failed to initialize!")
        }
        guard let light3 = Light(name: "Dining Room Lights", brightness: 100, status: LightStatus.Disconnected, api: api)
            else {
                fatalError("Sample light 3 failed to initialize!")
        }
        guard let light4 = Light(name: "StaticColor Set", staticColor: UIColor(red: 0, green: 1, blue: 1, alpha: 1), brightness: 50, patterns: [LightPattern(colorPalette: "Summer", patternSequenceName: "Burst", duration: 120)!], status: LightStatus.Off, api: api)
            else {
                fatalError("Sample light 4 failed to initialize!")
        }
        
        lights += [light1, light2, light3, light4]
    }

}
