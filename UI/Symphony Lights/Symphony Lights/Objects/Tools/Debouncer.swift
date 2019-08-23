//
//  Debouncer.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 19/04/2019.
//  Copyright © 2019 Symphony. All rights reserved.
//

import Foundation

class Debouncer {
    
    typealias Function = () -> Void
    private let timeInterval: TimeInterval
    private var count: Int = 0
    // handler is the closure to run when all the debouncing is done
    // in my case, this is where I sync the data to our server
    var function: Function?
//    {
//        didSet {
//            if self.function != nil {
//                // increment count of callbacks, since each time I get a
//                // callback, I update the handler
////                self.count += 1
//                // start a new asyncAfter call
////                self.renewInterval()
//            }
//        }
//    }
    
    init(timeInterval: TimeInterval) {
        self.timeInterval = timeInterval
        Timer.scheduledTimer(withTimeInterval: timeInterval, repeats: true, block: { (timer) in
            self.function?()
            self.function = nil
        })
    }
    
//    func renewInterval() {
//        DispatchQueue.main.asyncAfter(deadline: .now() + self.timeInterval) {
//            self.runFunction()
//        }
//    }
//
//    private func runFunction() {
//        self.function?()
//        self.function = nil
//    }
    
//    private func runFunction() {
//        // first, decrement count because a callback delay has finished and called runHandler
//        self.count -= 1
//        // only continue to run self.handler if the count is now zero
//        if self.count <= 0 {
//            self.function?()
//            self.function = nil
//        }
//    }
    
    // MARK: Static Functions
    
//    public static func debounce( delay: TimeInterval, queue: DispatchQueue, action: @escaping (()->()) ) {
//        //        var lastFireTime: DispatchTime = DispatchTime.now()
//        //        let dispatchDelay = Int64(delay * Double(NSEC_PER_SEC))
//        //        lastFireTime = DispatchTime.now()
//        queue.asyncAfter(deadline: DispatchTime.now() + delay) {
//            action()
//        }
//    }
}
