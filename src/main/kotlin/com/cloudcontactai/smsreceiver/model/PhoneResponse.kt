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

package com.cloudcontactai.smsreceiver.model

import com.cloudcontactai.smsreceiver.converter.PhoneActionPayloadConverter
import com.cloudcontactai.smsreceiver.model.type.PhoneActionPayload
import com.cloudcontactai.smsreceiver.model.type.PhoneActionType
import org.springframework.data.annotation.CreatedDate
import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "phone_responses")
class PhoneResponse() {
    constructor(clientId: Long, phoneNumber: String): this() {
        this.clientId = clientId
        this.phoneNumber = phoneNumber
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "phone_responses_sequence")
    @SequenceGenerator(name = "phone_responses_sequence", sequenceName = "phone_responses_sequence", allocationSize = 1)
    var id: Long = 0

    @Column(name = "client_id")
    var clientId: Long? = null

    @Column(name = "phone_number")
    var phoneNumber: String? = null

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    var actionType: PhoneActionType = PhoneActionType.MESSAGE

    @Column(name = "action_payload")
    @Convert(converter = PhoneActionPayloadConverter::class)
    var actionPayload: PhoneActionPayload? = null

    @Column(name = "auto_archive")
    var autoArchive: Boolean = true

    @OneToMany(mappedBy = "phoneResponse", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    val keywords = mutableListOf<ResponseKeyword>()

    fun addKeyword(keyword: ResponseKeyword) {
        this.keywords.add(keyword)
        keyword.phoneResponse = this
    }
}