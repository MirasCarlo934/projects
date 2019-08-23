//
//  0_Errors.swift
//  Symphony Lights
//
//  Created by Carlo Miras on 03/01/2019.
//  Copyright © 2019 Symphony. All rights reserved.
//

import Foundation

struct InvalidArgumentError: Error {
    enum ErrorKind {
        case unrecognizedArgument
    }
    
    let kind: ErrorKind
    let errorMessage: String
}
