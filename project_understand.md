# Logistics Project Understanding

This document outlines the end-to-end workflow and architecture of the logistics system, including the Dispatcher Web Dashboard, Spring Boot Backend, and Android Driver App.

## Core Workflow

### 1. Order Ingestion (Pending Orders)
- **Upload**: Delivery orders are uploaded into the system (e.g., via CSV upload or direct API integration).
- **Listing**: These orders initially sit in a `PENDING` state and are visible in the dashboard's Pending Orders list. They await grouping and assignment.

### 2. Autonomous Route Grouping (Jsprit Optimization)
- **The Engine**: When the optimization process runs, the system uses the **Jsprit** library to autonomously group the pending orders into logical routes.
- **Vehicle Constraints**: Jsprit looks at the physical constraints of the orders (weight, package count, location) and matches them against vehicle types:
  - **Motorcycle (BIKE)**: Assigned small, localized, lighter parcels (e.g., max 15 items / 20kg).
  - **Van (VAN)**: Assigned bulkier, heavier parcels spread across larger areas (e.g., max 50 items / 300kg).
- **Result**: A set of optimized, unassigned `DeliveryRoute` entities containing ordered sequences of stops.

### 3. Smart Route Allocation
- **Driver Pool**: The system pulls a list of all drivers who are currently `Active` (checked in for their shift).
- **Greedy Allocation**: Routes are automatically allocated to these active drivers based on their **Performance Score**. Drivers with higher scores are prioritized for route assignment. If there is a tie in performance, the system uses a FIFO (First-In, First-Out) method based on who checked in earliest.
- **Dashboard Control**: The dispatcher can trigger this process manually via an "Auto-Allocate" button on the web dashboard.

### 4. Driver Execution (Android Captain App)
- **Shift Management**: Drivers log into the Android app, set up their profiles (selecting their Vehicle Type), and confirm their location to start their shift.
- **Route Execution**: They view their assigned routes and navigate through the sequence of stops.
- **Customer Alerts**: As the driver progresses, the backend triggers automated **Twilio SMS notifications** to customers (e.g., dynamic "2 stops away" alerts).
- **Live Telemetry**: The driver's app sends live GPS coordinates back to the system, allowing dispatchers to monitor their progress on the dashboard map.

### 5. Exception Handling & Support
- **Delivery Failures**: If a driver is unable to deliver a package (e.g., customer unavailable, no access), they mark it as failed on the app.
- **Exception Console**: The failed order immediately reflects in the dashboard's Exception Console.
- **Customer Recovery**: The backend automatically fires a customized failed delivery SMS to the customer, explaining the reason, providing a link to reschedule, and offering a direct dispatcher support phone number.

## Summary of Tech Stack Integration
- **Backend**: Spring Boot (REST APIs, Jsprit routing, Twilio integration, PostgreSQL on Render).
- **Dispatcher UI**: Vue.js Dashboard (Telemetry maps, Roster management, Route allocation tables).
- **Driver App**: Android Compose UI (Location tracking, shift toggles, stop-by-stop navigation).


There is a clean separation of responsibilities in the backend:

Jsprit's Job (Physics & Geometry): It only looks at the orders, map locations, and physical constraints (weight/package limits). It autonomously groups the pending orders and splits them into unassigned Bike and Van routes. It knows nothing about your drivers or their performance.
Auto-Allocator's Job (Business Logic): It steps in after Jsprit is done. It pulls the list of active drivers (who are currently checked-in), sorts them by their Performance Score, and assigns the unassigned routes to the highest performers first (making sure the driver's vehicle type matches the route's requirement).
This ensures your routing is mathematically optimized for distance and capacity, while your dispatching prioritizes your most reliable, actively working staff!