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