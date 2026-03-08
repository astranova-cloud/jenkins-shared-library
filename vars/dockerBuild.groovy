def call(imageName, tag) {

    sh """
    docker build -t ${imageName}:${tag} .
    """

}