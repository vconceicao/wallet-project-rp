# Wallet Service

This project implements a Wallet Service that allows operations such as creating wallets, depositing funds, withdrawing funds, and transferring money between wallets. The service is designed with scalability and reliability in mind, adhering to modern software design principles.

---

## **How to Run the Service**

### **Run Locally**

#### **Prerequisites:**
1. **JDK 21**: Ensure JDK 21 is installed.
2. **Maven**: Ensure Maven is installed.
3. **MySQL Database**: Have a local MySQL instance running or use Docker Compose to set it up.

#### **Steps:**
1. Clone the repository:
    ```bash
    git clone https://github.com/your-repository/wallet-service.git
    cd wallet-service
    ```
2. Build the project:
    ```bash
    mvn clean install
    ```
3. Update the `application.properties` file in `src/main/resources` with your MySQL credentials:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/wallet_db
    spring.datasource.username=root
    spring.datasource.password=rootpassword
    ```
4. Run the application:
    ```bash
    mvn spring-boot:run
    ```

### **Run with Docker**

#### **Prerequisites:**
1. **Docker**: Ensure Docker is installed.
2. **Docker Compose**: Ensure Docker Compose is installed.

#### **Steps:**
1. Build the Docker image using Spring Boot's plugin:
    ```bash
    mvn spring-boot:build-image
    ```
   Or build using the `Dockerfile`:
    ```bash
    docker build -t wallet-service:latest .
    ```
2. Start the application and database using Docker Compose:
    ```bash
    docker-compose up --build
    ```
3. Access the service:
    - Application: [http://localhost:8080](http://localhost:8080)
    - Adminer (Database UI): [http://localhost:8081](http://localhost:8081)

---

## **Design Choices and Trade-offs**

### **Design Choices:**
1. **Microservices Architecture:**
    - The Wallet Service is implemented as a microservice to allow scalability and separation of concerns.
2. **Database:**
    - **MySQL** is used for persistent storage to ensure ACID compliance and reliability for financial data.
3. **Optimistic Locking:**
    - Implemented on critical entities (e.g., `Wallet`) to handle concurrency and avoid race conditions.
4. **Spring Retry:**
    - Used to handle transient errors and ensure robustness in financial transactions.
5. **Dockerization:**
    - The service is containerized using Docker for portability and ease of deployment.
6. **Cloud-Native Buildpacks:**
    - Leverage Spring Boot's `build-image` capability for building optimized container images.

### **Trade-offs:**
1. **Time Constraints:**
    - Limited the scope of advanced features such as distributed tracing and advanced monitoring.
2. **Simplicity vs. Completeness:**
    - Focused on core functionality (create wallet, deposit, withdraw, transfer) instead of additional features like wallet analytics.
3. **No Advanced Security:**
    - Authentication and authorization are not implemented in this version, as the focus was on core transactional operations.

---

## **Time Spent on the Project**

- **Planning and Design:** 1 hour
- **Development:** 6 hours
- **Testing:** 1 hour
- **Documentation and Cleanup:** 1 hour

**Total Time:** ~9 hours

---

## **Endpoints**

### **1. Create Wallet**
- **POST** `/wallets`
- Request Body:
    ```json
    {
        "userId": "uuid"
    }
    ```
- Response:
    ```json
    {
        "walletId": "uuid"
    }
    ```

### **2. Get Balance**
- **GET** `/wallets/{id}/balance`
- Response:
    ```json
    {
        "balance": 100.00
    }
    ```

### **3. Deposit Funds**
- **POST** `/wallets/{id}/deposit`
- Request Body:
    ```json
    {
        "amount": 50.00
    }
    ```

### **4. Withdraw Funds**
- **POST** `/wallets/{id}/withdraw`
- Request Body:
    ```json
    {
        "amount": 20.00
    }
    ```

### **5. Transfer Funds**
- **POST** `/wallets/transfer`
- Request Body:
    ```json
    {
        "sourceWalletId": "uuid",
        "targetWalletId": "uuid",
        "amount": 10.00
    }
    
