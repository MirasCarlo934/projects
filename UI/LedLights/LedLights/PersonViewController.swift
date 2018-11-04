//
//  PersonViewController.swift
//  LedLights
//
//  Created by Carlo Miras on 22/10/2018.
//  Copyright © 2018 Mirascarlo. All rights reserved.
//

import UIKit
import os.log

class PersonViewController: UIViewController, UITextFieldDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate{
    
    //MARK: Properties
//    @IBOutlet weak var label1: UILabel!
    @IBOutlet weak var nameTextBox: UITextField!
    @IBOutlet weak var imgView: UIImageView!
    @IBOutlet weak var ratingControl: RatingControl!
    @IBOutlet weak var saveButton: UIBarButtonItem!
    
    /*
     This value is either passed by `MealTableViewController` in `prepare(for:sender:)`
     or constructed as part of adding a new meal.
     */
    var person: Person?
    
//    var defaultLabel1Text: String = "Label"
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view, typically from a nib.
//        defaultLabel1Text = label1.text!
        nameTextBox.delegate = self;
        
        // Set up views if editing an existing Meal.
        if let person = person {
            navigationItem.title = person.getName()
            nameTextBox.text = person.getName()
            imgView.image = person.getPhoto()
            ratingControl.rating = person.getRating()
        }
        
        updateSaveButtonState()
    }
    
    //MARK: UITextFieldDelegate
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }
    
    func textFieldDidBeginEditing(_ textField: UITextField) {
        saveButton.isEnabled = false
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        updateSaveButtonState()
        navigationItem.title = textField.text
    }
    
    //MARK: UIImagePickerControllerDelegate
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        guard let selectedImg = info[UIImagePickerController.InfoKey.originalImage] as? UIImage
            else {
                fatalError("Expected a dictionary containing an image, but was provided the following: \(info)")
        }
        
        imgView.image = selectedImg
        dismiss(animated: true, completion: nil)
    }
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        dismiss(animated: true, completion: nil)
    }
    
    //MARK: Navigation
    
    @IBAction func cancel(_ sender: UIBarButtonItem) {
        let isPresentingInAddMealMode = presentingViewController is UINavigationController
        if isPresentingInAddMealMode {
            dismiss(animated: true, completion: nil)
        } else if let owningNavigationController = navigationController{
            owningNavigationController.popViewController(animated: true)
        } else {
            fatalError("The MealViewController is not inside a navigation controller.")
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        guard let button = sender as? UIBarButtonItem, button === saveButton else {
            os_log("The save button was not pressed, cancelling", log: OSLog.default, type: .debug)
            return
        }
        
        let name = nameTextBox.text ?? ""
        let photo = imgView.image
        let rating = ratingControl.rating
        
        person = Person(name: name, photo: photo, rating: rating)
    }

    //MARK: Actions
    @IBAction func setImageFromPhotoLibrary(_ sender: UITapGestureRecognizer) {
        let imagePickerController = UIImagePickerController()

        nameTextBox.resignFirstResponder()
        imagePickerController.sourceType = .photoLibrary

        imagePickerController.delegate = self

        present(imagePickerController, animated: true, completion: nil)
    }
    
    //MARK: Private Methods
    private func updateSaveButtonState() {
        // Disable the Save button if the text field is empty.
        let text = nameTextBox.text ?? ""
        saveButton.isEnabled = !text.isEmpty
    }
    
    //MARK: Functions
//    private func setLabelText(text: String) {
//        if text != "" {
//            label1.text = text
//        } else {
//            label1.text = defaultLabel1Text
//        }
//    }
}

