//
//  PatternsTableViewCell.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 03/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit

class PatternsTableViewCell: UITableViewCell {

    //MARK: Properties
    var pattern: LightPattern?
    
    @IBOutlet weak var patternLabel: UILabel!
    @IBOutlet weak var durationLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
