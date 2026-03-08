def call() {

    if (fileExists('pom.xml')) {
        return "java"
    }

    if (fileExists('package.json')) {
        return "node"
    }

    if (fileExists('requirements.txt')) {
        return "python"
    }

    if (fileExists('Dockerfile')) {
        return "docker"
    }

    error "Unknown project type"

}