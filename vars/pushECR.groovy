def call(repo, imageName, tag) {

    sh """

    aws ecr get-login-password --region us-east-1 | \
    docker login --username AWS --password-stdin ${repo}

    docker tag ${imageName}:${tag} ${repo}:${tag}

    docker push ${repo}:${tag}

    """

}