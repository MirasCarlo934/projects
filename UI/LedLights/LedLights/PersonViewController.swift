//
//  ViewController.swift
//  LedLights
//
//  Created by Carlo Miras on 22/10/2018.
//  Copyright © 2018 Mirascarlo. All rights reserved.
//

import UIKit

class ViewController: UIViewController, UITextFieldDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate{
    
    //MARK: Properties
    @IBOutlet weak var label1: UILabel!
    @IBOutlet weak var nameTextBox: UITextField!
    @IBOutlet weak var imgView: UIImageView!
    @IBOutlet weak var ratingControl: RatingControl!
    
    var defaultLabel1Text: String = "Label"
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view, typically from a nib.
        defaultLabel1Text = label1.text!
        nameTextBox.delegate = self;
    }
    
    //MARK: UITextFieldDelegate
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }
    
    func textFieldDidEndEditing(_ textField: UITextField) {
        setLabelText(text: textField.text!)
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

    //MARK: Actions
    @IBAction func setImageFromPhotoLibrary(_ sender: UITapGestureRecognizer) {
        let imagePickerController = UIImagePickerController()

        nameTextBox.resignFirstResponder()
        imagePickerController.sourceType = .photoLibrary

        imagePickerController.delegate = self

        present(imagePickerController, animated: true, completion: nil)
    }
    
    //MARK: Functions
    func setLabelText(text: String) {
        if text != "" {
            label1.text = text
        } else {
            label1.text = defaultLabel1Text
        }
    }
}

