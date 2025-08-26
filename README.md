# 🎓 Shiksha Learning Management System

A modern web-based learning management system designed for tuition centers, enabling seamless communication between teachers and students through assignment sharing, exam scheduling, and assessment management.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18+-blue.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg)](https://www.typescriptlang.org/)
[![AWS](https://img.shields.io/badge/AWS-Cloud%20Infrastructure-orange.svg)](https://aws.amazon.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue.svg)](https://postgresql.org/)

## ✨ Features

### For Teachers
- 📄 **Assignment Management** - Upload and organize PDF assignments by subject and grade level
- 📅 **Exam Scheduling** - Schedule exams with detailed information (date, time, duration, topics, instructions)
- 📊 **Student Dashboard** - View student performance, track assignment submissions, and analyze progress
- 📈 **Grade Management** - Enter exam scores and manage student performance tracking
- 🔍 **Student Analytics** - Drill down to individual student performance across subjects

### For Students  
- 📱 **Subject-Based Dashboard** - Organized tabs for each enrolled subject (Math, Physics, Chemistry)
- 📋 **Assignment Tracking** - View completed and pending assignments with due dates
- 📤 **Assignment Submission** - Upload completed assignments as PDF files
- 🏆 **Grade Tracking** - View exam results and performance trends by subject
- 🔔 **Smart Notifications** - Email alerts for new assignments, exam schedules, and posted grades

### System Features
- 🔐 **Secure Authentication** - JWT-based authentication with role-based access control
- 📧 **Reliable Notifications** - AWS SQS + Amazon SES for guaranteed email delivery
- 📱 **Mobile Responsive** - Optimized for both desktop and mobile devices
- ☁️ **Cloud-Native** - Full AWS infrastructure with global content delivery
- 🛡️ **Security First** - Secure file handling, encrypted storage, and access controls

## 🏗️ Architecture

### Technology Stack

**Frontend**
- **React 18+** with TypeScript
- **React Router DOM** for navigation
- **Tailwind CSS** for styling
- **React Hook Form + Zod** for form validation
- **Axios** for API communication

**Backend**
- **Java 21** with **Spring Boot 3.x**
- **Spring Modulith** for modular architecture
- **Spring Security** with JWT authentication
- **Spring Data JPA** for database operations
- **AWS SDK** for cloud service integration

**Infrastructure**
- **AWS S3 + CloudFront** - Static hosting and global CDN
- **AWS RDS PostgreSQL** - Managed database
- **AWS SQS** - Message queuing for notifications
- **Amazon SES** - Email service
- **AWS EC2/Elastic Beanstalk** - Application hosting

### System Architecture

```
CloudFront CDN → S3 (React App) → Spring Boot API → PostgreSQL
                    ↓                    ↓           ↓
               PDF Storage          SQS Queues   User Data
                                       ↓
                                  Amazon SES
```

## 🚀 Getting Started

### Prerequisites for Development

- **Java 21+**
- **Node.js 18+**
- **AWS Account** with appropriate permissions
- **PostgreSQL 14+** 
- **Git**
- **IDE:** IntelliJ IDEA or VS Code recommended

### Project Setup Overview

This project will be structured as a full-stack application with:
- **Backend:** Spring Boot application using Spring Modulith architecture
- **Frontend:** React application with TypeScript
- **Database:** PostgreSQL with the provided schema
- **Infrastructure:** AWS services (S3, CloudFront, RDS, SQS, SES)

## 📁 Project Structure

### Backend (Spring Modulith)
```
src/main/java/com/shiksha/lms/
├── auth/                 # Authentication Module
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── entity/
├── assignment/           # Assignment Module
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── entity/
├── exam/                 # Examination Module
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── entity/
├── notification/         # Notification Module
│   ├── service/
│   ├── sqs/
│   └── entity/
├── dashboard/            # Dashboard & Analytics Module
│   ├── controller/
│   ├── service/
│   └── dto/
└── shared/               # Shared utilities and configurations
    ├── config/
    ├── exception/
    └── util/
```

### Frontend (React)
```
src/
├── components/           # Reusable UI components
│   ├── common/
│   ├── forms/
│   └── dashboard/
├── pages/                # Main page components
│   ├── auth/
│   ├── teacher/
│   └── student/
├── services/             # API service layers
├── hooks/                # Custom React hooks
├── utils/                # Utility functions
├── types/                # TypeScript type definitions
└── styles/               # CSS and styling files
```

## 🗄️ Database Schema

### Core Tables
- **`users`** - User authentication and profile information
- **`user_subjects`** - Subject enrollment and teaching assignments
- **`subjects`** - Available subjects (Math, Physics, Chemistry)
- **`assignments`** - Teacher-created assignment questions
- **`assignment_submissions`** - Student completed work submissions
- **`exams`** - Scheduled examinations
- **`grades`** - Exam results and scores
- **`email_notifications`** - Notification tracking and history

See the complete database schema in the [PRD document](./PRD.md).

## 🚀 Deployment Planning

### AWS Infrastructure Requirements

**Core Services:**
- **S3 Buckets:** One for React frontend, one for PDF file storage
- **CloudFront Distribution:** CDN for global content delivery
- **RDS PostgreSQL:** Managed database service
- **EC2/Elastic Beanstalk:** Spring Boot application hosting
- **SQS Queues:** Message queuing for reliable notifications
- **Amazon SES:** Email notification service

**Security Setup:**
- IAM roles and policies for secure service access
- Origin Access Control for S3-CloudFront integration
- Security groups for RDS and EC2 access
- SSL certificates through CloudFront

### Infrastructure Deployment Strategy
1. Set up AWS infrastructure using CloudFormation or Terraform
2. Configure database with provided schema
3. Deploy Spring Boot application to EC2/Elastic Beanstalk
4. Build and deploy React application to S3
5. Configure CloudFront distribution with proper error handling
6. Set up monitoring and logging with CloudWatch

## 📋 API Design Overview

### Authentication Endpoints
- User login and registration
- JWT token management and refresh
- Role-based access control validation

### Assignment Management
- Assignment creation and organization by teachers
- Assignment retrieval by students (filtered by grade/subject)
- Assignment submission workflow with file upload to S3

### Dashboard & Analytics
- Teacher dashboard with grade-level filtering and student performance
- Student subject-wise dashboard with grades and assignment status
- Performance analytics and progress tracking

Complete API specifications will be documented using OpenAPI/Swagger once development begins.

## 🔧 Development Guidelines

### Project Structure Planning

#### Backend Structure (Spring Modulith)
```
src/main/java/com/shiksha/lms/
├── auth/                 # Authentication Module
├── assignment/           # Assignment Module  
├── exam/                 # Examination Module
├── notification/         # Notification Module
├── dashboard/            # Dashboard & Analytics Module
└── shared/               # Shared utilities and configurations
```

#### Frontend Structure (React)
```
src/
├── components/           # Reusable UI components
├── pages/                # Main page components
├── services/             # API service layers
├── hooks/                # Custom React hooks
├── utils/                # Utility functions
├── types/                # TypeScript type definitions
└── styles/               # CSS and styling files
```

### Development Best Practices
- Follow Spring Modulith architecture principles from the workshop reference
- Maintain clear module boundaries between auth, assignment, exam, and notification modules
- Use TypeScript for type safety in React frontend
- Implement comprehensive error handling and logging
- Follow AWS security best practices for file upload and storage
- Write unit and integration tests for all modules

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Spring Modulith architecture principles
- Maintain clear module boundaries
- Write comprehensive tests for all features
- Follow established code formatting standards
- Update documentation for any new features

## 📖 Learning Resources

### Spring Modulith
- [Spring Modulith Workshop](https://github.com/sivaprasadreddy/spring-modulith-workshop) - Hands-on exercises and examples
- [Official Spring Modulith Documentation](https://docs.spring.io/spring-modulith/reference/)
- [Spring Modulith Introduction](https://spring.io/blog/2022/10/21/introducing-spring-modulith)

### AWS Integration
- [Spring Cloud AWS Documentation](https://docs.awspring.io/)
- [AWS SDK for Java V2](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [Amazon SES Integration with Spring Boot](https://docs.aws.amazon.com/ses/latest/dg/send-email-spring.html)

## 📄 Documentation

- [Product Requirements Document (PRD)](./prd.md) - Complete project specifications


## 🐛 Issues & Support

- **Bug Reports:** Use GitHub Issues with the `bug` label
- **Feature Requests:** Use GitHub Issues with the `enhancement` label
- **Questions:** Check existing issues or create a new `question` issue

## 📊 Project Status

- 📋 **Planning Phase:** Product Requirements Document completed
- 🏗️ **Architecture Design:** Technical stack and database schema finalized
- ⏳ **Development:** Ready to begin implementation
- 🎯 **Next Steps:** AWS infrastructure setup and backend module development

### Development Milestones
- **Week 1-2:** AWS infrastructure and authentication module
- **Week 3-4:** Assignment and exam management modules
- **Week 5-6:** Frontend development and dashboard implementation
- **Week 7-8:** Integration testing and security validation
- **Week 9:** Production deployment and user training

## 🏷️ License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Product Owner:** Shiksha Tuition Center
- **Development Team:** [Add team member names]
- **Architecture Reference:** Based on [Spring Modulith Workshop](https://github.com/sivaprasadreddy/spring-modulith-workshop)

---

**Built with ❤️ for modern education**
