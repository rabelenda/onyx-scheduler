{
"type":"http",
"name":"${jobName}",
"url":"http://localhost:${port}/${jobName}",
"method": "POST",
"headers": { "Content-Type": "application/json"},
"body": "{\"field\":\"value\"}",
"triggers": [
{"cron":"${cron}"}
]
}
