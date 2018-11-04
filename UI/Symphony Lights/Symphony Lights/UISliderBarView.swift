//
//  UISliderBarView.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 02/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit
import os.log

class UISliderBarView: UIView {

    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
    override func draw(_ rect: CGRect) {
        
        os_log("HAAA")
        
        let path = UIBezierPath(ovalIn: rect)
        UIColor.green.setFill()
        path.fill()
        
//        // Size of rounded rectangle
//        let rectWidth = rect.width
//        let rectHeight = rect.height
//
//        // Find center of actual frame to set rectangle in middle
//        let xf:CGFloat = (self.frame.width  - rectWidth)  / 2
//        let yf:CGFloat = (self.frame.height - rectHeight) / 2
//
//        let ctx: CGContext = UIGraphicsGetCurrentContext()!
//        ctx.saveGState()
//
//        let rect = CGRect(x: xf, y: yf, width: rectWidth, height: rectHeight)
//        let clipPath: CGPath = UIBezierPath(roundedRect: rect, cornerRadius: 30).cgPath
//
//        ctx.addPath(clipPath)
//        ctx.setFillColor(UIColor.lightGray.cgColor)
//
//        ctx.closePath()
//        ctx.fillPath()
//        ctx.restoreGState()
    }

}
