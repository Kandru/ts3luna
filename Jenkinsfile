pipeline {
  agent any
  stages {
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
        input 'Deploy to Staging?'
      }
    }
    stage('deploy production') {
      steps {
        input 'Deploy to production?'
      }
    }
    post {
      always {
        junit(keepLongStdio: true, testResults: 'build/test-results/test/*xml')
      }
    }
  }
}