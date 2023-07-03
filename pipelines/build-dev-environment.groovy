def ex(exceptionError) {
    currentBuild.result = 'ABORTED'
    error(exceptionError)
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
        choice(name: 'ENVIRONMENT_NAME', choices: ['mysql', 'postgresql'], description: 'Value must be mysql or postgresql') 
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
                credentialsId: 'github-token',
                url: 'https://github.com/manuelcura96/automated-database-creation.git'
              }
            }
        }
        stage('Create latest Docker image') {
            steps {     
              script {
                if (!params.SKIP_STEP_1){    
                    echo "Creating docker image with name $params.ENVIRONMENT_NAME using port: $params.DATABASE_PORT"
                    sh """
                    sed 's/<PASSWORD>/$params.DATABASE_PASSWORD/g' pipelines/include/create_developer.template > pipelines/include/create_developer.sql
                    """

                    sh """
                    docker build pipelines/ -t $params.ENVIRONMENT_NAME:latest
                    """

                }else{
                    echo "Skipping STEP1"
                }
              }
            }
        }
        stage('Start new container using latest database image and create user') {
            steps {     
              script {
                
                def dateTime = (sh(script: "date +%Y%m%d%H%M%S", returnStdout: true).trim())
                def containerName = "${params.ENVIRONMENT_NAME}_${dateTime}"

                sh """
                docker run -itd --name ${containerName} --rm -e MYSQL_ROOT_PASSWORD=$params.DATABASE_PASSWORD -p $params.DATABASE_PORT:3306 $params.ENVIRONMENT_NAME:latest
                """

                sh """
                docker exec ${containerName} /bin/bash -c 'mysql --user="root" --password="$params.DATABASE_PASSWORD" < /scripts/create_developer.sql'
                """

                echo "Docker container created: $containerName"

              }
            }
        }
    }

}
