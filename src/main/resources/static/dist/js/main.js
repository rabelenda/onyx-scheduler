var SERVER_URL = "http://192.168.10.128:8080/";
// var SERVER_URL = "/";

//Make sure jQuery has been loaded before main.js
if (typeof jQuery === "undefined") {
  throw new Error("Onyx Web Requires jQuery");
}

$(function() {
  "use strict";

  jQuery.fn.exists = function() {
    return this.length > 0;
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
      success: function(data, status, xhr) {
        if (data != null) {
          var ArrArr = $.each(data, function(index, value) {
            var slNo = index + 1;
            $('#jobList>tbody').append('<tr onclick="jobDetails(this, event);">' +
              '<td class="slNo">' + slNo + '</td>' +
              '<td class="gName">' + value.group + '</td>' +
              '<td class="jName">' + value.name + '</td>' +
              '<td class="deleteRow">' +
                '<a href="#" onclick="deleteJob(this, event);" class="text-red text-center">' +
                  '<i class="fa fa-trash-o"></i>' +
                '</a>' +
              '</td>' +
            '</tr>');
          });
          // DataTable
          $('#jobList').DataTable({
            "lengthChange": false,
            "columnDefs": [{
              "orderable": false,
              "targets": [-1, -4]
            }]
          });
        }
      },
      error: function(xhr, status, err) {

      }
    });
  }

  //jQueryCron
  var cronVal;
  if ($('.generateCron').exists()) {
    $('#generateCron').cron({
      initial: "*/2 * * * *",
      customValues: {
        "2 Minutes": "*/2 * * * *",
      },
      onChange: function() {
        cronVal = $(this).cron("value");
      }
    });
    $('#generateCron select').addClass('form-control').css({
      'width': 'auto',
      'display': 'inline',
      'margin': '0 5px'
    });
  }

  //DateTime picker
  if ($('#when').exists()) {
    $('#when').datetimepicker({
      sideBySide: true,
      format: 'YYYY-MM-DD LT'
    }).on("changeDate", function(e) {
      var TimeZoned = new Date(e.date.setTime(e.date.getTime() + (e.date.getTimezoneOffset() * 60000)));
    });
  }

  //Add Job
  if ($('#addJob').exists()) {
    $('select#type').on('change', function() {
      if (this.value != '') {
        $('#url').css('display', 'block');
        $('#url input').val($('select#type :selected').text() + '://');
      } else {
        $('#url').css('display', 'none');
        $('#url input').val('');
      }
    });

    $('select#method').on('change', function() {
      if (this.value != '') {
        $('#url').css('display', 'block');
      } else {
        $('#url').css('display', 'none');
      }

      if (this.value == 2) {
        $('#headers, #body').css('display', 'block');
      } else {
        $('#headers, #body').css('display', 'none');
      }
    });

    $('select#triggers').on('change', function() {
      if (this.value == 2) {
        $('#cronExp').css('display', 'block');
        $('#future').css('display', 'none');
      } else if (this.value == 3) {
        $('#cronExp').css('display', 'none');
        $('#future').css('display', 'block');
      } else {
        $('#cronExp, #future').css('display', 'none');
      }
    });
    $('input#auditUrl').change(function() {
      if($('input#auditUrl').hasClass('valid') && $('input#auditUrl').val() != '') {
        $('#auditHeaders').css('display', 'block');
      } else {
        $('#auditHeaders').css('display', 'none');
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
        url: "required",
        body: "required",
        triggers: "required",
        when: "required",
        auditUrl: { pattern : /^(https?|s?ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/}
      },
      messages: {
        auditUrl: "Please enter a valid URL."
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
          var dataHeader = {}
          var headerKey,
              headerValue;
          $('#headersTable>tbody>tr').each(function() {
            headerKey = $(this).find('td input.headerKey').val();
            headerValue = $(this).find('td input.headerValue').val();
            dataHeader[headerKey] = headerValue;
            data['headers'] = dataHeader;
          });
          data['body'] = $('#body textarea').val()
        }
        if ($('select#triggers').val() == 2) {
          data['triggers'] = [{
            "cron": cronVal + ' ?'
          }];
        } else if ($('select#triggers').val() == 3) {
          var dateTime = $('#when').val(),
              isoDate = new Date(dateTime).toISOString();
          data['triggers'] = [{
            "when": isoDate
          }];
        } else {
          data['triggers'] = [{
            "immediate": true
          }];
        }
        if($('#auditUrl input').val() != '') {
          data['auditUrl'] = $('input#auditUrl').val();
          var auditHeader = {}
          var auditKey,
              auditValue;
          $('#auditTable>tbody>tr').each(function() {
            auditKey = $(this).find('td input.headerKey').val();
            auditValue = $(this).find('td input.headerValue').val();
            auditHeader[auditKey] = auditValue;
            data['auditHeaders'] = auditHeader;
          });
        }
        if($('input#maxTrail').val() != '') {
          data['maxTrial'] = $('input#maxTrail').val();
        }
        if($('input#njID').val() != '') {
          data['nextJobId'] = $('input#njID').val();
        }
        console.log(JSON.stringify(data));
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
          success: function(data, status, xhr) {
            location.reload();
          },
          error: function(xhr, status, err) {

          }
        });
      }
    });
  }

  if ($('#auditTable, #headersTable').exists()) {
    autoComplete();
  }
});

function jobDetails(param, event) {
  event.stopPropagation();
  $('#jobDetails').append('<div class="overlay"><i class="fa fa-refresh fa-spin"></i></div>');
  $('#jobDetails').find('.box').remove();
  var gName = $(param).find('.gName').html(),
      jName = $(param).find('.jName').html();
  $.ajax({
    url: SERVER_URL + 'onyx/groups/' + gName + '/jobs/' + jName,
    type: 'GET',
    headers: {
      "Accept": "application/json",
      "Authorization": "Basic YWRtaW46YWRtaW4=",
      "Content-Type": "application/json"
    },
    success: function(data, status, xhr) {
      console.log(data);
      setTimeout(function() {
        $('#jobDetails').find('.overlay').remove();
        var auditUrl,
            auditHeaders,
            maxTrial,
            nextJobId,
            headers,
            body,
            trigger;
        if (data.auditUrl) {
          auditUrl = '<div class="form-group">' +
            '<label>Audit Log URL:</label>' +
            '<span>' + data.auditUrl + '</span>' +
            '</div>';
        } else {
          auditUrl = '';
        }
        if ($.isEmptyObject(data.auditHeaders)) {
          auditHeaders = '';
        } else {
          auditHeaders = '<div class="form-group"><label>Audit Log Headers:</label><div>';
          $.each(data.auditHeaders, function(key, value) {
            auditHeaders += '<div>' + key + ' : ' + value + '</div>';
          });
          auditHeaders += '</div></div>';
        }
        if (data.maxTrial) {
          maxTrial = '<div class="form-group">' +
            '<label>Max Trail:</label>' +
            '<span>' + data.maxTrial + '</span>' +
            '</div>';
        } else {
          maxTrial = '';
        }
        if (data.nextJobId) {
          nextJobId = '<div class="form-group">' +
            '<label>Next Job ID:</label>' +
            '<span>' + data.nextJobId + '</span>' +
            '</div>';
        } else {
          nextJobId = '';
        }
        if ($.isEmptyObject(data.headers)) {
          headers = '';
        } else {
          headers = '<div class="form-group"><label>Headers:</label><div>';
          $.each(data.headers, function(key, value) {
            headers += '<div>' + key + ' : ' + value + '</div>';
          });
          headers += '</div></div>';
        }
        if (data.body) {
          body = '<div class="form-group">' +
            '<label>Body:</label>' +
            '<span>' + JSON.stringify(data.body) + '</span>' +
            '</div>';
        } else {
          body = '';
        }
        if (data.triggers[0].when) {
          trigger = new Date(data.triggers[0]['when']);
        } else if (data.triggers[0].cron) {
          trigger = data.triggers[0]['cron'];
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
              auditUrl +
              auditHeaders +
              maxTrial +
              nextJobId +
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
  var gName = $(param).closest('tr').find('.gName').html(),
      jName = $(param).closest('tr').find('.jName').html();
  $.ajax({
    url: SERVER_URL + 'onyx/groups/' + gName + '/jobs/' + jName,
    type: 'DELETE',
    headers: {
      "Accept": "application/json",
      "Authorization": "Basic YWRtaW46YWRtaW4=",
      "Content-Type": "application/json"
    },
    success: function(data, status, xhr) {
      location.reload();
    },
    error: function(xhr, status, err) {

    }
  });
}

function addHeaders(param) {
  var newRow = $('<tr>' +
    '<td><input type="text" class="form-control headerKey" required></td>' +
    '<td class="lastChild text-center">:</td>' +
    '<td><input type="text" class="form-control headerValue" required></td>' +
    '<td class="lastChild">' +
      '<a class="text-center" onclick="removeHeaders(this);"><i class="fa fa-close"></i></a>' +
    '</td>' +
  '</tr>');

  if ($('#auditTable>tbody>tr:first-child').find('.headerKey').val() == '' || $('#auditTable>tbody>tr:first-child').find('.headerValue').val() == '') {
    $('#addHeaderError').fadeIn();
    setTimeout(function() {
      $('#addHeaderError').fadeOut();
    }, 3000);
  } else {
    $('#auditTable>tbody').append(newRow);
    autoComplete();
  }

  if ($('#headersTable>tbody>tr:first-child').find('.headerKey').val() == '' || $('#headersTable>tbody>tr:first-child').find('.headerValue').val() == '') {
    $('#addHeaderError').fadeIn();
    setTimeout(function() {
      $('#addHeaderError').fadeOut();
    }, 3000);
  } else {
    $('#headersTable>tbody').append(newRow);
    autoComplete();
  }
}

function removeHeaders(param) {
  if ($('#auditTable>tbody>tr').length == 1) {
    $('#removeHeaderError').fadeIn();
    setTimeout(function() {
      $('#removeHeaderError').fadeOut();
    }, 3000);
  } else {
    $(param).closest('tr').remove();
  }

  if ($('#headersTable>tbody>tr').length == 1) {
    $('#removeHeaderError').fadeIn();
    setTimeout(function() {
      $('#removeHeaderError').fadeOut();
    }, 3000);
  } else {
    $(param).closest('tr').remove();
  }
}

function autoComplete() {
  var headerKey = [
    "Accept",
    "Content-Type"
  ];
  $('#auditTable>tbody>tr, #headersTable>tbody>tr').find('.headerKey').autocomplete({
    source: headerKey
  });
  var headerValue = [
    "application/json"
  ];
  $('#auditTable>tbody>tr, #headersTable>tbody>tr').find('.headerValue').autocomplete({
    source: headerValue
  });
}
