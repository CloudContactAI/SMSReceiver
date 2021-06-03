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

package com.cloudcontactai.smsreceiver.service

import com.cloudcontactai.smsreceiver.model.IncomingMessage
import com.cloudcontactai.smsreceiver.model.dto.IncomingMessageDto
import com.cloudcontactai.smsreceiver.model.dto.RequeueRequestDto
import com.cloudcontactai.smsreceiver.model.type.PhoneActionPayload
import com.cloudcontactai.smsreceiver.model.type.PhoneActionType
import com.cloudcontactai.smsreceiver.repository.IncommingMessageRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service

@Service
class IncomingMessageService {

    @Value("\${spring.queue.pendingIncomingSms}")
    lateinit var pendingIncomingSms:String

    @Value("\${cloudcontactai.opt-out-keywords}")
    lateinit var optOutKeywords:String

    @Value("\${cloudcontactai.opt-in-keywords}")
    lateinit var optInKeywords:String

    @Autowired
    lateinit var incomingMessageRepository: IncommingMessageRepository

    @Autowired
    lateinit var jmsTemplate: JmsTemplate

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @Autowired
    lateinit var phoneResponseService: PhoneResponseService

    companion object {
        private val logger = LoggerFactory.getLogger(IncomingMessageService::class.java)
    }

    private fun checkKeywords(keywordsStr: String): List<String> {
        return keywordsStr.toLowerCase().replace(" ", "").split(",")
    }

    fun buildIncomingMessage(request: Map<String, String>): IncomingMessage {
        val incomingMessage = IncomingMessage()
        incomingMessage.bodyRequest = request
        request["Body"]?.let { message ->
            incomingMessage.optOut = checkKeywords(optOutKeywords).contains(message.toLowerCase())
            incomingMessage.optIn = checkKeywords(optInKeywords).contains(message.toLowerCase())
        }
        return incomingMessage
    }

    fun processMessage(incomingMessage: IncomingMessage) {
        incomingMessageRepository.save(incomingMessage)
        queueMessage(incomingMessage)
    }

    fun checkMessageActions(incomingMessage: IncomingMessage): List<String>? {
        try {
            val to = incomingMessage.bodyRequest!!["To"]!!
            val bodyMessage = incomingMessage.bodyRequest!!["Body"]!!
            val actions = mutableListOf<PhoneActionPayload>()
            val responses = mutableListOf<String>()
            val response = phoneResponseService.getResponse(to, bodyMessage)
            response.filter { it.actionPayload != null }.forEach { message ->
                incomingMessage.autoArchive = message.autoArchive || incomingMessage.autoArchive
                val payload = message.actionPayload!!
                actions.add(payload)
                if (message.actionType == PhoneActionType.MESSAGE) {
                    incomingMessage.autoResponse = true
                    payload.message?.let { responseMessage -> responses.add(responseMessage) }
                }
            }
            incomingMessage.actions = actions
            return responses
        } catch (e: Exception) {
            logger.warn("Error checking incoming messages actions", e)
            return null
        }
    }

    fun queueMessage(incomingMessage: IncomingMessage) {
        try {
            val request = incomingMessage.bodyRequest!!
            val newMessage = IncomingMessageDto(request["From"].toString(),
                request["Body"].toString(),
                request["To"],
                incomingMessage.autoArchive,
                incomingMessage.optOut,
                incomingMessage.optIn,
                incomingMessage.actions?.filter { it.listId != null }
            )
            jmsTemplate.convertAndSend(pendingIncomingSms,  ObjectMapper().writeValueAsString(newMessage))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun findMessagesToRequeue(request: RequeueRequestDto): List<IncomingMessage> {
        var where: Criteria
        val query = Query()
        when {
            request.ids != null -> {
                val ids = request.ids.map { id -> ObjectId(id) }
                where = where("_id").`in`(ids)
            }
            request.to != null -> {
                where = where("body_request.To").isEqualTo(request.to)
                if (request.until != null && request.since != null) {
                    where = where.and("created_date").gt(request.since).lt(request.until)
                } else if (request.until != null) {
                    where = where.and("created_date").lt(request.until)
                } else if (request.since != null) {
                    where = where.and("created_date").gt(request.since)
                }
                request.limit?.let { limit -> query.limit(limit) }
            }
            else -> {
                throw Exception("Either \"ids\" or \"to\" param is required")
            }
        }
        query.addCriteria(where)
        return mongoTemplate.find(query, IncomingMessage::class.java)
    }
}
