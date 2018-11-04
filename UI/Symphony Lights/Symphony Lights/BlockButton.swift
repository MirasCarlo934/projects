//
//  BlockButton.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 02/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit
import os.log

@IBDesignable class BlockButton: UIButton {
    
    private var touched = false
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setup()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setup()
    }
    
    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
    override func draw(_ boundingRect: CGRect) {
        let outlineWidth = boundingRect.width
        let outlineHeight = boundingRect.height
        
        // Find centers of rectangles
        let outlineX: CGFloat = (self.frame.width  - outlineWidth)  / 2
        let outlineY: CGFloat = (self.frame.height - outlineHeight) / 2
        
        // Draw rounded rectangle outline
        let ctx: CGContext = UIGraphicsGetCurrentContext()!
        ctx.saveGState()
        
        let outline = CGRect(x: outlineX, y: outlineY, width: outlineWidth, height: outlineHeight)
        let outlineClipPath: CGPath = UIBezierPath(roundedRect: outline, cornerRadius: 20).cgPath
        
        ctx.addPath(outlineClipPath)
        if touched {
            ctx.setFillColor(UIColor.lightGray.cgColor)
        } else {
            ctx.setFillColor(UIColor.init(red: 232.0/255.0, green: 232.0/255.0, blue: 232.0/255.0, alpha: 1.0).cgColor)
        }
        
        ctx.closePath()
        ctx.fillPath()
        ctx.restoreGState()
    }

    //MARK: Private Methods
    private func setup() {
        let longPressRecognizer = UILongPressGestureRecognizer(target: self, action: #selector(self.pressAction(_:)) )
        longPressRecognizer.minimumPressDuration = 0
        self.addGestureRecognizer(longPressRecognizer)
    }
    
    @objc private func pressAction(_ sender: UILongPressGestureRecognizer) {
        if sender.state == .began {
            touched = true
        } else if sender.state == .ended {
            touched = false
        }
        setNeedsDisplay()
    }
}
