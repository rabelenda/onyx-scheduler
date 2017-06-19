properties([
        [$class  : 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '5']]
])

stage('Package') {
    node() {
        checkout scm
        sh "${tool 'maven'} package"
        sh 'zip -rqdg target/onyx-scheduler.zip target/onyx-scheduler.jar .ebextensions'
        archiveArtifacts artifacts: 'target/onyx-scheduler.zip', fingerprint: true, onlyIfSuccessful: true
    }
}


