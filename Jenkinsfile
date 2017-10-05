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
        junit(keepLongStdio: true, testResults: 'build/test-results/test/*xml')
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
  }
}