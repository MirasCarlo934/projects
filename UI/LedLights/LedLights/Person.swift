//
//  Person.swift
//  LedLights
//
//  Created by Carlo Miras on 24/10/2018.
//  Copyright © 2018 Mirascarlo. All rights reserved.
//

import UIKit
import os.log

class Person: NSObject, NSCoding {
    
    //MARK: Properties
    private var name: String
    private var photo: UIImage?
    private var rating: Int
    
    //MARK: Archiving Paths
    static let DocumentsDirectory = FileManager().urls(for: .documentDirectory, in: .userDomainMask).first!
    static let ArchiveURL = DocumentsDirectory.appendingPathComponent("path")
    
    //MARK: Types
    struct PropertyKey {
        static let name = "name"
        static let photo = "photo"
        static let rating = "rating"
    }
    
    init?(name: String, photo: UIImage?, rating: Int) {
        if(name.isEmpty || rating < 0 || photo == nil) {
            return nil
        }
        self.name = name
        self.photo = photo
        self.rating = rating
    }
    
    func getName() -> String {
        return name
    }
    
    func getPhoto() -> UIImage? {
        return photo
    }
    
    func getRating() -> Int {
        return rating
    }
    
    //MARK: NSCoding
    func encode(with aCoder: NSCoder) {
        aCoder.encode(name, forKey: PropertyKey.name)
        aCoder.encode(photo, forKey: PropertyKey.photo)
        aCoder.encode(rating, forKey: PropertyKey.rating)
    }
    
    required convenience init?(coder aDecoder: NSCoder) {
        // The name is required. If we cannot decode a name string, the initializer should fail.
        guard let name = aDecoder.decodeObject(forKey: PropertyKey.name) as? String else {
            os_log("Unable to decode the name for a Person object.", log: OSLog.default, type: .debug)
            return nil
        }
        let photo = aDecoder.decodeObject(forKey: PropertyKey.photo) as? UIImage
        let rating = aDecoder.decodeInteger(forKey: PropertyKey.rating)
        self.init(name: name, photo: photo, rating: rating)
    }
}
