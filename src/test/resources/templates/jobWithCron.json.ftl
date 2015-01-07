{
"type":"http",
"name":"${jobName}",
"method":"GET",
"url":"http://localhost:${port}/${jobName}",
"triggers": [
{"cron":"${cron}"}
]
}
