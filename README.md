# E-Commerce Cart Simulation

A Java-based desktop application developed as an Object-Oriented Programming (OOP) group project.

## Project Overview

The application simulates a basic e-commerce shopping experience. Users can register and log in, browse products, manage a shopping cart, apply discounts, and place orders.

The project is extended with a **Waiting Queue Management Module** for currently unavailable products.

## Technologies Used

*Language:** Java
 **GUI:** Java Swing
 **Database:** SQLite
 **Database Connectivity:** JDBC
 **Version Control:** Git and GitHub- **IDE:** Visual Studio Code

## OOP Concepts

The project demonstrates the following Object-Oriented Programming concepts:

Classes and Objects
 Encapsulation
 Inheritance
 Polymorphism
 Abstraction
 Exception Handling
## Features

 Customer registration and login
 Display available products
 Category-based product browsing
 Product images and details
 Shopping cart management
 Add products with quantity selection
 Remove products from the cart
 Calculate prices with discounts
 Calculate the total order amount
 Checkout and order placement
 Update product stock after a successful order
 Store order details in the SQLite database
 **Join waiting queue for out-of-stock products**
 **Prevent duplicate waiting requests**
 **Display waiting queue position**
 **Automatically process waiting requests when stock becomes available**
**Automatically add fulfilled queue items to the customer's cart**
 **Update product stock and queue status after fulfilment**

## Database

The application uses SQLite to store and manage customer, product, order, order-item, cart, and waiting-queue information. JDBC is used to connect the Java application to the database.

The database contains the following tables:

 Customers
 Admins
 Products
 Orders
 Order_Items
 Waiting_Queue
 Cart_Items
## Waiting Queue Extension

The Waiting Queue module allows customers to join a queue when a product is out of stock.

When stock becomes available, the system processes the waiting queue in FIFO order and automatically adds the eligible customer's requested product quantity to their cart.

## Team

Four-member student project team.

## Project Status

Development and testing completed for the implemented features, including the Waiting Queue Management Module.