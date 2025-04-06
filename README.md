# Gmail MCP Server

This project is a Spring Boot MCP (Model Context Protocol) server for Gmail. It provides various tools for interacting with Gmail through the `GMailer` class. Additionally, it includes a client, `ClientStdio`, to test the MCP server functionality.

## Features

- **GMailer Tools**:
    - `trashEmail`: Moves an email to the trash given its ID.
    - `markEmailAsRead`: Marks an email as read given its ID.
    - `sendEmail`: Sends an email to a specified recipient.
    - `getUnreadMessages`: Retrieves unread messages.
    - `readEmail`: Reads an email given its ID.

- **ClientStdio**: A client to test the MCP server functionality using standard I/O.

## Prerequisites

- Java 17 or higher
- Maven
- Gmail API credentials

## Setup

1. **Clone the repository**:
    ```sh
    git clone <repository-url>
    cd <repository-directory>
    ```

2. **Configure Gmail API credentials**:
    - [Create a new Google Cloud project](https://console.cloud.google.com/projectcreate)
    - [Enable the Gmail API](https://console.cloud.google.com/workspace-api/products)
    - [Configure an OAuth consent screen](https://console.cloud.google.com/apis/credentials/consent)
      - Select "external". However, we will not publish the app.
      - Add your personal email address as a "Test user". 
    - Add OAuth scope `https://www.googleapis.com/auth/gmail/modify`
    - [Create an OAuth Client ID](https://console.cloud.google.com/apis/credentials/oauthclient) for application type "Desktop App"
    - Download the JSON file of your client's OAuth keys 
    - Rename the key file to `credentials.json` and save it to your local machine in a secure location. Take note of the location.
    - Place your `credentials.json` file in the `src/main/resources` directory.



3. **Build the project**:
    ```sh
    mvn clean install
    ```

## Running the MCP Server

To start the MCP server, run the following command:
```sh
mvn spring-boot:run
```

## Testing with ClientStdio

The `ClientStdio` class can be used to test the MCP server functionality. It interacts with the server using standard I/O.

Build the project first using the command:
```sh
mvn clean install -DskipTests
```

Then run the `ClientStdio` class.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.