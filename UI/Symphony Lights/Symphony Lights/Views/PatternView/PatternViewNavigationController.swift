//
//  PatternViewNavigationController.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 04/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit
import os.log

class PatternViewNavigationController: UINavigationController {
    
    //MARK: Properties
    var pattern: LightPattern?

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
    }
    

    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
        guard let patternViewController = segue.destination as? PatternViewController
            else {
                fatalError("Invalid destination!")
        }
        patternViewController.pattern = pattern
    }

}
