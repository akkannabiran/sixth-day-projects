import org.gradle.api.DefaultTask
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class TestDockerImage extends DefaultTask {

    @Option(description = "The name of the Docker image to test")
    def imageName

    @Option(description = "The tag of the Docker image to test")
    def imageTag

    @Input
    def getImageId() {
        def fullName = imageName
        if (imageTag) {
            fullName += ":${imageTag}"
        }
        return fullName
    }

    @Option(description = "The build number that the image under test should have")
    @Input
    def buildNumber

    @Option(description = "The environment profile under which the container should run")
    @Input
    def env

    @Option(description = "The name and version of the stack under which the container will run (e.g. dev-23, dev-int-22")
    @Input
    def envVersionName

    @Option(description = "The path to the s3 bucket to retrieve the vault access token")
    def vaultAccessTokenPath

    @TaskAction
    void testDockerImage() {
        logger.info("Starting image ${imageId}")
        def containerID = project.execWithOutput {
            executable = "docker"
            if (vaultAccessTokenPath) {
                args = ["run", "-d", "-P", "-e", "ENV_NAME=${env}", "-e", "ENV_VERSION_NAME=${envVersionName}", "-e", "VAULT_ACCESS_TOKEN_PATH=${vaultAccessTokenPath}", imageId]
            } else {
                args = ["run", "-d", "-P", "-e", "ENV_NAME=${env}", "-e", "ENV_VERSION_NAME=${envVersionName}", imageId]
            }

        }
        try {
            def binding = project.execWithOutput {
                executable = "docker"
                args = ["port", containerID, 8080]
            }.replaceAll('.*:', 'localhost:')

            def oneAndOneHalfMinutes = 1.5 * 60
            def baseUrl = "http://${binding}/navigation-batch"
            project.waitForService(baseUrl, buildNumber, env, oneAndOneHalfMinutes)
        } catch (Throwable t) {
            project.exec {
                executable = "docker"
                args = ["logs", containerID]
            }
            throw t
        } finally {
            project.execWithOutput {
                executable = "docker"
                args = ["stop", containerID]
            }
        }
    }
}
