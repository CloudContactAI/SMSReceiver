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

package com.cloudcontactai.smsreceiver.listener

import com.cloudcontactai.smsreceiver.service.PhoneResponseService
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component

@Component
class DeleteClientsResponsesListener {
    @Autowired
    lateinit var phoneResponseService: PhoneResponseService

    @JmsListener(destination = "\${cloudcontactai.queue.deleteClientPhoneResponses}")
    fun receiveMessage(message: String) {
        try {
            val typeRef: TypeReference<List<Long>> = object : TypeReference<List<Long>>() {}
            val clients: List<Long> = ObjectMapper().readValue(message, typeRef)
            phoneResponseService.deleteByClients(clients)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}