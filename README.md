# Alohomora

Alohomora is a Java-based application designed to help you manage your files by encrypting and decrypting them, keeping them safe from prying eyes.

## Features

- **Encrypt Files or Directories**: Secure your sensitive data by encrypting individual files or entire directories.
- **Decrypt Files or Directories**: Access your protected data by decrypting previously secured files or directories.
- **Planned Features**:
    - Automatic re-encryption upon closing the device.
    - Enhanced security measures, including improved hashing algorithms and salted hashes.

## Installation

To install and run Alohomora:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Day-fit/Alohomora.git
   ```
2. **Navigate to the Project Directory**:
   ```bash
   cd Alohomora
   ```
    if this don't work try:

   ```bash
   cd alohomora
   ```
   
4. **Build the Project Using Maven**:
   ```bash
   mvn clean package
   ```
5. **Run the io.github.dayfit.Application**:
   ```bash
   java -jar target/Alohomora-0.2-jar-with-dependencies.jar [options]
   ```

*Note: Ensure you have [Prerequisites](#prerequisites) installed.*

## Usage

Run the application with the appropriate arguments:

- `-h`: Display help information.
- `-e="path"`: Encrypt the specified file or directory.
- `-d="path"`: Decrypt the specified file or directory.
- `-r="path"` Remove protected path.
- `-a="path"` Add new protected path.
- `-p`: Decrypt all the protected paths.
- `-o`: Encrypt all the protected paths.
- `-vp` Display all the protected paths.

**Examples**:

- To encrypt a directory:
  ```bash
  java -jar target/alohomora.jar -e="/path/to/directory"
  ```
- To decrypt a file:
  ```bash
  java -jar target/alohomora.jar -d="/path/to/file"
  ```

## Prerequisites

- Java Development Kit (JDK 23)
- Maven (for building the project)

## License

Alohomora is licensed under the BSD 3-Clause License. This permissive license allows for redistribution and use in source and binary forms, with or without modification, under certain conditions. For more details, see the [BSD 3-Clause License](https://opensource.org/licenses/BSD-3-Clause).

## Contributing

Currently, we are not accepting external contributions. Future updates will provide guidelines for those interested in contributing.

## Contact & Support

If you encounter any issues or have suggestions, please open an issue on our GitHub repository. For direct contact information, visit the [About Me](https://day-fit.github.io) website on my GitHub profile.
