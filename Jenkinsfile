node {
    stages {
        def stagingTimeout = false
        stage('build') {
            steps {
                sh './gradlew assemble'
            }
        }
        stage('unit test') {
            steps {
                sh './gradlew check'
            }
        }
        stage('deploy staging') {
            steps {
                try {
                    timeout(time: 30, unit: 'SECONDS') {
                        input 'Deploy to Staging?'

                    }
                }
                catch (err) {
                    def user = err.getCauses()[0].getUser()
                    if('SYSTEM' == user.toString()) {
                        echo 'Staging skipped'
                        stagingTimeout = true
                    }
                }
            }
        }
        stage('deploy production') {
            steps {
                try {
                    timeout(time:30, unit: 'SECONDS') {
                        if(!stagingTimeout) {
                            input 'Deploy to production?'

                        }
                    }
                }
                catch (err) {
                    def user = err.getCauses()[0].getUser()
                    if('SYSTEM' == user.toString()) {
                        echo 'Production skipped'
                    }
                }
            }
        }
    }
    post {
        always {
            junit(keepLongStdio: true, testResults: 'build/test-results/test/*xml')
        }
    }
}
