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

package com.cloudcontactai.smsreceiver.model.dto

import com.cloudcontactai.smsreceiver.model.PhoneResponse
import com.cloudcontactai.smsreceiver.model.type.PhoneActionPayload
import com.cloudcontactai.smsreceiver.model.type.PhoneActionType

data class ClientPhoneResponseDto(
    val phoneNumber: String,
    val actionType: PhoneActionType,
    val actionPayload: PhoneActionPayload?,
    val keywords: List<String>,
    val autoArchive: Boolean
) {
    constructor(phoneResponse: PhoneResponse) : this(
            phoneResponse.phoneNumber!!,
            phoneResponse.actionType,
            phoneResponse.actionPayload,
            phoneResponse.keywords.map { it.keyword!! },
            phoneResponse.autoArchive
    )
}