# Automated Database Creation/Configuration

Create and configure MySQL and/or PostgreSQL database with Jenkins

## Overview

This project demonstrates the integration of Jenkins and Docker to enable seamless automation of database deployment. Jenkins, an open-source automation server, is used to build, test, and deploy applications, while Docker provides lightweight, isolated containers for packaging and running the applications.

The integration of Jenkins and Docker enables the creation of a robust and scalable CI/CD (Continuous Integration/Continuous Delivery) pipeline, allowing developers to automate the entire software delivery process.

In this project both are used to ensure that in no time you have a running MySQL and/or PostgreSQL fully separated to test and develop your solutions locally.

## Prerequisites

The only requirement is to have Docker installed. 
One of the biggest advantages of using Docker is not having to install any software directly in your machine, all the services will run inside one container fully prepared to host it.

## Setup Instructions

### 1. Clone the project repository

```
git clone https://github.com/manuelcura96/automated-database-creation.git
```

### 2. Run the docker-compose.yaml file
The docker compose file will automate:
- Creation of the Docker network;
- Build of the Jenkins image;
- Run the two required containers (one for Jenkins and the other to execute Docker commands inside Jenkins).

You can find this steps in the Jenkins official documentation:
https://www.jenkins.io/doc/book/installing/docker/

Inside the folder where the project was cloned, please run the following command to start the required services.

```
docker compose up
```

To stop and delete the containers, also inside the folder where the project was cloned, please run the following command .

```
docker compose down
```

### 3. Start using Jenkins

After the Docker containers are running you can use Jenkins in your browser.

```
http://localhost:8080
```

#### 3.1. Get Jenkins password

The first time you run this project, you will need to create one user (be aware this process only needs to happen the first time).

To start the account creation and configuration of Jenkins, you need one password that is displayed in your terminal logs.

Please look in the logs for the password as shown in the image below.

<img width="460" alt="Screenshot 2023-07-05 at 18 13 57" src="https://github.com/manuelcura96/automated-database-creation/assets/55201302/0abcefb8-1413-4147-bfda-4ab06906d1d0">

Now you can paste it and start the configuration of Jenkins.

<img width="460" alt="Screenshot 2023-07-05 at 18 14 25" src="https://github.com/manuelcura96/automated-database-creation/assets/55201302/aa840047-c4fc-4d47-93fc-c6557220d98f">

#### 3.2. Create your Jenkins user

<img width="460" alt="Screenshot 2023-07-05 at 18 15 04" src="https://github.com/manuelcura96/automated-database-creation/assets/55201302/508b7584-1b50-4744-9572-0de7bdf447fc">

### 4. Configure Jenkins

After the initial setup is done, you need to configure the label used by this pipeline in thebuilt-in node. This ensures that the pipeline runs inside this node.

Please configure the **docker-host** label, as shown in the image below.

<img width="460" alt="Screenshot 2023-07-05 at 18 16 18" src="https://github.com/manuelcura96/automated-database-creation/assets/55201302/479dfb83-a978-44c4-b188-a699d701a189">

### 5. Create the Jenkins Pipeline

Now lets create the pipeline to have your databases up and running.

In the pipeline configuration page, please paste the contents of the groovy file **pipelines/build-dev-environment.groovy**.

<img width="460" alt="Screenshot 2023-07-05 at 19 29 36" src="https://github.com/manuelcura96/automated-database-creation/assets/55201302/0e85911a-28c6-413d-bd61-a5e6d93186d3">

### 6. Build the Jenkins Pipeline

Click in save and build the pipeline.

The build will fail the first time because required parameters are missing, click again in **"Build with Parameters"**.

Select the database that you want to use (MySQL/PostgreSQL), the user password and the port (list of ports available in the description).

<img width="460" alt="Screenshot 2023-07-05 at 19 36 33" src="https://github.com/manuelcura96/automated-database-creation/assets/55201302/6cefa96d-0b9e-455a-9f33-dc58236b466d">

## Aditional Configurations

The databases by default will have the contents of the SQL scripts.

- One user created with the username **developer** and the password selected at the begining of the build.
- One test table (**departments**) with multiple example rows.

|    dept    |  dept_name  |
| ---------- | ----------- |
| 1111       | Marketing   |
| ...        | ...         |

## Conclusions

This project provides a basic guide for integrating Jenkins and Docker to automate database deployment. With Jenkins-Docker integration, developers can effectively build, test, and deploy applications in a consistent and scalable manner. Feel free to explore additional features and customization options based on your project requirements.

For more information and advanced usage, refer to the official documentation of Jenkins and Docker.
