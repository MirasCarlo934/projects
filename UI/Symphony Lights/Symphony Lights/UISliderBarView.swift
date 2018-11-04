//
//  UISliderBarView.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 02/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit
import os.log

@IBDesignable class UISliderBarView: UIView {
    
    //MARK: Properties
//    private var boundingRect: CGRect?
//    private var ctx: CGContext?
    @IBInspectable var value: Int = 0
    weak var delegate: UISliderBarViewDelegate?
    
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
        // Size of rectangles
        let outlineWidth = boundingRect.width
        let outlineHeight = boundingRect.height
        let valueRectHeight = (boundingRect.height/100) * CGFloat(value)
        
        // Find centers of rectangles
        let outlineX: CGFloat = (self.frame.width  - outlineWidth)  / 2
        let outlineY: CGFloat = (self.frame.height - outlineHeight) / 2
        let valueRectX: CGFloat = (self.frame.width  - outlineWidth)  / 2
        let valueRectY: CGFloat = self.frame.height - ((boundingRect.height/100) * CGFloat(value))
        
        // Draw rounded rectangle outline
        let ctx: CGContext = UIGraphicsGetCurrentContext()!
        ctx.saveGState()
        
        let outline = CGRect(x: outlineX, y: outlineY, width: outlineWidth, height: outlineHeight)
        let outlineClipPath: CGPath = UIBezierPath(roundedRect: outline, cornerRadius: 30).cgPath
        
        ctx.addPath(outlineClipPath)
        ctx.setFillColor(UIColor.lightGray.cgColor)
        
        ctx.closePath()
        ctx.fillPath()
        ctx.restoreGState()
        
        //Draw value rectangle
        
        ctx.saveGState()
        
        let valueRect = CGRect(x: valueRectX, y: valueRectY, width: outlineWidth, height: valueRectHeight)
        let valueRectClipPath: CGPath = UIBezierPath(roundedRect: valueRect, cornerRadius: 30).cgPath
        
        ctx.addPath(valueRectClipPath)
        ctx.setFillColor(UIColor.init(red: 232.0/255.0, green: 232.0/255.0, blue: 232.0/255.0, alpha: 1.0).cgColor)
        
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
//        os_log("%@", "Tap Action: \(sender.location(in: self))")
        setValue(rawY: sender.location(in: self).y)
    }
    
    private func setValue(rawY: CGFloat) {
        value = Int( ((self.frame.height - rawY) / self.frame.height) * 100 )
        
        if value < 0 {
            value = 0
        } else if value > 100 {
            value = 100
        }
        
        self.setNeedsDisplay()
        
        if(delegate != nil) {
            delegate!.valueChanged(self)
        }
    }

}

protocol UISliderBarViewDelegate: AnyObject {
    func valueChanged(_ sender: UISliderBarView)
}
