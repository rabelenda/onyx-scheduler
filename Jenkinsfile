properties([
        [$class  : 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '5']]
])

stage('Package') {
    node() {
        checkout scm
        sh "${tool 'maven'} package"
        sh 'mv target/onyx-scheduler.jar target/application.jar'
        sh 'zip -rqdg target/onyx-scheduler.zip target/application.jar .ebextensions'
        archiveArtifacts artifacts: 'target/onyx-scheduler.zip', fingerprint: true, onlyIfSuccessful: true
    }
}


