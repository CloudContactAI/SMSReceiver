/* Copyright 2021 Cloud Contact AI, Inc. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.cloudcontactai.smsreceiver.controller


import com.cloudcontactai.smsreceiver.model.IncomingMessage
import com.cloudcontactai.smsreceiver.model.dto.RequeueRequestDto
import com.cloudcontactai.smsreceiver.service.IncomingMessageService
import com.twilio.twiml.MessagingResponse
import com.twilio.twiml.messaging.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException


@RestController
@Validated
@RequestMapping("/api/v1/sms/incoming")
class IncomingMessageController {

    @Autowired
    lateinit var incomingMessageService: IncomingMessageService

    @RequestMapping(value = [""], method = [(RequestMethod.POST)],
            consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = ["text/html"])
    fun smsReceived(@RequestParam body: Map<String, String>) : String {
        val incomingMessage = incomingMessageService.buildIncomingMessage(body)
        val builder = MessagingResponse.Builder()
        incomingMessageService.checkMessageActions(incomingMessage)?.forEach { responseMessage ->
            builder.message(Message.Builder(responseMessage).build())
        }
        incomingMessageService.processMessage(incomingMessage)
        return builder.build().toXml()
    }

    @PostMapping("/queue")
    fun requeueMessages(@RequestBody request: RequeueRequestDto): List<IncomingMessage> {
        try {
            val messages = incomingMessageService.findMessagesToRequeue(request)
            if (request.queue) {
                messages.forEach { incomingMessageService.queueMessage(it) }
            }
            return messages
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }
}

