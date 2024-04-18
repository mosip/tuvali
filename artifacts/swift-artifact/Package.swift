// swift-tools-version:5.7.1
import PackageDescription

let package = Package(
    name: "ios-tuvali-library",
    platforms: [
       .macOS(.v11) // Specify macOS 11.0 as the minimum required version
    ],
    products: [
        .library(
            name: "ios-tuvali-library",
            targets: ["ios-tuvali-library"]),
    ],
    dependencies: [
        // Define our package's dependencies
        .package(url: "https://github.com/1024jp/GzipSwift", from: "6.0.0"),
        .package(url: "https://github.com/ivanesik/CrcSwift.git", from: "0.0.3")
    ],
    targets: [
        .target(
            name: "ios-tuvali-library",
            dependencies: [
                // Include GzipSwift and CrcSwift as dependencies for this target
                .product(name: "Gzip", package: "GzipSwift"),
                .product(name: "CrcSwift", package: "CrcSwift")
            ]),
        .testTarget(
            name: "ios-tuvali-libraryTests",
            dependencies: ["ios-tuvali-library"]),
    ]
)
