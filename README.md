# Alohomora

Alohomora is a Java-based application designed to help you manage your files by encrypting and decrypting them, keeping them safe from unauthorized access.

## Features

- Encrypt Files or Directories: Secure your sensitive data by encrypting individual files or entire directories.
- Decrypt Files or Directories: Access your protected data by decrypting previously secured files or directories.
- **Planned Features**:
    - Enhanced security measures, including improved hashing algorithms and salted hashes.

## Installation (from releases)
1. Look for the **latest release** at [releases](https://github.com/Day-fit/Alohomora/releases), and download the jar file
2. Run the Application:
   ```bash
    java -jar target/Alohomora-[version].jar [options]
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

3. Build the Project Using Maven:
   ```bash
   mvn clean package
    ```
4. Run the Application:
   ```bash
    java -jar target/Alohomora-{version}-jar-with-dependencies.jar [options]
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

### Interactive Mode

The new functionality allows you to pass arguments to a running application instance. When the application starts, it launches a background server (by default on port 8765) that allows dynamic command input. In interactive mode:
- A prompt ">" is displayed on the console for entering commands such as -e="/path/to/directory" or -d="/path/to/file".
- Entered commands are sent to the running server, which processes the arguments similarly to when they are provided at startup.
- To verify that the server is running, you can send the ping command, to which the server responds with ALOHOMORA_SERVER.

## Prerequisites

- Java Development Kit (JDK 21) 
- Maven (for building the project)

## License

Alohomora is licensed under the BSD 3-Clause License. This permissive license allows redistribution and use in source and binary forms, with or without modification, under certain conditions. For more details, see the [BSD 3-Clause License](https://opensource.org/licenses/BSD-3-Clause).

## Contributing

Currently, external contributions are not accepted. Future updates will provide guidelines for those interested in contributing.

## Contact & Support

If you encounter any issues or have suggestions, please open an issue on our GitHub repository. For direct contact information, visit the About Me page on my [GitHub profile](https://day-fit.github.io).
