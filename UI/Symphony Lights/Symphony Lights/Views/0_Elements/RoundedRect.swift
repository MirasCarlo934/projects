//
//  RoundedRect.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 20/04/2019.
//  Copyright © 2019 Symphony. All rights reserved.
//

import UIKit

@IBDesignable class RoundedRect: UIView {
    
    // MARK: Properties
    @IBInspectable private var color: UIColor = UIColor.init(red: 232.0/255.0, green: 232.0/255.0, blue: 232.0/255.0, alpha: 1.0) {
        didSet {
            setNeedsDisplay()
        }
    }
    @IBInspectable private var cornerRadius: CGFloat = 10 {
        didSet {
            setNeedsDisplay()
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }
    
    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
//    override func draw(_ boundingRect: CGRect) {
//        super.draw(boundingRect)
//        self.layer.cornerRadius = cornerRadius
//        self.layer.backgroundColor = color.cgColor
//        self.clipsToBounds = true
//        let outlineWidth = boundingRect.width
//        let outlineHeight = boundingRect.height
//
//        // Find centers of rectangles
//        let outlineX: CGFloat = (self.frame.width  - outlineWidth)  / 2
//        let outlineY: CGFloat = (self.frame.height - outlineHeight) / 2
//
//        // Draw rounded rectangle outline
//        let ctx: CGContext = UIGraphicsGetCurrentContext()!
//        ctx.saveGState()
//
//        let outline = CGRect(x: outlineX, y: outlineY, width: outlineWidth, height: outlineHeight)
//        let outlineClipPath: CGPath = UIBezierPath(roundedRect: outline, cornerRadius: cornerRadius).cgPath
//
//        ctx.addPath(outlineClipPath)
//        ctx.setFillColor(color.cgColor)
//
//        ctx.closePath()
//        ctx.fillPath()
//        ctx.restoreGState()
//    }
    
    // MARK: Public Methods
    
    func setColor(color: UIColor) {
        self.color = color
        setNeedsDisplay()
    }
}
