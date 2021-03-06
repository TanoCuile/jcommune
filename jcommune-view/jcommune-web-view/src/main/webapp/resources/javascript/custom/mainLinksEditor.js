/*
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * This script handles editor popup for links on top of page
 */
//handling click on admin button nearly links

//save id of link to action (delete or edit)
var actionId = null;
var baseUrl = $root;
var externalLinksGroupInTopLine = "ul .links-menu";
var externalLinksGroupId = "#externalLinks";
var externalLinksTableClass = '.list-of-links';
var idToExternalLinkMap = new Object;
var linksEditor;
var bigScreenExternalLinkIdPrefix = "big-screen-external-link-";
var smallScreenExternalLinkIdPrefix = "small-screen-external-link-";


function getLinkById(id) {
    return idToExternalLinkMap[id];
}

$(function () {

    $('.modal-backdrop').live('click', function (e) {
        $('#main-links-editor').find('.close').click();
    });

    $('.btn-navbar').bind('mainLinksPosition', function (e) {
        var sizeMin = $('.btn-navbar').css('display');
        if (sizeMin && sizeMin == 'block') {
            //show in topLine
            $('li.top-line-links').show();
            $('span#externalLinks').parent('div').hide();
        } else {
            //show in mainPage
            $('li.top-line-links').hide();
            $('span#externalLinks').parent('div').show();
        }
    });

    $('.links_editor').on('click', function (e) {
        e.preventDefault();

        var elements = [];
        $(externalLinksGroupId).find('a').each(function (i, elem) {
            var fullId = $(elem).attr('id');
        	var id = extractExternalLinkIdFrom(fullId);
            var externalLink = {};
            externalLink.id = id;
            externalLink.url = $(elem).attr('href');
            externalLink.title = $(elem).text();
            externalLink.hint = $(elem).attr('data-original-title');
            idToExternalLinkMap[id] = externalLink;
            elements[i] = externalLink;
        });
        linksEditor = createMainLinkEditor(elements);

        linksEditor.modal({
            "backdrop": "static",
            "keyboard": true,
            "show": true
        });

        linksEditor.find('#add-main-link').focus();

        linksEditor.unbind();

        linksEditor.find('.close').bind('click', function (e) {
            linksEditor.modal('hide');
            linksEditor.remove();
        });

        toAction('list');

        $(document).delegate('.icon-pencil', 'click', function (e) {
            e.preventDefault();
            actionId = $(e.target).parent('tr').attr('id');
            toAction('edit');
        });
        $(document).delegate('.icon-trash', 'click', function (e) {
            e.preventDefault();
            actionId = $(e.target).parent('tr').attr('id');
            toAction('confirmRemove');

        })


        $('#add-main-link').bind('click', function (e) {
            e.preventDefault();
            toAction('add');
        });


        linksEditor.keydown(Keymaps.linksEditor);

        linksEditor.find('#link-hint').keydown(Keymaps.linksEditorHintInput);

        linksEditor.find('#save-link').keydown(Keymaps.linksEditorSaveButton);

        linksEditor.find('#cancel-link').keydown(Keymaps.linksEditorCancelButton);

        linksEditor.find('#remove-link').keydown(Keymaps.linksEditorRemoveButton);

        Utils.resizeDialog(linksEditor);
    });

});

function extractExternalLinkIdFrom(fullId) {
	if (fullId.indexOf(bigScreenExternalLinkIdPrefix) !== -1) {
		return fullId.replace(bigScreenExternalLinkIdPrefix, "");
	} else if(fullId.indexOf(smallScreenExternalLinkIdPrefix) !== -1) {
		return fullId.replace(smallScreenExternalLinkIdPrefix, "");
	}
}


function createLinksTableRows(elements) {
    var elementHtml = "";
    $.each(elements, function (index, element) {
        var tr =
            $("<tbody/>")//this one is needed because html() returns INNER elements
                .append($("<tr/>").attr("id", element.id)
                    .append($("<td/>").addClass("link-url").text(element.url))
                    .append($("<td/>").addClass("link-hint").text(element.hint))
                    .append($("<td/>").addClass("link-title").text(element.title))
                    .append($("<td/>").addClass("icon-pencil cursor-hand").attr("title", $linksEditIcon))
                    .append($("<td/>").addClass("icon-trash cursor-hand").attr("title", $linksRemoveIcon))
                );
        elementHtml += tr.html();
    })
    return elementHtml;
}

function createMainLinkEditor(elements) {
    return $(' \
        <div class="modal" id="main-links-editor" align="center"> \
            <div class="modal-header"> \
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button> \
                <h3>' + $labelLinksEditor + '</h3> \
            </div> \
            <div class="modal-body"> \
            <table cellpadding="0" cellspacing="0" class="list-of-links"> <tbody>' +
        createLinksTableRows(elements) + '\
            </tbody></table>' +
        createFormElement($labelTitle, 'link-title', 'text', 'hide-element edit-links') +
        createFormElement($labelUrl, 'link-url', 'text', 'hide-element edit-links') +
        createFormElement($labelHint, 'link-hint', 'text', 'hide-element edit-links') + ' \
            <span class="confirm-delete-text remove-links"></span>\
            </div> \
            <div class="modal-footer"> \
                <button id="add-main-link" class="btn btn-block list-of-links hide-element">' + $labelAdd + '</button> \
                <button id="cancel-link" class="btn  edit-links remove-links hide-element">' + $labelCancel + '</button> \
                <button id="save-link" class="btn btn-primary  edit-links hide-element">' + $labelSave + '</button> \
                <button id="remove-link" class="btn btn-primary  remove-links hide-element">' + $labelDelete + '</button> \
            </div> \
        </div> \
        ');
}

function listOfLinksVisible(visible) {

    var intervalID = setInterval(function () {
        if ($('.edit-links').size() > 1) {

            if (visible) {
                $(externalLinksTableClass).removeClass("hide-element");
            }
            else {
                $(externalLinksTableClass).addClass("hide-element");
            }
            clearInterval(intervalID)
        }
    }, '100');
}

function editLinksVisible(visible) {
    var intervalID = setInterval(function () {
        if ($('.edit-links')) {
            if (visible) {
                var link = getLinkById(actionId);
                $('#link-title').val(link.title);
                $('#link-url').val(link.url);
                $('#link-hint').val(link.hint);
                $('.edit-links').removeClass("hide-element");
                $('#link-title').focus();
                //save edited link
                $('#save-link').unbind("click").bind('click', function () {
                    link.title = $('#link-title').val();
                    link.url = $('#link-url').val();
                    link.hint = $('#link-hint').val();
                    $.ajax({
                        url: baseUrl + "/links/save",
                        type: "POST",
                        contentType: "application/json",
                        async: false,
                        data: JSON.stringify(link),
                        success: function (resp) {
                            if (resp.status == "SUCCESS") {
                                updateExternalLink(link, bigScreenExternalLinkIdPrefix);
                                updateExternalLink(link, smallScreenExternalLinkIdPrefix);
                                toAction('list');
                            } else {
                                // remove previous errors and show new errors
                                prepareDialog(linksEditor);
                                showErrors(linksEditor, resp.result, "link-", "");
                            }
                        },
                        error: function (resp) {
                            bootbox.alert($labelErrorLinkSave);
                        }
                    });

                });
                $('#cancel-link').unbind("click").bind('click', function () {
                    toAction('list');
                });
            }
            else {
                $('.edit-links').addClass("hide-element");
            }
            clearInterval(intervalID)
        }
    }, '100');

    function updateExternalLink(externalLink, externalLinkIdPrefix) {
        idToExternalLinkMap[externalLink.id] = externalLink;

        //update in main page
        var link = $(externalLinksGroupId).find('a#' + externalLinkIdPrefix + externalLink.id);
        link.attr('href', externalLink.url);
        link.attr('name', externalLink.title);
        link.text(externalLink.title);
        link.attr('data-original-title', externalLink.hint);

        //update in popup
        $(externalLinksTableClass).find('#' + externalLink.id + ' .link-title').text(externalLink.title + " ");

        //update in top line dropdown
        var link = $(externalLinksGroupInTopLine).find('a#' + externalLink.id);
        link.attr('href', externalLink.url);
        link.attr('name', externalLink.title);
        link.text(externalLink.title);
        link.attr('data-original-title', externalLink.hint);

    }
}

function addLinkVisible(visible) {
    var intervalID = setInterval(function () {
        if ($('.edit-links')) {
            if (visible) {
                $('#link-title').val("");
                $('#link-url').val("");
                $('#link-hint').val("");
                $('.edit-links').removeClass("hide-element");
                $('#link-title').focus();
                $('#save-link').unbind("click").bind('click', function () {
                    var link = {};
                    link.title = $('#link-title').val();
                    link.url = $('#link-url').val();
                    link.hint = $('#link-hint').val();
                    $.ajax({
                        url: baseUrl + "/links/save",
                        type: "POST",
                        contentType: "application/json",
                        async: false,
                        data: JSON.stringify(link),
                        success: function (resp) {
                            if (resp.status == "SUCCESS") {
                                link.id = resp.result.id;
                                addNewExternalLink(link);
                                toAction('list');
                            } else {
                                // remove previous errors and show new errors
                                prepareDialog(linksEditor);
                                showErrors(linksEditor, resp.result, "link-", "");
                            }
                        },
                        error: function (resp) {
                            bootbox.alert($labelErrorLinkSave);
                        }
                    });

                });
                $('#cancel-link').unbind("click").bind('click', function () {
                    toAction('list');
                });
            }
            else {
                $('.edit-links').addClass("hide-element");
            }
            clearInterval(intervalID)
        }
    }, '100');

    function addNewExternalLink(externalLink) {
        idToExternalLinkMap[externalLink.id] = externalLink;

        //add to link editor
        var elements = [];
        elements[0] = externalLink;
        var tableRow = createLinksTableRows(elements);
        $(externalLinksTableClass).find('tbody').append(tableRow);

        //add to main page
        var bigScreenATag = prepareNewLinkATag(externalLink, bigScreenExternalLinkIdPrefix);
        $(externalLinksGroupId).append(bigScreenATag);

        //add to top line dropdown
        var smallScreenATag = prepareNewLinkATag(externalLink, smallScreenExternalLinkIdPrefix);
        $(externalLinksGroupInTopLine).append('<li>' + smallScreenATag + "</li>");

        function prepareNewLinkATag(externalLink, externalLinkIdPrefix) {
            return result = '<span><a id="' + externalLinkIdPrefix + externalLink.id + '"'
                + 'href="' + externalLink.url + '"'
                + 'data-original-title="' + externalLink.hint + '">'
                + externalLink.title + " "
                + '</a></span>';
        }
    }
}

function confirmRemoveVisible(visible) {
    var intervalID = setInterval(function () {
        if ($('.remove-links')) {
            if (visible) {
                var link = getLinkById(actionId);
                var deleteConfirmationMessage = $labelDeleteMainLink.replace('{0}', link.title);
                var removeLinkBut = $('#remove-link');
                $('.confirm-delete-text').text(deleteConfirmationMessage);
                $('.remove-links').removeClass("hide-element");
                removeLinkBut.focus();
                //delete link
                removeLinkBut.unbind("click").bind('click', function () {
                    $.ajax({
                        url: baseUrl + "/links/delete/" + link.id,
                        type: "DELETE",
                        contentType: "application/json",
                        async: false,
                        success: function (data) {
                            if (data.result == true) {
                                idToExternalLinkMap[link.id] = null;
                                //remove from popup editor
                                $(externalLinksTableClass).find('#' + link.id).remove();
                                //remove from main page
                                $(externalLinksGroupId).find('#' + bigScreenExternalLinkIdPrefix + link.id).parent('span').remove();
                                //remove from top line dropdown
                                $(externalLinksGroupInTopLine).find('#' + smallScreenExternalLinkIdPrefix + link.id).parent('li').remove();
                                toAction('list');
                            }
                            else {
                                bootbox.alert($labelErrorLinkDelete);
                            }
                        },
                        error: function () {
                            bootbox.alert($labelErrorLinkDelete);
                        }
                    });
                });
                $('#cancel-link').unbind("click").bind('click', function () {
                    toAction('list');
                });
            }
            else {
                $('.remove-links').addClass("hide-element");
            }
            clearInterval(intervalID)
        }
    }, '100');
}

function toAction(typeOfAction) {
    addLinkVisible(false);
    editLinksVisible(false);
    confirmRemoveVisible(false);
    listOfLinksVisible(false);
    switch (typeOfAction) {
        case "list":
            prepareDialog(linksEditor)
            listOfLinksVisible(true);
            break;
        case "add":
            addLinkVisible(true);
            break;
        case "confirmRemove":
            confirmRemoveVisible(true);
            break;
        case "edit":
            editLinksVisible(true);
            break;
        default:
            listOfLinksVisible(true);
            break;

    }
    Utils.resizeDialog($('#main-links-editor'));
}

