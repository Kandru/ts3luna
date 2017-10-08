node {
    def stagingTimeout = false

    checkout scm

    stage('build') {
        withEnv(['GRADLE_OPTS="-Dorg.gradle.daemon=false"']) {
            sh './gradlew clean assemble'
        }
    }
    stage('unit test') {
        try {
            withEnv(['GRADLE_OPTS="-Dorg.gradle.daemon=false"']) {
                sh './gradlew check'
            }
        } finally {
            junit(keepLongStdio: true, testResults: 'build/test-results/test/*xml')
        }
    }
    stage('deploy staging') {
        try {
            timeout(time: 30, unit: 'SECONDS') {
                input 'Deploy to Staging?'
            }
        } catch (err) {
            echo 'Not deploying to staging.'
            def user = err.getCauses()[0].getUser()
            if ('SYSTEM' == user.toString()) {
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
        } catch (err) {
            echo 'Not deploying to production.'
        }
    }
}
