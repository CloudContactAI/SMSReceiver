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

import com.cloudcontactai.smsreceiver.model.dto.ClientPhoneResponseDto
import com.cloudcontactai.smsreceiver.model.dto.PhoneResponseRequestDto
import com.cloudcontactai.smsreceiver.service.PhoneResponseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*


@RestController
@Validated
@RequestMapping("/api/v1/phone-response")
class PhoneResponseController {
    @Autowired
    lateinit var phoneResponseService: PhoneResponseService

    @GetMapping("/{clientId}")
    fun getClientPhoneResponses(@PathVariable clientId: Long): List<ClientPhoneResponseDto> {
        return phoneResponseService.getByClient(clientId)
    }

    @GetMapping("/{clientId}/phone/{phone}")
    fun getClientPhoneResponse(@PathVariable clientId: Long, @PathVariable phone: String): List<ClientPhoneResponseDto> {
        return phoneResponseService.getPhoneByClient(clientId, phone)
    }

    @PostMapping("/{clientId}/phone/{phone}")
    fun saveClientPhoneResponse(@PathVariable clientId: Long, @PathVariable phone: String,
                                @RequestBody request: List<PhoneResponseRequestDto>): List<ClientPhoneResponseDto> {
        return phoneResponseService.saveAll(clientId, phone, request)
    }
}

