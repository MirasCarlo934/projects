//
//  ViewController.swift
//  iCounter
//
//  Created by Carlo Miras on 08/08/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit

class ViewController: UIViewController {
    
    //MARK: Objects
    @IBOutlet weak var stepper1: UIStepper!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func stepperFunc(_ sender: UIStepper) {
        
    }
    
}

