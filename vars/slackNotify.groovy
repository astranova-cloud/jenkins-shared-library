def call(status) {
    slackSend(
        channel: "#jenkins-alerts",
        color: status == "SUCCESS" ? "good" : "danger",
        message: "${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        webhookUrl: credentials('slack-webhook')
    )
}