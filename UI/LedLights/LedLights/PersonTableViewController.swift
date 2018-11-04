//
//  PersonTableViewController.swift
//  LedLights
//
//  Created by Carlo Miras on 25/10/2018.
//  Copyright © 2018 Mirascarlo. All rights reserved.
//

import UIKit
import os.log

class PersonTableViewController: UITableViewController {
    
    //MARK: Properties
    var persons = [Person]()

    override func viewDidLoad() {
        super.viewDidLoad()

        //set edit button at left side of navigation bar
        navigationItem.leftBarButtonItem = editButtonItem
        
        if let savedPersons = loadPersons() { //load stored persons data
            persons += savedPersons
        } else {
            //load sample persons data
            loadSamplePersons()
        }
        
    }

    // MARK: - Table view data source

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return persons.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        // Table view cells are reused and should be dequeued using a cell identifier.
        let person = persons[indexPath.row]
        let cellIdentifier = "PersonTableViewCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? PersonTableViewCell
            else {
                fatalError("The dequeued cell is not an instance of PersonTableViewCell!")
        }
        
        cell.nameLabel.text = person.getName()
        cell.photoImageView.image = person.getPhoto()
        cell.ratingControl.rating = person.getRating()

        return cell
    }

    // Override to support conditional editing of the table view.
    override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the specified item to be editable.
        return true
    }

    // Override to support editing the table view.
    override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCell.EditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            // Delete the row from the data source
            persons.remove(at: indexPath.row)
            savePersons()
            tableView.deleteRows(at: [indexPath], with: .fade)
        } else if editingStyle == .insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }    
    }

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
        
        switch(segue.identifier ?? "") {
        case "AddPerson":
            os_log("Adding a new person.", log: OSLog.default, type: .debug)
        case "ShowPersonDetails":
            guard let personDetailViewController = segue.destination as? PersonViewController else {
                fatalError("Unexpected destination: \(segue.destination)")
            }
            
            guard let selectedMealCell = sender as? PersonTableViewCell else {
                fatalError("Unexpected sender: \(sender)")
            }
            
            guard let indexPath = tableView.indexPath(for: selectedMealCell) else {
                fatalError("The selected cell is not being displayed by the table")
            }
            
            let selectedPerson = persons[indexPath.row]
            personDetailViewController.person = selectedPerson
        default:
            fatalError("Unexpected Segue Identifier; \(segue.identifier)")
        }
    }
    
    //MARK: Actions
    
    @IBAction func unwindToPersonList(sender: UIStoryboardSegue) {
        if let sourceViewController = sender.source as? PersonViewController,
            let person = sourceViewController.person {
            
            if let selectedIndexPath = tableView.indexPathForSelectedRow {
                // Update an existing meal.
                persons[selectedIndexPath.row] = person
                tableView.reloadRows(at: [selectedIndexPath], with: .none)
            }
            else {
                // Add a new meal.
                let newIndexPath = IndexPath(row: persons.count, section: 0)
                
                persons.append(person)
                tableView.insertRows(at: [newIndexPath], with: .automatic)
            }
        }
        
        savePersons()
    }
    
    //MARK: Private Methods
    
    private func loadSamplePersons() {
        let celsoImg = UIImage(named: "celsoPhoto")
        let carloImg = UIImage(named: "carloPhoto")
        let cariImg = UIImage(named: "hiddenPhoto")
        
        guard let person1 = Person(name: "Celso Pangit", photo: celsoImg, rating: 1)
            else {
                fatalError("Failed to instantiate person1!")
        }
        guard let person2 = Person(name: "Carlo", photo: carloImg, rating: 4)
            else {
                fatalError("Failed to instantiate person2!")
        }
        guard let person3 = Person(name: "Cari Agatha", photo: cariImg, rating: 5)
            else {
                fatalError("Failed to instantiate person3!")
        }
        
        persons += [person1, person2, person3]
    }
    
    private func savePersons() {
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(persons, toFile: Person.ArchiveURL.path)
        if isSuccessfulSave {
            os_log("Meals successfully saved.", log: OSLog.default, type: .debug)
        } else {
            os_log("Failed to save meals...", log: OSLog.default, type: .error)
        }
    }
    
    private func loadPersons() -> [Person]? {
        return NSKeyedUnarchiver.unarchiveObject(withFile: Person.ArchiveURL.path) as? [Person]
    }

}
