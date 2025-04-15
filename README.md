# Alohomora

Alohomora is a Java-based application designed to help you manage your files by encrypting and decrypting them, keeping them safe from unauthorized access.

## Features

- Encrypt Files or Directories: Secure your sensitive data by encrypting individual files or entire directories.
- Decrypt Files or Directories: Access your protected data by decrypting previously secured files or directories.
- **Encryption on close**: Protected paths are encrypted on closing the device.
  
- **Planned Features**:
    - Enhanced security measures, including improved hashing algorithms and salted hashes.
    - UI Client (based on JavaFX)
    - Instalator + native binary for Linux and Windows (GraalVM)

## Installation (from releases)
1. Look for the **latest release** at [releases](https://github.com/Day-fit/Alohomora/releases), and download the jar file
2. Run the Application:
   ```bash
    java -jar Alohomora-client-[version].jar [options]
    ```
   Make sure you have the [Prerequisites](#prerequisites) installed.

## Installation (from the source code)

1. Clone the Repository:
   ```bash
   git clone https://github.com/Day-fit/Alohomora.git
   ```

2. Navigate to the Project Directory:
   ```bash
   cd Alohomora
   ```

   If this doesn't work, try:
   ```bash
   cd alohomora
   ```

3. Build the Project Using Maven-Wrapper:
   ```bash
   mvnw clean package
    ```
 
4. Move jar files to one directory.
   - Move **Alohomora-background-[version].jar** From: `backgroundServices/target/` to your directory
   - Move **Alohomora-client-[version].jar** From: `clientApp/target/` to your directory

5. Run the Application:
   ```bash
    java -jar Alohomora-client-[version].jar [options]
    ```
   
   Make sure you have the [Prerequisites](#prerequisites) installed.

## Usage

Alohomora supports the following command-line arguments:

- -h – display help information.
- -e="path" – encrypt the specified file or directory.
- -d="path" – decrypt the specified file or directory.
- -a="path" – add a path to the protected paths list.
- -r="path" – remove a path from the protected paths list.
- -p – decrypt all protected paths.
- -c – encrypt all protected paths.
- -vp – display the list of protected paths.

### Why I need to have two files?

The application consists of two components:  
- A client application for user interactions
- A background service that manages file operations
When the application starts, it launches a background server (by default on port 8080),
That's why!

## Prerequisites

- Java Development Kit (JDK 21 or later) (For compiling and running)
- JRE (JDK21 compatible) **(If you're downloading from releases)**

## License

Alohomora is licensed under the BSD 3-Clause License. This permissive license allows redistribution and use in source and binary forms, with or without modification, under certain conditions. For more details, see the [BSD 3-Clause License](https://opensource.org/licenses/BSD-3-Clause).

## Contributing

Currently, external contributions are not accepted. Future updates will provide guidelines for those interested in contributing.

## Contact & Support

If you encounter any issues or have suggestions, please open an issue on our GitHub repository. For direct contact information, visit the About Me page on my [GitHub profile](https://day-fit.github.io).
