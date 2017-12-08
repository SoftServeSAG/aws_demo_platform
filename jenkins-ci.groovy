node {
    def github_user = "SoftServeSAG"
    def app_names = ["aws_anomaly_detection", "ocean_recognition_app”, “scorecard_builder_app”]
    def s3_bucket = "softserve-analytics"
    def app_port = 3340
    stage('Extract') {
        parallel app_names.collectEntries {app -> [app, {
            node {
                dir("sources/${app}") {
                    git url: "https://github.com/${github_user}/${app}"
                    sh "wget -q https://s3.amazonaws.com/${s3_bucket}/${app}.zip"
                    sh "unzip -o ${app}.zip"
                    sh "rm ${app}.zip"
                }
            }
        }]}
    }
    stage('Build') {
        parallel app_names.collectEntries {app -> [app, {
            node {
                dir("sources/${app}") {
                    sh "docker build --rm --force-rm -t ${app} ."
                }
                // dir("build/${app}") {
                //     sh "docker save -o ${app}.image ${app}"
                // }
            }
        }]}
    }
    stage('Deploy') {
        parallel app_names.collectEntries {app -> [app, {
            node {
                dir("sources/${app}") {
                    sh "sh dockerRun.sh ${app} ${app_port}"
                }
            }
            app_port += 1
        }]}
    }
}
