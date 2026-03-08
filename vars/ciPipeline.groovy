def call(Map config) {

pipeline {

agent any

environment {

IMAGE_NAME = config.imageName
ECR_REPO   = config.repo
IMAGE_TAG  = "${BUILD_NUMBER}"

}

stages {

stage('Checkout') {

steps {
checkout scm
}

}

stage('Detect Project Type') {

steps {

script {

PROJECT_TYPE = projectDetect()

echo "Detected project type: ${PROJECT_TYPE}"

}

}

}

stage('SonarQube Scan') {

steps {
sonarScan(config.projectKey)
}

}

stage('Quality Gate') {

steps {

timeout(time: 5, unit: 'MINUTES') {

waitForQualityGate abortPipeline: true

}

}

}

stage('Trivy Scan') {

steps {
trivyScan()
}

}

stage('Build') {

steps {

script {

if(PROJECT_TYPE == "java") {

sh "mvn clean package"

}

else if(PROJECT_TYPE == "node") {

sh "npm install"

}

else if(PROJECT_TYPE == "python") {

sh "pip install -r requirements.txt"

}

else if(PROJECT_TYPE == "docker") {

dockerBuild(IMAGE_NAME, IMAGE_TAG)

}

}

}

}

stage('Push Image') {

steps {
pushECR(ECR_REPO, IMAGE_NAME, IMAGE_TAG)
}

}

}

post {

success {

slackNotify("SUCCESS")

emailext(
subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
body: "Build Success\n\n${env.BUILD_URL}",
to: "meherrohit99@gmail.com"
)

}

failure {

slackNotify("FAILED")

emailext(
subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
body: "Build Failed\n\n${env.BUILD_URL}",
to: "meherrohit99@gmail.com"
)

}

}

}
}