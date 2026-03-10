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
                    script {

                        sh """
                        rm -rf astranova-gitops
                        
                        git clone https://github.com/astranova-cloud/astranova-gitops.git

                        cd astranova-gitops/k8s

                        sed -i 's|image: .*|image: ${config.repo}:${config.imageTag}|' deployment.yaml

                        git config user.email "meherrohit99@gmail.com"
                        git config user.name "kumar99786"

                        git add .
                        git commit -m "Update image to ${config.imageTag}"
                        git push
                        """
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
