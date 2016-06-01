//var SERVER_URL = "http://192.168.10.128:8080/";
 var SERVER_URL = "/";

//Make sure jQuery has been loaded before main.js
if (typeof jQuery === "undefined") {
  throw new Error("Onyx Web Requires jQuery");
}

$(function() {
    "use strict";

    jQuery.fn.exists = function() {
        return this.length>0;
    }

    //Job List
    if ($('#jobList').exists()) {
        $.ajax({
            url: SERVER_URL + 'onyx/jobs',
            type: 'GET',
            headers: {
                "Accept": "application/json",
                "Authorization": "Basic YWRtaW46YWRtaW4=",
                "Content-Type": "application/json"
            },
            success: function(data,status,xhr) {
                if (data != null) {
                    var ArrArr = $.each(data, function (index,value) {
                        var slNo = index + 1;
                        $('#jobList>tbody').append('<tr onclick="getRowValue(this, event);">' +
                            '<td class="slNo">' + slNo + '</td>' +
                            '<td class="gName">' + value.group + '</td>' +
                            '<td class="jName">' + value.name + '</td>' +
                            '<td class="deleteRow">' +
                                '<a href="#" onclick="deleteJob(this, event);" class="text-red text-center"><i class="fa fa-trash-o"></i></a>' +
                            '</td>' +
                        '</tr>');
                    });
                    $('#jobList').DataTable({
                        "lengthChange": false,
                        "columnDefs": [{
                            "orderable": false,
                            "targets":  [-1, -4]
                        }]
                    });
                }
            },
            error: function(xhr, status, err) {

            }
        });
    }

    //DateTime picker
    if ($('#when').exists()) {
        $('#when').datetimepicker({
            sideBySide: true,
            format: 'YYYY-MM-DD LT'
        }).on("changeDate", function (e) {
            var TimeZoned = new Date(e.date.setTime(e.date.getTime() + (e.date.getTimezoneOffset() * 60000)));
        });
    }

    //jQueryCron
    var cronVal;
    if ($('.generateCron').exists()) {
        $('#generateCron').cron({
            initial: "*/2 * * * *",
            customValues: {
                "2 Minutes" : "*/2 * * * *",
            },
            onChange: function() {
                cronVal = $(this).cron("value");
            }
        });
        $('#generateCron select').addClass('form-control').css({
            'width' : 'auto',
            'display' : 'inline',
            'margin' : '0 5px'
        });
    }

    //Add Job
    if ($('#addJob').exists()) {
        $('select#type').on('change', function() {
            if (this.value != '') {
                $('#url').css('display','block');
                $('#url input').val($('select#type :selected').text() + '://');
            } else {
                $('#url').css('display','none');
                $('#url input').val('');
            }
        });

        $('select#method').on('change', function() {
            if (this.value != '') {
                $('#url').css('display','block');
            } else {
                $('#url').css('display','none');
            }

            if (this.value == 2) {
                $('#headers, #body').css('display','block');
            } else {
                $('#headers, #body').css('display','none');
            }
        });

        $('select#triggers').on('change', function() {
            if (this.value == 2) {
                $('#cronExp').css('display','block');
                $('#future').css('display','none');
            } else if (this.value == 3) {
                $('#cronExp').css('display','none');
                $('#future').css('display','block');
            } else {
                $('#cronExp, #future').css('display','none');
            }
        });

        jQuery.validator.addMethod("defaultInvalid", function(value, element) {
            return value != element.defaultValue;
        }, "");

        $("#addJob").validate({
            rules: {
                groupName: "required",
                jobName: "required",
                type: "required",
                method: "required",
                co_url: "required",
                headerName: "required",
                headerValue: "required",
                body: "required",
                triggers: "required",
                when: "required",
            },
            submitHandler: function(e) {
                var data = {
                    "group": $('input#groupName').val(),
                    "type": $('select#type :selected').text(),
                    "name": $('input#jobName').val(),
                    "method": $('select#method :selected').text(),
                    "url": $('#url input').val(),
                };
                if ($('select#method ').val() == 2) {
                    var dataHeader = {};
                    var headerName = $('select#headerName :selected').text();
                    var headerValue = $('select#headerValue :selected').text();
                    dataHeader[headerName] = headerValue;
                    data['headers'] = dataHeader;
                    data['body'] = $('#body textarea').val()
                }
                if ($('select#triggers').val() == 2) {
                    data['triggers'] = [{ "cron": cronVal + ' ?' }];
                } else if ($('select#triggers').val() == 3) {
                    var dateTime = $('#when').val();
                    var isoDate = new Date(dateTime).toISOString();
                    data['triggers'] = [{ "when": isoDate }];
                } else {
                    data['triggers'] = [{ "immediate": true }];
                }
                $.ajax({
                    url: SERVER_URL + 'onyx/groups/' + $('input#groupName').val() + '/jobs',
                    type: 'POST',
                    data: JSON.stringify(data),
                    dataType: 'json',
                    headers: {
                        "Accept": "application/json",
                        "Authorization": "Basic YWRtaW46YWRtaW4=",
                        "Content-Type": "application/json"
                    },
                    success: function(data,status,xhr) {
                        location.reload();
                    },
                    error: function(xhr, status, err) {

                    }
                });
            }
        });
    }
});

function getRowValue(param, event) {
    event.stopPropagation();
    $('#jobDetails').append('<div class="overlay"><i class="fa fa-refresh fa-spin"></i></div>');
    $('#jobDetails').find('.box').remove();
    
    var gName = $(param).find('.gName').html();
    var jName = $(param).find('.jName').html();
    $.ajax({
        url: SERVER_URL + 'onyx/groups/' + gName + '/jobs/' + jName,
        type: 'GET',
        headers: {
            "Accept": "application/json",
            "Authorization": "Basic YWRtaW46YWRtaW4=",
            "Content-Type": "application/json"
        },
        success: function(data,status,xhr) {
            console.log(data);
            setTimeout(function () {
                $('#jobDetails').find('.overlay').remove();
                var trigger,
                    Headers
                    body;
                if (data.triggers[0].when) {
                    trigger = new Date(data.triggers[0]['when']);
                } else if (data.triggers[0].cron) {
                    trigger = data.triggers[0]['cron'];
                }
                if ($.isEmptyObject(data.headers)) {
                    headers = '';
                } else {
                    $.each(data.headers, function (key,value) {
                        headers = '<div class="form-group">' +
                            '<label>Headers:</label>' +
                            '<span>' + key + ' : </span>' +
                            '<span>' + value + '</span>' +
                        '</div>';
                    });
                    
                }
                if (data.body) {
                    body = '<div class="form-group">' +
                        '<label>Body:</label>' +
                        '<span>' + JSON.stringify(data.body) + '</span>' +
                    '</div>';
                } else {
                    body = '';
                }
                $('#jobDetails').append(
                  '<div class="box">' +
                    '<div class="box-header with-border">' +
                      '<h3 class="box-title">Job Details</h3>' +
                    '</div>' +
                    '<div class="box-body">' +
                      '<div class="form-group">' +
                        '<label>Group Name:</label>' +
                        '<span>' + data.group + '</span>' +
                      '</div>' +
                      '<div class="form-group">' +
                        '<label>Job Name:</label>' +
                        '<span>' + data.name + '</span>' +
                      '</div>' +
                      '<div class="form-group">' +
                        '<label>Type:</label>' +
                        '<span>' + data.type + '</span>' +
                      '</div>' +
                      '<div class="form-group">' +
                        '<label>Method:</label>' +
                        '<span>' + data.method + '</span>' +
                      '</div>' +
                      '<div class="form-group">' +
                        '<label>URL:</label>' +
                        '<span>' + data.url + '</span>' +
                      '</div>' +
                      headers +
                      body +
                      '<div class="form-group">' +
                        '<label>Triggers:</label>' +
                        '<span>' + trigger + '</span>' +
                      '</div>' +
                    '</div>' +
                  '</div>'
                );
            }, 1000);
        },
        error: function(xhr, status, err) {

        }
    });
}

function deleteJob(param, event) {
    event.stopPropagation();
    var row =  $(param).closest('tr');  
    var gName = $(row).find('.gName').html();
    var jName = $(row).find('.jName').html();
    $.ajax({
        url: SERVER_URL + 'onyx/groups/' + gName + '/jobs/' + jName,
        type: 'DELETE',
        headers: {
            "Accept": "application/json",
            "Authorization": "Basic YWRtaW46YWRtaW4=",
            "Content-Type": "application/json"
        },
        success: function(data,status,xhr) {
            location.reload();
        },
        error: function(xhr, status, err) {

        }
    });
}