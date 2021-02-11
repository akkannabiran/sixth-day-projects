import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask

class ValidateDockerImage extends DefaultTask {

    @Option(description = "The Docker image to test")
    @Input
    def imageName

    @Option(description = "The build number that the image under test should have")
    @Input
    def buildNumber

    @Option(description = "The environment profile under which the container should run")
    @Input
    def env

    @Option(description = "The path to the s3 bucket to retrieve the vault access token")
    @Input
    def vaultAccessTokenPath

    @Option(description = "The name and version of the stack under which the container will run (e.g. dev-23, dev-int-22")
    @Input
    def envVersionName

    @TaskAction
    void testDockerImage() {
        def containerID = project.execWithOutput {
            executable = "docker"
            args = ["run", "-d", "-p", "8083:8080", "-e", "ENV_NAME=${env}", "-e", "ENV_VERSION_NAME=${envVersionName}",
                    "-e", "VAULT_ACCESS_TOKEN_PATH=${vaultAccessTokenPath}", imageName]
        }
        try {
            def binding = project.execWithOutput {
                executable = "docker"
                args = ["port", containerID, 8080]
            }.replaceAll('.*:', 'localhost:')

            def oneMinute = 3 * 60
            def baseUrl = "http://${binding}"
            project.waitForService(baseUrl, buildNumber, env, oneMinute)
           //TODO : Once consul DOWN health check issue is resolved, TestDockerImage.groovy can be used instead of this file
           /* if(project.verifyService) {
                project.verifyService(baseUrl);
            }
            */
        } finally {
            "docker stop ${containerID}".execute()
        }
    }
}
