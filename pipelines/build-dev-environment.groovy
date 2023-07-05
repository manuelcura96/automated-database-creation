def ex(exceptionError) {
    currentBuild.result = 'ABORTED'
    error(exceptionError)
}

def createDatabaseContainer() {
    def dateTime = (sh(script: "date +%Y%m%d%H%M%S", returnStdout: true).trim())
    def containerName = "${params.ENVIRONMENT_NAME}_${dateTime}"
    if ("${params.ENVIRONMENT_NAME}" == 'mysql') {
      sh """
      docker run -itd --name ${containerName} --rm -e MYSQL_ROOT_PASSWORD=$params.DATABASE_PASSWORD -p $params.DATABASE_PORT:3306 $params.ENVIRONMENT_NAME:latest
      """
    } else {
      sh """
      docker run -itd --name ${containerName} --rm -e POSTGRES_PASSWORD=$params.DATABASE_PASSWORD -e POSTGRES_DB=devapp -p $params.DATABASE_PORT:5432 -d $params.ENVIRONMENT_NAME:latest
      """
    }
    echo "Docker container $params.ENVIRONMENT_NAME created"
    return containerName
}

def populateDatabase(containerName) {
    try {
      if ("${params.ENVIRONMENT_NAME}" == 'mysql') {
          sh """
          docker exec ${containerName} /bin/bash -c 'mysql --user="root" --password="$params.DATABASE_PASSWORD" < /scripts/create_developer.sql'
          """
      } else {
          sh """
          docker exec ${containerName} /bin/bash -c 'PGPASSWORD="$params.DATABASE_PASSWORD" psql -U postgres -d devapp < /scripts/create_developer.sql'
          """
      } 
    } catch (Exception ex) {
        return false
    }
    echo "Database $params.ENVIRONMENT_NAME populated"
    return true
}

pipeline {
    agent {
        label 'docker-host'
    }
    options {
        disableConcurrentBuilds()
        disableResume()
    }

    parameters {
        choice(name: 'ENVIRONMENT_NAME', choices: ['mysql', 'postgres'], description: 'Select the database of your choice: MySQL or PostgreSQL') 
        password defaultValue: '', description: 'Password to use for MySQL/PostgreSQL container - root user', name: 'DATABASE_PASSWORD'
        string description: 'Database port available options: [3306, 3307, 5432, 5433]', name: 'DATABASE_PORT', trim: true  

        booleanParam(name: 'SKIP_STEP_1', defaultValue: false, description: 'STEP 1 - RE-CREATE DOCKER IMAGE')
    }
  
    stages {
        stage('Validate parameters') {
            steps {
                script {
                    int[] availablePorts = [3306, 3307, 5432, 5433]
                    int port = "${params.DATABASE_PORT}".toInteger()
                    if (port == null || !availablePorts.contains(port)) {
                      ex('Database port must be one of the following options: [3306, 3307, 5432, 5433]');
                    }
                    echo "Parameters validation completed"
                }
            }
        }
        stage('Checkout GIT repository') {
            steps {     
              script {
                git branch: 'master',
                url: 'https://github.com/manuelcura96/automated-database-creation.git'
              }
            }
        }
        stage('Create latest Docker image') {
            steps {     
              script {
                if (!params.SKIP_STEP_1){    
                    echo "Creating docker image with name $params.ENVIRONMENT_NAME using port: $params.DATABASE_PORT"

                    if ("${params.ENVIRONMENT_NAME}" == 'mysql') {
                      sh """
                      sed 's/<PASSWORD>/$params.DATABASE_PASSWORD/g' pipelines/include/mysql/create_developer.template > pipelines/include/mysql/create_developer.sql
                      """

                      sh """
                      docker build -t $params.ENVIRONMENT_NAME:latest -f pipelines/mysql/Dockerfile .
                      """
                    } else {
                      sh """
                      sed 's/<PASSWORD>/$params.DATABASE_PASSWORD/g' pipelines/include/postgresql/create_developer.template > pipelines/include/postgresql/create_developer.sql
                      """

                      sh """
                      docker build -t $params.ENVIRONMENT_NAME:latest -f pipelines/postgresql/Dockerfile .
                      """
                    }

                }else{
                    echo "Skipping STEP1"
                }
              }
            }
        }
        stage('Start new container using latest database image and create user') {
            steps {     
              script {
        
                def containerName = createDatabaseContainer()

                int count = 1
                while(!populateDatabase(containerName) && count <= 5) {
                  echo "Attempting to configure the database: attempt ($count/5)"
                  echo "Sleeping for 5 seconds..."
                  sleep(time:5, unit:"SECONDS")
                  count++
                }
                echo "Docker container created and properly running with $params.ENVIRONMENT_NAME: $containerName"
              }
            }
        }
    }
}
