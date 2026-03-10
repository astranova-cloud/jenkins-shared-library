def call(Map config) {

    pipeline {
        agent any

        environment {
            AWS_REGION = "us-east-1"
        }

        stages {

            stage('Checkout Code') {
                steps {
                    checkout scm
                }
            }

            stage('SonarQube Scan') {
                steps {
                    sonarScan()
                }
            }

            stage('Filesystem Security Scan') {
                steps {
                    trivyScan()
                }
            }

            stage('Build Docker Image') {
                steps {
                    script {
                        dockerBuild(config.imageName, config.imageTag)
                    }
                }
            }

            stage('Push Docker Image') {
                steps {
                    script {
                        pushECR(config.repo, config.imageName, config.imageTag)
                    }
                }
            }

            stage('Update GitOps Repo') {
    steps {
        withCredentials([usernamePassword(
            credentialsId: 'github-creds',
            usernameVariable: 'GIT_USER',
            passwordVariable: 'GIT_PASS'
        )]) {

            sh '''
            rm -rf astranova-gitops

            git clone https://${GIT_USER}:${GIT_PASS}@github.com/astranova-cloud/astranova-gitops.git

            cd astranova-gitops/k8s

            sed -i "s|image:.*|image: 806889657148.dkr.ecr.us-east-1.amazonaws.com/astranova-app:${BUILD_NUMBER}|g" deployment.yaml

            git config user.email "meherrohit99@gmail.com"
            git config user.name "ROHIT KUMAR MEHER"

            git add .

            git commit -m "Update image ${BUILD_NUMBER}" || echo "No changes to commit"

            git push origin main
            '''
        }
    }
}

        }

        post {

            success {
                slackNotify("SUCCESS")
            }

            failure {
                slackNotify("FAILED")
            }

        }
    }
}
