{
"type":"http",
"id":"${jobId}",
"name":"httpJobWithNonDefaultParams",
"url":"http://localhost/test",
"method": "PUT",
"headers": { "Content-Type": "application/json"},
"body": "{\"field\":\"value\"}",
"triggers": [
{"cron":"0/2 * * * * ?"}
]
}
