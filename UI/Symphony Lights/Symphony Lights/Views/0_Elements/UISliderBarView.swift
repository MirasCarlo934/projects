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
    @IBInspectable var value: Int = 0  {
        didSet {
            if !setupComplete {
                setup()
            }
            setValue(value: value)
        }
    }
    @IBInspectable var cornerRadius: CGFloat = 0 {
        didSet {
            self.layer.cornerRadius = cornerRadius
            valueRect.layer.cornerRadius = cornerRadius
        }
    }
    @IBInspectable var valueRect: UIView = UIView()
    var valueRectBottom: NSLayoutConstraint!
    var valueRectRight: NSLayoutConstraint!
    var valueRectLeft: NSLayoutConstraint!
    var valueRectTop: NSLayoutConstraint!
    weak var delegate: UISliderBarViewDelegate?
    
    var setupComplete = false
    
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
//    override func draw(_ boundingRect: CGRect) {
//        // Size of rectangles
//        let outlineWidth = boundingRect.width
//        let outlineHeight = boundingRect.height
//        let valueRectHeight = (boundingRect.height/100) * CGFloat(value)
//
//        // Find centers of rectangles
//        let outlineX: CGFloat = (self.frame.width  - outlineWidth)  / 2
//        let outlineY: CGFloat = (self.frame.height - outlineHeight) / 2
//        let valueRectX: CGFloat = (self.frame.width  - outlineWidth)  / 2
//        let valueRectY: CGFloat = self.frame.height - ((boundingRect.height/100) * CGFloat(value))
//
//        // Draw rounded rectangle outline
//        let ctx: CGContext = UIGraphicsGetCurrentContext()!
//        ctx.saveGState()
//
//        let outline = CGRect(x: outlineX, y: outlineY, width: outlineWidth, height: outlineHeight)
//        let outlineClipPath: CGPath = UIBezierPath(roundedRect: outline, cornerRadius: 30).cgPath
//
//        ctx.addPath(outlineClipPath)
//        ctx.setFillColor(UIColor.lightGray.cgColor)
//
//        ctx.closePath()
//        ctx.fillPath()
//        ctx.restoreGState()
//
//        //Draw value rectangle
//
//        ctx.saveGState()
//
//        let valueRect = CGRect(x: valueRectX, y: valueRectY, width: outlineWidth, height: valueRectHeight)
//        let valueRectClipPath: CGPath = UIBezierPath(roundedRect: valueRect, cornerRadius: 30).cgPath
//
//        ctx.addPath(valueRectClipPath)
//        ctx.setFillColor(UIColor.init(red: 232.0/255.0, green: 232.0/255.0, blue: 232.0/255.0, alpha: 1.0).cgColor)
//
//        ctx.closePath()
//        ctx.fillPath()
//        ctx.restoreGState()
//    }
    
    //MARK: Private Methods
    private func setup() {
        let valueRectHeight: CGFloat = (self.frame.height/100) * CGFloat(self.value)
        let longPressRecognizer = UILongPressGestureRecognizer(target: self, action: #selector(self.pressAction(_:)) )
        longPressRecognizer.minimumPressDuration = 0
        
        self.addSubview(valueRect)
        self.sendSubviewToBack(valueRect)
        valueRect.backgroundColor = UIColor(red: 1.0/255 * 232.0, green: 1.0/255 * 232.0, blue: 1.0/255 * 232.0, alpha: 1.0)
        valueRect.translatesAutoresizingMaskIntoConstraints = false
        valueRectBottom = NSLayoutConstraint(item: valueRect, attribute: NSLayoutConstraint.Attribute.bottom, relatedBy: NSLayoutConstraint.Relation.equal, toItem: self, attribute: NSLayoutConstraint.Attribute.bottom, multiplier: 1, constant: 0)
        valueRectRight = NSLayoutConstraint(item: valueRect, attribute: NSLayoutConstraint.Attribute.right, relatedBy: NSLayoutConstraint.Relation.equal, toItem: self, attribute: NSLayoutConstraint.Attribute.right, multiplier: 1, constant: 0)
        valueRectLeft = NSLayoutConstraint(item: valueRect, attribute: NSLayoutConstraint.Attribute.left, relatedBy: NSLayoutConstraint.Relation.equal, toItem: self, attribute: NSLayoutConstraint.Attribute.left, multiplier: 1, constant: 0)
        valueRectTop = NSLayoutConstraint(item: valueRect, attribute: NSLayoutConstraint.Attribute.top, relatedBy: NSLayoutConstraint.Relation.equal, toItem: self, attribute: NSLayoutConstraint.Attribute.top, multiplier: 1, constant: self.frame.height - valueRectHeight)
        self.addConstraints([valueRectTop, valueRectBottom, valueRectLeft, valueRectRight])
        
        self.addGestureRecognizer(longPressRecognizer)
        valueRect.addGestureRecognizer(longPressRecognizer)
        
        setupComplete = true
    }
    
    @objc private func pressAction(_ sender: UILongPressGestureRecognizer) {
//        os_log("%@", "Tap Action: \(sender.location(in: self))")
        setValue(rawY: sender.location(in: self).y)
    }
    
    private func setValue(rawY: CGFloat) {
        let val = Int( ((self.frame.height - rawY) / self.frame.height) * 100 )
        if val < 0 {
            value = 0
        } else if val > 100 {
            value = 100
        } else {
            value = val
        }
        setValue(value: value)
    }
    
    /**
     Set the value of this UISliderBarView.
     
     - Parameter value: The value, from 0 to 100
     */
    private func setValue(value: Int) {
        let valueRectHeight: CGFloat = (self.frame.height/100) * CGFloat(self.value)
        
        valueRectTop.constant = self.frame.height - valueRectHeight
        self.layoutIfNeeded()
        
        if(delegate != nil) {
            delegate!.valueChanged(self)
        }
    }

}

protocol UISliderBarViewDelegate: AnyObject {
    func valueChanged(_ sender: UISliderBarView)
}
