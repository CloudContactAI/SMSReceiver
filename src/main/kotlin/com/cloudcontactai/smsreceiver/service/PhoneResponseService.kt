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

import com.cloudcontactai.smsreceiver.model.PhoneResponse
import com.cloudcontactai.smsreceiver.model.ResponseKeyword
import com.cloudcontactai.smsreceiver.model.dto.ClientPhoneResponseDto
import com.cloudcontactai.smsreceiver.model.dto.PhoneResponseRequestDto
import com.cloudcontactai.smsreceiver.repository.PhoneResponseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.math.max


@Service
class PhoneResponseService {

    @Autowired
    lateinit var phoneResponseRepository: PhoneResponseRepository

    fun getByClient(clientId: Long): List<ClientPhoneResponseDto> {
        return phoneResponseRepository.findAllByClientId(clientId).map { ClientPhoneResponseDto(it) }
    }

    fun getResponse(phone: String, message: String): List<PhoneResponse> {
        return phoneResponseRepository.findAllByPhoneNumberAndKeywordsKeywordContainsIgnoreCase(phone, message.trim())
    }

    fun getPhoneByClient(clientId: Long, phoneNumber: String): List<ClientPhoneResponseDto> {
        return phoneResponseRepository.findAllByClientIdAndPhoneNumber(clientId, phoneNumber)
            .map { ClientPhoneResponseDto(it) }
    }

    fun saveAll(clientId: Long, phoneNumber: String, request: List<PhoneResponseRequestDto>): List<ClientPhoneResponseDto> {
        val current = phoneResponseRepository.findAllByClientIdAndPhoneNumber(clientId, phoneNumber)
        val max = max(request.size, current.size)
        val currentIterator = current.iterator()
        val requestIterator = request.iterator()
        val result = mutableListOf<ClientPhoneResponseDto>()
        for (i in 1..max) {
            val phoneResponse: PhoneResponse = if (currentIterator.hasNext()) {
                currentIterator.next()
            } else {
                PhoneResponse(clientId, phoneNumber)
            }
            if (requestIterator.hasNext()) {
                result.add(savePhoneByClient(phoneResponse, requestIterator.next()))
            } else if (phoneResponse.id > 0) {
                delete(phoneResponse)
            }
        }
        return result
    }

    fun savePhoneByClient(phoneResponse: PhoneResponse, request: PhoneResponseRequestDto): ClientPhoneResponseDto {
        phoneResponse.actionType = request.actionType
        phoneResponse.actionPayload = request.actionPayload
        phoneResponse.autoArchive = request.autoArchive
        phoneResponse.keywords.clear()
        request.keywords?.forEach { phoneResponse.addKeyword(ResponseKeyword(it)) }
        phoneResponseRepository.save(phoneResponse)
        return ClientPhoneResponseDto(phoneResponse)
    }

    fun deleteByClients(clients: List<Long>) {
        phoneResponseRepository.findAllByClientIdIn(clients).forEach { delete(it) }
    }

    fun delete(phoneResponse: PhoneResponse) {
        phoneResponse.deletedAt = Instant.now()
        phoneResponse.actionPayload = null
        phoneResponse.keywords.clear()
        phoneResponseRepository.save(phoneResponse)
    }
}
