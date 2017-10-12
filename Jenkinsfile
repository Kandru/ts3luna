node {

    stage('scm checkout') {
        checkout scm
    }

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
        } catch (err) {
            junit(keepLongStdio: true, testResults: 'build/test-results/test/*xml')
        }
    }
    stage('deploy staging') {
        try {
            timeout(time: 30, unit: 'SECONDS') {
                input 'Deploy to Staging?'
            }
        } catch (err) {
            echo "Not deploying to staging"
            return
        }
        sh './gradlew deployStaging'
    }
    stage('deploy production') {
        try {
            timeout(time: 30, unit: 'SECONDS') {
                input 'Deploy to production?'
            }
        } catch (err) {
            echo "Not deploying to production"
            return
        }
        sh './gradlew deployProduction'
    }
}
