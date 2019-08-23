//
//  HomeTableViewCell.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 02/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit

class HomeTableViewCell: UITableViewCell {

    //MARK: Properties
    @IBOutlet weak var lightNameLabel: UILabel!
    @IBOutlet weak var lightStatusLabel: UILabel!
    @IBOutlet weak var powerButton: UIButton!
    
    var light: Light!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

    @IBAction func power(_ sender: Any) {
        if light.getStatus() == .Running {
            light.setStatus(.Off)
        } else if light.getStatus() == .Off {
            light.setStatus(.Running)
        }
        displayStatus()
    }
    
    public func displayStatus() {
        lightStatusLabel.text = light.getStatus().rawValue
        switch(light.getStatus()) {
        case .Running:
            lightStatusLabel.textColor = UIColor.green
            powerButton.setImage(UIImage(named: "power-green.png"), for: .normal)
            break
        case .Disconnected:
            lightStatusLabel.textColor = UIColor.red
            powerButton.setImage(UIImage(named: "power-gray.png"), for: .normal)
            break
        case .Off:
            lightStatusLabel.textColor = UIColor.gray
            powerButton.setImage(UIImage(named: "power-orange.png"), for: .normal)
            break
        }
    }
}
