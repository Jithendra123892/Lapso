# LAPSO System Architecture

```mermaid
graph TB
    subgraph "Client Devices"
        direction TB
        A[Windows Device<br/>PowerShell Agent] --> D
        B[macOS Device<br/>Bash Agent] --> D
        C[Linux Device<br/>Bash Agent] --> D
    end

    subgraph "LAPSO Server"
        direction TB
        D[Spring Boot Application] --> E[(Database<br/>H2/PostgreSQL)]
        D --> F[REST API Layer]
        D --> G[WebSocket Layer]
        D --> H[Business Logic<br/>Services]
        D --> I[Data Access<br/>Repositories]
        D --> J[Security Layer<br/>Authentication]
    end

    subgraph "User Interface"
        K[Web Dashboard<br/>Vaadin UI] --> F
        K --> G
    end

    subgraph "External Services"
        L[IP Geolocation<br/>ip-api.com] <--> D
        M[GPS Services] <--> D
    end

    D <--> A
    D <--> B
    D <--> C

    style A fill:#e1f5fe
    style B fill:#e8f5e8
    style C fill:#fff3e0
    style D fill:#f3e5f5
    style E fill:#ffebee
    style F fill:#bbdefb
    style G fill:#c8e6c9
    style H fill:#f8bbd0
    style I fill:#e1bee7
    style J fill:#d1c4e9
    style K fill:#b2ebf2
    style L fill:#ffccbc
    style M fill:#ffccbc
```

## Component Descriptions

### Client Devices
- **Windows Device**: Runs PowerShell agent that sends heartbeats and executes commands
- **macOS Device**: Runs Bash agent for device tracking and remote actions
- **Linux Device**: Runs Bash agent for cross-platform compatibility

### LAPSO Server (Spring Boot Application)
- **REST API Layer**: Handles HTTP requests from agents and dashboard
- **WebSocket Layer**: Provides real-time updates to the web dashboard
- **Business Logic**: Services implementing core functionality (tracking, commands, geofencing)
- **Data Access**: Repositories for database operations
- **Security Layer**: Authentication and authorization mechanisms

### Database
- **Development**: H2 in-memory database
- **Production**: PostgreSQL database with geospatial extensions
- Stores device information, user data, commands, and tracking history

### User Interface
- **Web Dashboard**: Vaadin-based UI for device management and monitoring
- Real-time map view with device locations
- Command interface for remote actions (lock, alarm, wipe)

### External Services
- **IP Geolocation**: Provides location data when GPS is unavailable
- **GPS Services**: High-accuracy location data when available

## Data Flow

1. **Device Registration**: Agents send initial registration with device information
2. **Heartbeat Communication**: Agents send periodic heartbeats with location data
3. **Command Processing**: Server sends remote commands to devices (lock, alarm, etc.)
4. **Real-time Updates**: WebSocket pushes device updates to dashboard
5. **Data Storage**: All information is persisted in the database
6. **User Interaction**: Administrators interact with the dashboard for device management