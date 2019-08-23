//
//  BlockButton.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 02/11/2018.
//  Copyright © 2018 Symphony. All rights reserved.
//

import UIKit

@IBDesignable class BlockButton: RoundedRect {
    
    private var touched = false
    private var color: UIColor = UIColor.init(red: 232.0/255.0, green: 232.0/255.0, blue: 232.0/255.0, alpha: 1.0)
    
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
//        let outlineClipPath: CGPath = UIBezierPath(roundedRect: outline, cornerRadius: 20).cgPath
//
//        ctx.addPath(outlineClipPath)
//        if touched {
//            ctx.setFillColor(UIColor.lightGray.cgColor)
//        } else {
//            ctx.setFillColor(color.cgColor)
//        }
//
//        ctx.closePath()
//        ctx.fillPath()
//        ctx.restoreGState()
//    }
    
//    override func layoutSubviews() {
//        super.layoutSubviews()
//        rectShape.path = UIBezierPath(roundedRect: self.layer.bounds, byRoundingCorners: [ .topLeft, .topRight], cornerRadii: CGSize(width: 10, height: 10)).cgPath
//    }

    // MARK: Private Methods
    
    private func setup() {
//        let longPressRecognizer = UILongPressGestureRecognizer(target: self, action: #selector(self.pressAction(_:)) )
//        longPressRecognizer.minimumPressDuration = 0
//        self.addGestureRecognizer(longPressRecognizer)
        
//        self.addTarget(self, action: #selector(self.pressDown(_:)), for: .touchDown)
//        self.addTarget(self, action: #selector(self.pressUp(_:)), for: .touchUpInside)
//        self.addTarget(self, action: #selector(self.pressUp(_:)), for: .touchUpOutside)
//        self.addTarget(self, action: #selector(self.pressMoved(_:)), for: .touchDragInside)
    }
    
//    @objc private func pressDown(_ sender: UILongPressGestureRecognizer) {
//        touched = true
//        setNeedsDisplay()
//    }
//
//    @objc private func pressUp(_ sender: UILongPressGestureRecognizer) {
//        cancelTouch()
//    }
    
//    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
//        touchesCancelled(touches, with: event)
//        cancelTouch()
//        super.touchesMoved(touches, with: event)
//    }
    
//    private func cancelTouch() {
//        touched = false
//        setNeedsDisplay()
//    }

    
//    @objc private func pressAction(_ sender: UILongPressGestureRecognizer) {
//        if sender.state == .began {
//            touched = true
//        } else if sender.state == .ended {
//            touched = false
//        }
//        setNeedsDisplay()
//    }
}
