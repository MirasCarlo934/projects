//
//  LedLightsTests.swift
//  LedLightsTests
//
//  Created by Carlo Miras on 22/10/2018.
//  Copyright © 2018 Mirascarlo. All rights reserved.
//

import XCTest
@testable import LedLights

class LedLightsTests: XCTestCase {

    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }

    func testExample() {
        // This is an example of a functional test case.
        // Use XCTAssert and related functions to verify your tests produce the correct results.
        
    }

    func testPerformanceExample() {
        // This is an example of a performance test case.
        self.measure {
            // Put the code you want to measure the time of here.
        }
    }

    //MARK: Person Class Tests
    func testPersonInitSuccess() {
        let negaPerson = Person.init(name: "Negative Person", photo: nil, rating: -1)
        XCTAssertNotNil(negaPerson)
        
        let veryHighPerson = Person.init(name: "High Person", photo: nil, rating: 100)
        XCTAssertNotNil(veryHighPerson)
    }
}
