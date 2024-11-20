# Nimble Natter Network

## Overview

The **Nimble Natter Network** is a lightweight, JavaFX-based networking application designed to facilitate seamless communication between multiple clients and servers. Built with modularity and scalability in mind, this project showcases essential networking concepts using JavaFX for a modern and user-friendly interface.

## Features

- **Client-Server Architecture**: Establish and manage connections between multiple clients and servers.
- **Secure Communication**: Includes password-protected server access for added security.
- **Dynamic UI**: Responsive and intuitive JavaFX-based user interface.
- **Real-Time Updates**: Observable lists and real-time client management.
- **LAN Discovery**: Automatically discover servers on the local network for quick and easy connections.
- **Key-Based Responses**: Servers respond dynamically to client requests using a flexible key-based mechanism.
- **Port Scanning**: Integrated functionality to scan ports dynamically and connect to available servers.

## Getting Started

### Prerequisites

- **Java 17 or newer**: Ensure you have JavaFX bundled or installed separately.
- **Maven or Gradle**: For dependency management and building the project.
- **Local Network Access**: For utilizing LAN discovery and real-time communication.

## Usage

1. Launch the application and create a new server by entering:
   - **Meeting ID**: A unique identifier for the session.
   - **Password**: A secure key to join the server.
2. Join the server as a client by entering the meeting ID and password.
3. Automatically discover available servers on the local network using the LAN discovery feature.
4. Manage connected clients in real-time via the intuitive interface.

## Architecture

- **Server**: Handles incoming client connections and maintains communication.
- **Client**: Connects to servers and participates in active sessions.
- **ControlUnit**: Acts as the bridge between the UI and backend logic, integrating server discovery and dynamic communication handling.

## Contribution

Contributions are welcome! If you’d like to report a bug or request a feature, feel free to open an issue or submit a pull request.
