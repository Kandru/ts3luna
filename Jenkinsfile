node {
    checkout scm

    def stagingTimeout = false
    stage('build') {
        sh './gradlew assemble'
    }
    stage('unit test') {
        sh './gradlew check'
    }
    stage('deploy staging') {
        try {
            timeout(time: 30, unit: 'SECONDS') {
                input 'Deploy to Staging?'

            }
        }
        catch (err) {
            def user = err.getCauses()[0].getUser()
            if ('SYSTEM' == user.toString()) {
                echo 'Staging skipped'
                stagingTimeout = true
            }
        }
    }
    stage('deploy production') {
        try {
            timeout(time: 30, unit: 'SECONDS') {
                if (!stagingTimeout) {
                    input 'Deploy to production?'

                }
            }
        }
        catch (err) {
            def user = err.getCauses()[0].getUser()
            if ('SYSTEM' == user.toString()) {
                echo 'Production skipped'
            }
        }
    }
    post {
        always {
            junit(keepLongStdio: true, testResults: 'build/test-results/test/*xml')
        }
    }
}
