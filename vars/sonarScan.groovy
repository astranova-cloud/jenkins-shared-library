def call(projectKey) {

    withSonarQubeEnv('SonarQube') {

        sh """
        sonar-scanner \
        -Dsonar.projectKey=${projectKey} \
        -Dsonar.sources=.
        """

    }

}