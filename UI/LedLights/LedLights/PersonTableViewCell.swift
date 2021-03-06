//
//  PersonTableViewCell.swift
//  LedLights
//
//  Created by Carlo Miras on 25/10/2018.
//  Copyright © 2018 Mirascarlo. All rights reserved.
//

import UIKit

class PersonTableViewCell: UITableViewCell {
    
    //MARK: Properties
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var photoImageView: UIImageView!
    @IBOutlet weak var ratingControl: RatingControl!
    

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
