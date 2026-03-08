def call(status) {

    slackSend(
        channel: '#jenkins-alerts',
        message: "${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    )

}