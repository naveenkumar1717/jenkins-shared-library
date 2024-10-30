
def call(Map pipelineParams){
    pipeline {
    agent any
    tools{
        maven '3.9.9'
    }

    // environment {
    //     DOCKER_CREDENTIALS_ID = 'docker-login'  
    //     DOCKER_IMAGE_NAME = 'naveentrodo/quizapp'  
    //     DOCKER_TAG = 'latest'
    //     gitUrl = 'https://github.com/naveenkumar1717/SpringBootMonolithicExample.git'
    //     gitBranch = 'main'
    // }
    stages {
        stage('Scm checkout') {
            steps {
             script{
                echo "git checkout"
                def gitUrl = pipelineParams.gitUrl
                def gitBranch = pipelineParams.gitBranch
                checkout([$class: 'GitSCM', 
                branches: [[name: "*/${gitBranch}"]],
                userRemoteConfigs: [
               [credentialsId: 'github-creds', url: gitUrl]
                ]
                  ])

             }
                
            }
        }
        stage ('Build'){
            steps{
              script {
                   sh 'mvn -B -DskipTests clean package'
              }  
            }
        }
        stage('Create image & push to dockerhub'){
            steps{
                    withCredentials([usernamePassword(credentialsId: 'docker-login', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    script {
                    def DOCKER_IMAGE_NAME = pipelineParams.imageName 
                    def DOCKER_TAG= pipelineParams.tag 
                    docker.build("${DOCKER_IMAGE_NAME}:${DOCKER_TAG}")
                    def dockerCredentials = credentials('docker-login') 
                    sh """
                    echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin
                    """
                    sh "docker push ${DOCKER_IMAGE_NAME}:${DOCKER_TAG}"
                  } 
                }  
            }
        } 
        
    }
  }
}
