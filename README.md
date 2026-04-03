# Piggy-Saving Backend

Piggy-Saving is a robust digital savings and banking platform built with Spring Boot. It empowers users to manage their finances through main accounts and specialized "Piggy Goals" for targeted savings. The system supports secure transfers, QR-based transactions, and automated email notifications.

## 🚀 Key Features

### 🔐 Security & Authentication
- **JWT Authentication:** Secure stateless session management.
- **Email OTP:** Multi-factor authentication for registration and critical actions.
- **Role-Based Access:** Granular control over system resources.

### 💰 Account & Goal Management
- **Digital Wallet:** Manage main account balances and transaction history.
- **Piggy Goals:** Create savings goals with target amounts and deadlines.
- **Public/Private Goals:** Share piggy goals to allow contributions from other users.
- **Goal Breaking:** Securely transfer funds from a completed or cancelled goal back to the main account.

### 💸 Transfer System
- **P2P Transfers:** Instant peer-to-peer money transfers between users.
- **Main-to-Piggy:** Seamlessly move personal funds into savings goals.
- **Contribution Transfers:** Contribute to friends' or family's public piggy goals.
- **Ledger System:** Double-entry ledger for accurate financial auditing.

### 📲 QR Code Integration
- **QR Generation:** Create dynamic QR codes for payments and contributions.
- **Secure Parsing:** HMAC-validated QR parsing to prevent tampering.
- **Transaction Flow:** Scan-to-pay and scan-to-contribute workflows.

### 📧 Notifications
- **Automated Emails:** Rich HTML notifications for transfers (sender/receiver), goal milestones, and OTPs.
- **Thymeleaf Templates:** Custom-branded email communications.

## 🛠 Tech Stack

- **Framework:** Spring Boot 4.0.3
- **Language:** Java 21 (Preview features enabled)
- **Database:** PostgreSQL
- **Security:** Spring Security, JJWT
- **Mapping:** MapStruct, Lombok
- **QR Engine:** ZXing (Zebra Crossing)
- **Caching:** Caffeine
- **Mailing:** Spring Boot Mail, Thymeleaf
- **Deployment:** Jib (Docker Containerization)

## 🚦 Getting Started

### Prerequisites
- JDK 21
- Maven 3.9+
- PostgreSQL

### Configuration
1. Clone the repository.
2. Create a `secrets.properties` file in the project root or set environment variables:
   ```properties
   # Database
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/piggy_saving
   SPRING_DATASOURCE_USERNAME=your_user
   SPRING_DATASOURCE_PASSWORD=your_password

   # JWT
   JWT_SECRET=your_super_secret_key
   JWT_EXPIRATION=900000

   # Email
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=your_email@gmail.com
   MAIL_PASSWORD=your_app_password

   # QR Security
   QR_HMAC_SECRET=your_hmac_secret
   ```

### Running the Application
```bash
mvn spring-boot:run
```

## 🔌 API Endpoints (Summary)

| Category | Endpoint | Method | Description |
| :--- | :--- | :--- | :--- |
| **Auth** | `/api/v1/auth/**` | POST | Login, Register, Refresh Token |
| **OTP** | `/api/v1/otp/**` | POST | Send/Verify Email OTP |
| **Accounts**| `/api/v1/accounts/**`| GET | View balance, summary, and profile |
| **Piggy** | `/api/v1/piggy/**` | GET/POST/PATCH| Manage savings goals |
| **Transfers**| `/api/v1/transfers/**`| POST | P2P, Main-to-Piggy, Contributions |
| **QR** | `/api/v1/qr/**` | POST | Generate and parse transaction QRs |
| **History** | `/api/v1/transactions/**`| GET | View personal transaction history |

## 🐳 Docker Deployment
The project uses Google Jib for containerization without requiring a Docker daemon during build.
```bash
mvn compile jib:build -Dimage=your-registry/piggy-saving
```

---
Built with ❤️ by the Wing Piggy-Saving Team.
